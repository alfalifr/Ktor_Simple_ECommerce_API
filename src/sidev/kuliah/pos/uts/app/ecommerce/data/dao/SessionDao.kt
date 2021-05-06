package sidev.kuliah.pos.uts.app.ecommerce.sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Sessions
import sidev.kuliah.pos.uts.app.ecommerce.data.db.SimpleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Session
import java.time.LocalDateTime

object SessionDao: SimpleDao<Session, Sessions> {
    override val table: Sessions = Sessions
    override val tableId: Column<EntityID<Int>>? = Sessions.userId

    override fun Sessions.generateModel(row: ResultRow): Session = Session(
        row[userId].value,
        row[session],
        row[exp].toString(),
    )

    override fun onInsert(model: Session): Sessions.(InsertStatement<Number>) -> Unit = {
        it[userId] = model.userId
        it[session] = model.session
        it[exp] = LocalDateTime.parse(model.exp)
    }
}