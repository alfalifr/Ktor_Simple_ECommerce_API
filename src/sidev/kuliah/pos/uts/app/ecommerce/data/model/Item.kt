package sidev.kuliah.pos.uts.app.ecommerce.data.model

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.db.ItemStocks
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Items
import java.lang.IllegalArgumentException

data class Item(
    val id: Int,
    val name: String,
    val price: Long,
    val owner: Int,
)

data class ItemStock(
    val itemId: Int,
    val count: Int,
)

@Serializable
data class ItemDisplay(
    val id: Int,
    val name: String,
    val price: Long,
    val count: Int,
) {
    companion object {
        fun join(item: Item, stock: ItemStock): ItemDisplay {
            if(item.id != stock.itemId)
                throw IllegalArgumentException()
            return ItemDisplay(item.id, item.name, item.price, stock.count)
        }

        fun join(items: List<Item>, stocks: List<ItemStock>): List<ItemDisplay> =
            items.mapIndexed { index, item ->
                join(item, stocks[index])
            }

        fun from(q: Query): List<ItemDisplay> = transaction {
            q.map {
                ItemDisplay(
                    it[Items.id].value,
                    it[Items.name],
                    it[Items.price],
                    it[ItemStocks.count],
                )
            }
        }
    }
}