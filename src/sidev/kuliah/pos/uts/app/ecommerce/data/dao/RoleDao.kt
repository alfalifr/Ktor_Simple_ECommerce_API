package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Roles
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SimpleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Role

object RoleDao: SimpleDao<Role, Roles> {
    override val table: Roles = Roles
    override val tableId: Column<EntityID<Int>>? = Roles.id

    override fun Roles.generateModel(row: ResultRow): Role = Role(
        row[id].value,
        row[name],
    )

    override fun Roles.onInsert(insert: InsertStatement<*>, model: Role) {
        insert[name] = model.name
    }
}