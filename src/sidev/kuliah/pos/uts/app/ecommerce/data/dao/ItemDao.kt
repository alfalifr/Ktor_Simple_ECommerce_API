package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.db.ItemStocks
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Items
import sidev.kuliah.pos.uts.app.ecommerce.data.db.SimpleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Item
import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemStock

object ItemDao: SimpleDao<Item, Items> {
    override val table: Items = Items
    override val tableId: Column<EntityID<Int>>? = Items.id

    override fun Items.generateModel(row: ResultRow): Item = Item(
        row[id].value,
        row[name],
        row[price],
        row[owner].value,
    )

    override fun onInsert(model: Item): Items.(InsertStatement<Number>) -> Unit = {
        it[name] = model.name
        it[price] = model.price
        it[owner] = model.owner
    }

    fun readAllByOwner(userId: Int): List<Item> = transaction {
        val list = mutableListOf<Item>()
        Items.select { Items.owner eq userId }.forEach {
            list += Item(
                it[Items.id].value,
                it[Items.name],
                it[Items.price],
                it[Items.owner].value,
            )
        }
        list
    }
}

object ItemStockDao: SimpleDao<ItemStock, ItemStocks> {
    override val table: ItemStocks = ItemStocks
    override val tableId: Column<EntityID<Int>>? = ItemStocks.itemId

    override fun ItemStocks.generateModel(row: ResultRow): ItemStock = ItemStock(
        row[itemId].value,
        row[count],
    )

    override fun onInsert(model: ItemStock): ItemStocks.(InsertStatement<Number>) -> Unit = {
        it[itemId] = model.itemId
        it[count] = model.count
    }

    fun update(itemId: Int, newStock: Int): Boolean = transaction {
        ItemStocks.update({ ItemStocks.itemId eq itemId }) {
            it[count] = newStock
        } == 1
    }
}