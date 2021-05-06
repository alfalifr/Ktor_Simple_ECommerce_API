package sidev.kuliah.pos.uts.app.ecommerce.data.model

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.`java-time`.datetime
import sidev.kuliah.pos.uts.app.ecommerce.util.reference
import java.time.LocalDateTime

data class Session(
    val userId: Int,
    val session: String,
    //val exp: String,
)