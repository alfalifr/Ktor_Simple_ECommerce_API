package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SimpleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.db.TransactionStatuss
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Transactions
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.model.TransactionStatus
import sidev.kuliah.pos.uts.app.ecommerce.data.model.User
import java.time.Instant

object TransactionDao: SimpleDao<Transaction, Transactions> {
    override val table: Transactions = Transactions
    override val tableId: Column<EntityID<Int>>? = Transactions.id

    override fun Transactions.generateModel(row: ResultRow): Transaction = Transaction(
        row[id].value,
        row[timestamp].toString(),
        row[count],
        row[buyer],
        row[seller],
        row[status],
    )

    override fun Transactions.onInsert(insert: InsertStatement<*>, model: Transaction) {
        insert[timestamp] = Instant.parse(model.timestamp)
        insert[count] = model.count
        insert[buyer] = model.buyer
        insert[seller] = model.seller
        insert[status] = model.status
    }

    fun readAllByBuyer(buyerId: Int): List<Transaction> = readAllByActor(Transactions.buyer, buyerId)
    fun readAllBySeller(sellerId: Int): List<Transaction> = readAllByActor(Transactions.seller, sellerId)
    private fun readAllByActor(actorColumn: Column<Int>, actorId: Int): List<Transaction> = transaction {
        val list = mutableListOf<Transaction>()
        val rows = Transactions.select { actorColumn eq actorId }
        for(row in rows){
            list += table.generateModel(row)
        }
        list
    }

    fun updateTransStatus(transId: Int, statusId: Int): Boolean = transaction {
        Transactions.update { id eq transId }
        true
    }
}

object TransactionStatusDao: SimpleDao<TransactionStatus, TransactionStatuss> {
    override val table: TransactionStatuss = TransactionStatuss
    override val tableId: Column<EntityID<Int>>? = TransactionStatuss.id

    override fun TransactionStatuss.generateModel(row: ResultRow): TransactionStatus = TransactionStatus(
        row[id].value,
        row[name],
    )

    override fun TransactionStatuss.onInsert(insert: InsertStatement<*>, model: TransactionStatus) {
        insert[name] = model.name
    }
}