package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.timestamp
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Users.autoIncrement
import sidev.kuliah.pos.uts.app.ecommerce.util.reference

object Transactions: IdTable<Int>("transactions") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
    val timestamp = timestamp("timestamp")
    val itemId = integer("item_id").references(Items.id)
    val count = integer("count")
    val buyer = integer("buyer_id").references(Users.id)
    val seller = integer("seller_id").references(Users.id)
    val status = integer("status_id").references(TransactionStatuss.id)

    override val primaryKey: PrimaryKey? = PrimaryKey(id)
}

object TransactionStatuss: IdTable<Int>("transaction_status") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
    val name = varchar("name", 20)

    override val primaryKey: PrimaryKey? = PrimaryKey(id)
}