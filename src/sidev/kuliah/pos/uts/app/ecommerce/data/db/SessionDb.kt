package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import sidev.kuliah.pos.uts.app.ecommerce.util.reference
import java.time.LocalDateTime

object Sessions: Table("sessions") {
    val userId = reference(Users.id)
    val session: Column<String> = varchar("session", 60)
    val exp: Column<LocalDateTime> = datetime("exp")
}