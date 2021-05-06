package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.db.ItemStocks
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Items
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

    override fun Items.onInsert(insert: InsertStatement<*>, model: Item) {
        insert[name] = model.name
        insert[price] = model.price
        insert[owner] = model.owner
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

    fun deleteAllByOwner(userId: Int): Boolean = transaction {
        Items.deleteWhere { Items.owner eq userId } > 0
    }
}

object ItemStockDao: SimpleDao<ItemStock, ItemStocks> {
    override val table: ItemStocks = ItemStocks
    override val tableId: Column<EntityID<Int>>? = ItemStocks.itemId

    override fun ItemStocks.generateModel(row: ResultRow): ItemStock = ItemStock(
        row[itemId].value,
        row[count],
    )

    override fun ItemStocks.onInsert(insert: InsertStatement<*>, model: ItemStock) {
        insert[itemId] = model.itemId
        insert[count] = model.count
    }

    fun update(itemId: Int, newStock: Int): Boolean = transaction {
        ItemStocks.update({ ItemStocks.itemId eq itemId }) {
            it[count] = newStock
        } == 1
    }
}