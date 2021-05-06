package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction

interface SimpleDao<M, T: Table> {
    val table: T
    val tableId: Column<EntityID<Int>>?
    fun T.generateModel(row: ResultRow): M
    fun onInsert(model: M): T.(InsertStatement<Number>) -> Unit
    //fun onUpdate(model: M): T.(UpdateStatement) -> Unit

    fun read(limit: Int = 10): List<M> = transaction {
        val list = mutableListOf<M>()
        table.selectAll().limit(limit).forEach {
            list += table.generateModel(it)
        }
        list
    }

    fun readById(id: Int): M? = transaction {
        if(tableId != null){
            table.select { tableId!! eq id }.firstOrNull()?.let {
                table.generateModel(it)
            }
        } else null
    }

    fun insert(
        model: M,
        onError: ((Exception) -> Unit)?= null,
        onIdGenerated: ((Int) -> Unit)?= null,
    ): Boolean = transaction {
        try {
            val id = table.insert(onInsert(model))
            if(tableId != null && table is IdTable<*>){
                onIdGenerated?.invoke((id get tableId!!).value)
            }
            true
        } catch (e: Exception){
            onError?.invoke(e)
            false
        }
    }
/*
    fun updateById(id: Int, model: M): Boolean = transaction {
        try {
            if(tableId != null){
                table.update({ tableId!! eq id }, body = onUpdate(model)) == 1
            } else false
        } catch (e: Exception) {
            false
        }
    }
 */
    fun deleteById(id: Int, op: SqlExpressionBuilder.() -> Op<Boolean>): Boolean = transaction {
        table.deleteWhere(op = op) == 1
    }
}