package sidev.kuliah.pos.uts.app.ecommerce.routes

import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleBadReqRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleInternalErrorRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.onPaymentAuth


fun Route.paymentRoutes() {
    route(PaymentRoutes) {
        register(PaymentRoutes.Topup)
    }
}

object PaymentRoutes: AppRoute {
    override val parent: AppRoute? = null
    override val method: HttpMethod = HttpMethod.Get
    override fun url(): String = "payment"

    object Topup: AppRoute by post("topup/{${Const.KEY_TOPUP}}", {
        var success = false
        onPaymentAuth {
            val body = call.receiveText()
            val jsonObj = JsonParser.parseString(body).asJsonObject
            if(!jsonObj.has(Const.KEY_TOPUP) || !jsonObj.has(Const.KEY_USER_ID)) return@onPaymentAuth call.simpleBadReqRespond(
                    "expecting for ${Const.KEY_TOPUP} and ${Const.KEY_USER_ID}"
            )
            val topupCount = jsonObj.getAsJsonPrimitive(Const.KEY_TOPUP).asInt
            val userId = jsonObj.getAsJsonPrimitive(Const.KEY_USER_ID).asInt

            if(topupCount <= 0) return@onPaymentAuth call.simpleBadReqRespond(
                    "expecting for positive topup"
            )

            val buyerBalance = UserDao.getBalance(userId)
            if(buyerBalance < 0) return@onPaymentAuth call.simpleInternalErrorRespond(
                    "no balance data found"
            )

            if(UserDao.updateBalance(userId, buyerBalance + topupCount)) {
                success = true
                return@onPaymentAuth call.simpleOkRespond()
            }
        }
        if(!success) call.simpleInternalErrorRespond()
        success
    })
}