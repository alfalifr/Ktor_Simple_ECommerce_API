package sidev.kuliah.pos.uts.app.ecommerce.routes

import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.util.pipeline.*
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemStockDao
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleRespond

object AppRoutes {
    object Auth: AppRoute {
        override val parent: AppRoute? = null
        override fun url(): String = "auth"

        object SignUp: AppRoute {
            override val parent: AppRoute? = Auth
            override fun url(): String = "signup"
        }
        object Login: AppRoute {
            override val parent: AppRoute? = Auth
            override fun url(): String = "login"
        }
        object Logout: AppRoute {
            override val parent: AppRoute? = Auth
            override fun url(): String = "logout"
        }
    }
    object Items: AppRoute {
        override val parent: AppRoute? = null
        override fun url(): String = "items"

        object DisplayItem: AppRoute {
            override val parent: AppRoute? = Items
            override fun url(): String = ""
        }
        object PostItem: AppRoute by DisplayItem
        object DeleteItem: AppRoute by DisplayItem
        object DisplayItemByOwner: AppRoute {
            override val parent: AppRoute? = Items
            override fun url(): String = "user/{${Const.KEY_USER_ID}}"
        }
        object DeleteItemByOwner: AppRoute by DisplayItemByOwner {
            override suspend fun doOp(pipeline: PipelineContext<Unit, ApplicationCall>): Boolean = pipeline.run {
                val body = call.receiveText()
                val id = call.parameters[Const.KEY_USER_ID]?.toInt() ?: return false.also {
                    call.simpleRespond(
                        "expecting for ${Const.KEY_USER_ID}",
                        HttpStatusCode.BadRequest,
                    )
                }

                var success = false
                try {
                    ItemDao.deleteAllByOwner()
                    val stock = JsonParser.parseString(body).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_STOCK).asInt
                    success = ItemStockDao.update(id, stock)
                } catch (e: Exception) { }
                if(success){
                    call.simpleOkRespond()
                } else {
                    call.simpleRespond("internal error", HttpStatusCode.InternalServerError)
                }
                return success
            }
        }
        object UpdateStock: AppRoute {
            override val parent: AppRoute? = Items
            override fun url(): String = "stock/{${Const.KEY_ITEM_ID}}"
            override suspend fun doOp(pipeline: PipelineContext<Unit, ApplicationCall>): Boolean = pipeline.run {
                val body = call.receiveText()
                val id = call.parameters[Const.KEY_ITEM_ID]?.toInt() ?: return false.also {
                    call.simpleRespond(
                        "expecting for ${Const.KEY_ITEM_ID}",
                        HttpStatusCode.BadRequest,
                    )
                }

                var success = false
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
                return success
            }
        }
    }
    object Transactions: AppRoute {
        override val parent: AppRoute? = null
        override fun url(): String = "trans"

        object Buy: AppRoute {
            override val parent: AppRoute? = Transactions
            override fun url(): String = "buy"
        }
        object Approve: AppRoute {
            override val parent: AppRoute? = Transactions
            override fun url(): String = "approve"
        }
        object Pay: AppRoute {
            override val parent: AppRoute? = Transactions
            override fun url(): String = "pay"
        }
    }
}


interface AppRoute {
    val parent: AppRoute?
    fun completeUrl(): String = if(parent != null) "${parent!!.completeUrl()}/${url()}" else "/${url()}"
    fun url(): String
    suspend fun doOp(pipeline: PipelineContext<Unit, ApplicationCall>): Boolean = true
}