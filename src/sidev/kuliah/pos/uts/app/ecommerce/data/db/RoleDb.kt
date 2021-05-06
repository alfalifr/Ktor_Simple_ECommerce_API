package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object Roles: IdTable<Int>("roles") {
    override val id: Column<EntityID<Int>> = integer("id").entityId()
    val name: Column<String> = varchar("name", 20)

    override val primaryKey: PrimaryKey? = PrimaryKey(id)
}