package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.BatchInsertStatement
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Users
import java.lang.IllegalArgumentException

interface SimpleDao<M, T: Table> {
    val table: T
    val tableId: Column<EntityID<Int>>?
    fun T.generateModel(row: ResultRow): M
    fun T.onInsert(insert: InsertStatement<*>, model: M)
    fun onSingleInsert(model: M): T.(InsertStatement<*>) -> Unit = {
        onInsert(it, model)
    }
    fun onBatchInsert(): BatchInsertStatement.(M) -> Unit = {
        this@SimpleDao.table.onInsert(this, it)
    }
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
            val id = table.insert(onSingleInsert(model))
            if(tableId != null && table is IdTable<*>){
                onIdGenerated?.invoke((id get tableId!!).value)
            }
            true
        } catch (e: Exception){
            onError?.invoke(e)
            false
        }
    }

    /**
     * Returns list of item id that was successfuly inserted.
     */
    fun batchInsert(
        vararg models: M,
        onError: ((Exception) -> Unit)?= null,
        idsContainer: MutableList<Int>?= null,
    ): Int = transaction {
        try {
            println("batchInsert() models= ${models.joinToString()}")
            val resultRows = table.batchInsert(models.asList(), body = onBatchInsert())
            println("batchInsert() resultRows.size= ${resultRows.size}")
            //val ids = mutableListOf<Int>()

            if(tableId != null && table is IdTable<*> && idsContainer != null){
                val tableId = tableId!!
                for(row in resultRows){
                    idsContainer += row[tableId].value
                }
            }
            resultRows.size
        } catch (e: Exception){
            e.printStackTrace()
            onError?.invoke(e)
            0
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
    fun deleteById(id: Int, op: (SqlExpressionBuilder.() -> Op<Boolean>)?= null): Boolean = transaction {
        table.deleteWhere { tableId?.eq(id) ?: op?.invoke(this) ?: throw IllegalArgumentException() } == 1
    }
}