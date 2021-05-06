package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import sidev.kuliah.pos.uts.app.ecommerce.util.reference

object Users: IdTable<Int>("users") {
    override val id: Column<EntityID<Int>> = integer("id").entityId().autoIncrement()
    val name: Column<String> = varchar("name", 60)
    val email: Column<String> = varchar("email", 60).uniqueIndex()
    val balance: Column<Long> = long("balance")
    val pswdHash: Column<String> = varchar("pswd_hash", 70)
    val role = reference(Roles.id, "role_id")

    override val primaryKey: PrimaryKey? = PrimaryKey(id)
}

