package sidev.kuliah.pos.uts.app.ecommerce.util

import io.ktor.application.*
import io.ktor.util.pipeline.*
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SessionDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Session
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleBadReqRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleForbiddenRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleUnauthRespond


suspend fun PipelineContext<Unit, ApplicationCall>.onPaymentAuth(
    onValid: suspend PipelineContext<Unit, ApplicationCall>.(paymentAuth: String) -> Unit,
) {
    val rawToken = call.request.headers[Const.KEY_AUTH] ?: return call.simpleBadReqRespond(
            "expecting for payment token"
    )
    val token = rawToken.split(" ").last()

    if(token == Datas.SECRET_PAYMENT){
        onValid(token)
        return
    }
    call.simpleUnauthRespond()
}

suspend fun PipelineContext<Unit, ApplicationCall>.onLogin(
    onValid: suspend PipelineContext<Unit, ApplicationCall>.(session: Session) -> Unit,
) {
    val rawToken = call.request.headers[Const.KEY_AUTH] ?: return call.simpleBadReqRespond(
            "expecting for token"
    )
    val token = rawToken.split(" ").last()

    val userId: Int
    if(SessionDao.exists(token).also { userId = it } >= 0){
        onValid(Session(userId, token))
        return
    }
    call.simpleUnauthRespond()
}

private suspend fun PipelineContext<Unit, ApplicationCall>.onRole(
    roleId: Int,
    onValid: suspend PipelineContext<Unit, ApplicationCall>.(session: Session) -> Unit,
) = onLogin {
    val userRoleId = UserDao.getRole(it.userId)
    //println("roleId= $roleId")

    val isValid = userRoleId == roleId
    if(isValid){
        onValid(it)
        return@onLogin
    }
    call.simpleForbiddenRespond()
}
suspend fun PipelineContext<Unit, ApplicationCall>.onSellerRole(
    onValid: suspend PipelineContext<Unit, ApplicationCall>.(session: Session) -> Unit,
) = onRole(Datas.ID_SELLER, onValid)
suspend fun PipelineContext<Unit, ApplicationCall>.onBuyerRole(
    onValid: suspend PipelineContext<Unit, ApplicationCall>.(session: Session) -> Unit,
) = onRole(Datas.ID_BUYER, onValid)

//suspend fun PipelineContext<Unit, ApplicationCall>.onSellerRole()