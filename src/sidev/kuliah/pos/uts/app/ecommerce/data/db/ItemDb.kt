package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import sidev.kuliah.pos.uts.app.ecommerce.util.reference

object Items: IdTable<Int>("items") {
    override val id: Column<EntityID<Int>> = integer("id").entityId()
    val name = varchar("name", 100)
    val price = long("price")
    val owner = reference(Users.id)

    override val primaryKey: PrimaryKey? = PrimaryKey(id)
}

object ItemStocks: Table("item_stocks") {
    val itemId = reference(Items.id)
    val count = integer("count")
}