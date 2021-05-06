package sidev.kuliah.pos.uts.app.ecommerce.sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import sidev.kuliah.pos.uts.app.ecommerce.data.db.SimpleDao
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

    override fun onInsert(model: Transaction): Transactions.(InsertStatement<Number>) -> Unit = {
        it[timestamp] = Instant.parse(model.timestamp)
        it[count] = model.count
        it[buyer] = model.buyer
        it[seller] = model.seller
        it[status] = model.status
    }
}

object TransactionStatusDao: SimpleDao<TransactionStatus, TransactionStatuss> {
    override val table: TransactionStatuss = TransactionStatuss
    override val tableId: Column<EntityID<Int>>? = TransactionStatuss.id

    override fun TransactionStatuss.generateModel(row: ResultRow): TransactionStatus = TransactionStatus(
        row[id].value,
        row[name],
    )

    override fun onInsert(model: TransactionStatus): TransactionStatuss.(InsertStatement<Number>) -> Unit = {
        it[name] = model.name
    }
}