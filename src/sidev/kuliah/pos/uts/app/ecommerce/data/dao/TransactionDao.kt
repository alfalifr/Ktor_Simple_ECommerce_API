package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SimpleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.db.TransactionStatuss
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Transactions
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.model.TransactionStatus
import java.time.Instant

object TransactionDao: SimpleDao<Transaction, Transactions> {
    override val table: Transactions = Transactions
    override val tableId: Column<EntityID<Int>>? = Transactions.id

    override fun Transactions.generateModel(row: ResultRow): Transaction = Transaction(
        row[id].value,
        row[timestamp].toString(),
        row[count],
        row[buyer].value,
        row[seller].value,
        row[status].value,
    )

    override fun Transactions.onInsert(insert: InsertStatement<*>, model: Transaction) {
        insert[timestamp] = Instant.parse(model.timestamp)
        insert[count] = model.count
        insert[buyer] = model.buyer
        insert[seller] = model.seller
        insert[status] = model.status
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