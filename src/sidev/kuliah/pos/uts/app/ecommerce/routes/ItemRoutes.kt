package sidev.kuliah.pos.uts.app.ecommerce.routes

import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.select
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemStockDao
import sidev.kuliah.pos.uts.app.ecommerce.data.db.ItemStocks
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Items
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Item
import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemDisplay
import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemStock
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.Util
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleFailRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleForbiddenRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.onSellerRole

fun Route.itemRoutes() {
    route(ItemRoutes){
        register(ItemRoutes.PostItem)
        register(ItemRoutes.UpdateStock)
        register(ItemRoutes.DisplayItem)
        register(ItemRoutes.DisplayItemByOwner)
        register(ItemRoutes.DeleteOneItem)
        register(ItemRoutes.DeleteItemByOwner)
    }
/*
    route(AppRoutes.Items.completeUrl()) {
        post(AppRoutes.Items.PostItem.url()) {
            onSellerRole {
                val body = call.receiveText()
                val gson = Gson()

                val item = gson.fromJson(body, Item::class.java)
                val rawItemStock = gson.fromJson(body, ItemStock::class.java)//.copy(itemId = item.id)

                var overallTransSuc = false
                var newItemId: Int = -1
                if(ItemDao.insert(item){ newItemId = it }){
                    val itemStock = rawItemStock.copy(itemId = newItemId)
                    if(ItemStockDao.insert(itemStock)){
                        overallTransSuc= true
                    }
                }

                if(overallTransSuc){
                    call.simpleOkRespond()
                } else {
                    call.simpleRespond("item was not inserted", HttpStatusCode.InternalServerError)
                }
            }
        }
        post(AppRoutes.Items.UpdateStock.url()) {
            onSellerRole {
                AppRoutes.Items.UpdateStock.doOp(this)
            }
        }
        get(AppRoutes.Items.DisplayItem.url()) {
            val items = ItemDao.read(20)
            val itemStocks = ItemStockDao.read(20)
            val itemDisplays = ItemDisplay.join(items, itemStocks)

            call.respond(itemDisplays)
        }
        get(AppRoutes.Items.DisplayItemByOwner.url()) {
            val sellerId = call.parameters[Const.KEY_USER_ID]?.toInt() ?: return@get call.simpleRespond(
                "expecting for ${Const.KEY_USER_ID}",
                HttpStatusCode.BadRequest,
            )
            val rows = (Items fullJoin ItemStocks).select { Items.id eq sellerId }
            val itemDisplays = ItemDisplay.from(rows)

            call.respond(itemDisplays)
        }
    }
 */
}


object ItemRoutes: AppRoute {
    override val parent: AppRoute? = null
    override val method: HttpMethod = HttpMethod.Get
    override fun url(): String = "items"


    object DisplayItem: AppRoute by get("", {
        val items = ItemDao.read(20)
        val itemStocks = ItemStockDao.read(20)
        val itemDisplays = ItemDisplay.join(items, itemStocks)

        call.respond(itemDisplays)
        super.doOp(this)
    })

    object PostItem: AppRoute by post("", {
        var overallTransSuc = false
        onSellerRole { session ->
            val body = call.receiveText()
            val gson = Gson()

            val items = mutableListOf<Item>()
            val rawItemStocks = mutableListOf<ItemStock>()

            val jsonArray = JsonParser.parseString(body).asJsonArray
            for(e in jsonArray){
                val obj = e.asJsonObject

                val item = gson.fromJson(obj, Item::class.java).copy(owner = session.userId)
                val rawItemStock = gson.fromJson(obj, ItemStock::class.java)//.copy(itemId = item.id)

                items += item
                rawItemStocks += rawItemStock
            }

            println("items= $items")

            val insertedId = mutableListOf<Int>()
            val insertedItemCount = ItemDao.batchInsert(*items.toTypedArray(), idsContainer = insertedId)

            println("insertedId= $insertedId")
            println("insertedItemCount= $insertedItemCount")

            val stocks = rawItemStocks.mapIndexed { i, stock ->
                stock.copy(itemId = insertedId[i])
            }

            println("stocks= $stocks")

            val insertedStockCount = ItemStockDao.batchInsert(*stocks.toTypedArray())

            println("insertedStockCount= $insertedStockCount")

            when(insertedStockCount) {
                items.size -> {
                    overallTransSuc = true
                    call.simpleOkRespond()
                }
                0 -> call.simpleRespond("items was not inserted", HttpStatusCode.InternalServerError)
                else -> call.simpleRespond("item was inserted partially", HttpStatusCode.PartialContent)
            }
        }
        overallTransSuc
    })

    object DeleteOneItem: AppRoute by delete("{${Const.KEY_ITEM_ID}}", {
        var success = false
        onSellerRole { session ->
            val itemId = call.parameters[Const.KEY_ITEM_ID]?.toInt() ?: return@onSellerRole call.simpleRespond(
                "expecting for ${Const.KEY_ITEM_ID}",
                HttpStatusCode.BadRequest,
            )
            val ownerId = ItemDao.getOwnerId(itemId)
            if(ownerId == session.userId){
                success = ItemDao.deleteById(itemId)
            } else {
                call.simpleForbiddenRespond()
            }
        }
        if(success){
            call.simpleOkRespond()
        } else {
            call.simpleFailRespond()
        }
        success
    })

    object DisplayItemByOwner: AppRoute by get("user/{${Const.KEY_USER_ID}}", {
        val sellerId = call.parameters[Const.KEY_USER_ID]?.toInt() ?: return@get false.also {
            call.simpleRespond(
                    "expecting for ${Const.KEY_USER_ID}",
                    HttpStatusCode.BadRequest,
            )
        }
        val rows = (Items fullJoin ItemStocks).select { Items.id eq sellerId }
        val itemDisplays = ItemDisplay.from(rows)

        call.respond(itemDisplays)
        super.doOp(this)
    })

    object DeleteItemByOwner: AppRoute by DisplayItemByOwner {
        override val method: HttpMethod = HttpMethod.Delete
        override suspend fun doOp(pipeline: PipelineContext<Unit, ApplicationCall>): Boolean = pipeline.run {
            var success = false
            onSellerRole { session ->
                success = ItemDao.deleteAllByOwner(session.userId)
                if(success){
                    call.simpleOkRespond()
                } else {
                    call.simpleFailRespond()
                }
            }
            return success
        }
    }

    object UpdateStock: AppRoute by post("stock/{${Const.KEY_ITEM_ID}}", {
        var success = false
        onSellerRole {
            val body = call.receiveText()
            val id = call.parameters[Const.KEY_ITEM_ID]?.toInt() ?: return@onSellerRole call.simpleRespond(
                    "expecting for ${Const.KEY_ITEM_ID}",
                    HttpStatusCode.BadRequest,
            )

            try {
                val stock = JsonParser.parseString(body).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_STOCK).asInt
                success = ItemStockDao.update(id, stock)
            } catch (e: Exception) { }
            if(success){
                call.simpleOkRespond()
            } else {
                call.simpleRespond("internal error", HttpStatusCode.InternalServerError)
            }
        }
        success
    })
}