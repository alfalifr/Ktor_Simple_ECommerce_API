package sidev.kuliah.pos.uts.app.ecommerce.data.model

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