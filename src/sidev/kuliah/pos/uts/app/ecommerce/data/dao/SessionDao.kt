package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Sessions
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SimpleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Session
import java.time.LocalDateTime

object SessionDao: SimpleDao<Session, Sessions> {
    override val table: Sessions = Sessions
    override val tableId: Column<EntityID<Int>>? = Sessions.userId

    override fun Sessions.generateModel(row: ResultRow): Session = Session(
        row[userId].value,
        row[session],
        //row[exp].toString(),
    )

    override fun Sessions.onInsert(insert: InsertStatement<*>, model: Session) {
        insert[userId] = model.userId
        insert[session] = model.session
    }

    fun exists(token: String): Int = transaction {
        Sessions.select { Sessions.session eq token }.firstOrNull()?.get(Sessions.userId)?.value
            ?: -1
    }
    fun hasLoggedIn(userId: Int): Boolean = transaction {
        Sessions.select { Sessions.userId eq userId }.firstOrNull() != null
    }

    fun deleteByToken(token: String): Boolean = transaction {
        Sessions.deleteWhere { Sessions.session eq token } == 1
    }
}