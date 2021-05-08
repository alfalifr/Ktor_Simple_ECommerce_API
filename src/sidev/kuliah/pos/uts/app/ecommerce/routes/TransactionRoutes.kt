package sidev.kuliah.pos.uts.app.ecommerce.routes

import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemStockDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.TransactionDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Transaction
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleBadReqRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleConflictRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleForbiddenRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleInternalErrorRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.onBuyerRole
import sidev.kuliah.pos.uts.app.ecommerce.util.onSellerRole
import java.time.Instant


fun Route.transactionRoutes() {
    route(TransactionRoutes) {
        register(TransactionRoutes.Order)
        register(TransactionRoutes.Approve)
        register(TransactionRoutes.Reject)
        register(TransactionRoutes.Pay)
    }
}

object TransactionRoutes: AppRoute {
    override val parent: AppRoute? = null
    override val method: HttpMethod = HttpMethod.Get
    override fun url(): String = "trans"

    private suspend fun PipelineContext<Unit, ApplicationCall>.approveOrder(approve: Boolean): Boolean {
        var success = false
        onSellerRole { session ->
            val transId = call.parameters[Const.KEY_TRANSACTION_ID]?.toIntOrNull() ?: return@onSellerRole call.simpleBadReqRespond(
                    "expecting for ${Const.KEY_TRANSACTION_ID}"
            )
            val trans = TransactionDao.readById(transId) ?: return@onSellerRole call.simpleInternalErrorRespond(
                    "no transaction data found"
            )
            if(trans.status != Datas.ID_BUY) return@onSellerRole call.simpleBadReqRespond(
                    "transaction status is not valid"
            )
            if(trans.seller != session.userId) return@onSellerRole call.simpleForbiddenRespond(
                    Const.MSG_NOT_SELLER_ITEM
            )

            if(TransactionDao.updateTransStatus(transId, if(approve) Datas.ID_APPROVE else Datas.ID_REJECT)){
                success = true
                return@onSellerRole call.simpleOkRespond()
            }
            if(!success) call.simpleInternalErrorRespond()
        }
        return success
    }

    object Order: AppRoute by post("order", {
        var success = false
        onBuyerRole { session ->
            val body = call.receiveText()
            val jsonObj = JsonParser.parseString(body).asJsonObject
            val itemIdStr = jsonObj.getAsJsonPrimitive(Const.KEY_ITEM_ID).asString
            val countStr = jsonObj.getAsJsonPrimitive(Const.KEY_COUNT).asString

            var itemId = -1
            var count = -1
            if(itemIdStr?.toIntOrNull()?.also { itemId = it } == null
                    || countStr?.toIntOrNull()?.also { count = it } == null
            ) return@onBuyerRole call.simpleBadReqRespond(
                    "expecting for ${Const.KEY_ITEM_ID} and ${Const.KEY_COUNT}"
            )

            if(count <= 0) return@onBuyerRole call.simpleBadReqRespond(
                    "expecting for positive item count"
            )

            val currentStatus = TransactionDao.getStatusByItemAndBuyer(itemId, session.userId)
            when(currentStatus.lastOrNull()){
                Datas.ID_PAY, Datas.ID_REJECT, null -> {
                    println("TransRoutes.Order() itemId= $itemId")
                    val item = ItemDao.readById(itemId) ?: return@onBuyerRole call.simpleInternalErrorRespond(
                            "no item data found"
                    )
                    val itemStock = ItemStockDao.readByItemId(item.id) ?: return@onBuyerRole call.simpleInternalErrorRespond(
                            "no item stock data found"
                    )
                    if(itemStock.count <= count) return@onBuyerRole call.simpleBadReqRespond(
                            Const.MSG_INSUFFICIENT_STOCK
                    )
                    val totalPrice = item.price * count
                    val buyerBalance = UserDao.getBalance(session.userId)
                    when {
                        buyerBalance < 0 -> return@onBuyerRole call.simpleInternalErrorRespond(
                                "no balance data found"
                        )
                        buyerBalance < totalPrice -> return@onBuyerRole call.simpleBadReqRespond(
                                Const.MSG_INSUFFICIENT_BALANCE
                        )
                    }

                    val sellerId = ItemDao.getOwnerId(itemId)
                    if(sellerId < 0) return@onBuyerRole call.simpleInternalErrorRespond()
                    val newTrans = Transaction(-1, Instant.now().toString(), itemId, count, session.userId, sellerId, Datas.ID_BUY)

                    var transId = -1
                    if(TransactionDao.insert(newTrans) { transId = it }) {
                        success = true
                        return@onBuyerRole call.respond(mapOf(Const.KEY_TRANSACTION_ID to transId))
                    }
                }
                else -> return@onBuyerRole call.simpleConflictRespond(Const.MSG_CONFLICT_TRANS)
            }
            if(!success) call.simpleInternalErrorRespond()
        }
        success
    })

    object Approve: AppRoute by post("approve/{${Const.KEY_TRANSACTION_ID}}", {
        approveOrder(true)
    })

    object Reject: AppRoute by post("reject/{${Const.KEY_TRANSACTION_ID}}", {
        approveOrder(false)
    })

    object Pay: AppRoute by post("pay/{${Const.KEY_TRANSACTION_ID}}", {
        var success = false
        onBuyerRole { session ->
            val transId = call.parameters[Const.KEY_TRANSACTION_ID]?.toIntOrNull() ?: return@onBuyerRole call.simpleBadReqRespond(
                    "expecting for ${Const.KEY_TRANSACTION_ID}"
            )
            val trans = TransactionDao.readById(transId) ?: return@onBuyerRole call.simpleInternalErrorRespond(
                    "no transaction data found"
            )
            if(trans.status != Datas.ID_APPROVE) return@onBuyerRole call.simpleBadReqRespond(
                    "transaction status is not valid"
            )
            val item = ItemDao.readById(trans.itemId) ?: return@onBuyerRole call.simpleInternalErrorRespond(
                    "no item data found"
            )
            val totalPrice = item.price * trans.count
            val buyerBalance = UserDao.getBalance(session.userId)
            if(buyerBalance < 0) return@onBuyerRole call.simpleInternalErrorRespond(
                    "no balance data found"
            )

            var paymentSucc = false
            transaction {
                val sellerBalance = UserDao.getBalance(item.owner)
                UserDao.updateBalance(item.owner, sellerBalance + totalPrice)
                UserDao.updateBalance(session.userId, buyerBalance - totalPrice)
                val initStock = ItemStockDao.readByItemId(item.id) ?: return@transaction
                if(ItemStockDao.update(item.id, initStock.count - trans.count)){
                    if(TransactionDao.updateTransStatus(transId, Datas.ID_PAY)){
                        paymentSucc = true
                    }
                }
            }
            if(paymentSucc){
                success = true
                return@onBuyerRole call.simpleOkRespond()
            }
        }
        if(!success) call.simpleInternalErrorRespond()
        success
    })
}