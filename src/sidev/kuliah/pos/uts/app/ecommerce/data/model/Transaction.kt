package sidev.kuliah.pos.uts.app.ecommerce.data.model

import org.jetbrains.exposed.sql.`java-time`.timestamp
import sidev.kuliah.pos.uts.app.ecommerce.util.reference

data class Transaction(
    val id: Int,
    val timestamp: String,
    val itemId: Int,
    val count: Int,
    val buyer: Int,
    val seller: Int,
    val status: Int,
) {
    override fun equals(other: Any?): Boolean = other is Transaction
            && id == other.id && itemId == other.itemId && count == other.count
            && buyer == other.buyer && seller == other.seller && status == other.status

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + itemId
        result = 31 * result + count
        result = 31 * result + buyer
        result = 31 * result + seller
        result = 31 * result + status
        return result
    }
}

data class TransactionStatus(
    val id: Int,
    val name: String,
)