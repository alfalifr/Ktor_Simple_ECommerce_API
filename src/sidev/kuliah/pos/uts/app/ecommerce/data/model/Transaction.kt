package sidev.kuliah.pos.uts.app.ecommerce.data.model

import org.jetbrains.exposed.sql.`java-time`.timestamp
import sidev.kuliah.pos.uts.app.ecommerce.util.reference

data class Transaction(
    val id: Int,
    val timestamp: String,
    val count: Int,
    val buyer: Int,
    val seller: Int,
    val status: Int,
)

data class TransactionStatus(
    val id: Int,
    val name: String,
)