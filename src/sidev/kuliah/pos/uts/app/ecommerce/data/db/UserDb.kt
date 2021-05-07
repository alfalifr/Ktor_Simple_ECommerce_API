package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import sidev.kuliah.pos.uts.app.ecommerce.util.reference

object Users: IdTable<Int>("users") {
    override val id: Column<EntityID<Int>> = integer("id").autoIncrement().entityId()
    val name: Column<String> = varchar("name", 60)
    val email: Column<String> = varchar("email", 60).uniqueIndex()
    val balance: Column<Long> = long("balance")
    val pswdHash: Column<String> = varchar("pswd_hash", 70)
    val role = integer("role_id").references(Roles.id) //reference(Roles.id, "role_id").foreignKey

    override val primaryKey: PrimaryKey? = PrimaryKey(id)
}

