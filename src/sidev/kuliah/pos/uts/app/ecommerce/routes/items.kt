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
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.onSellerRole

fun Route.itemRoutes() {
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
}