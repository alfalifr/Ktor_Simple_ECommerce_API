package sidev.kuliah.pos.uts.app.ecommerce.util

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SessionDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleRespond


suspend fun PipelineContext<Unit, ApplicationCall>.onSellerRole(
    onValid: suspend PipelineContext<Unit, ApplicationCall>.(token: String) -> Unit,
) {
    val rawToken = call.request.headers[Const.KEY_AUTH] ?: return call.simpleRespond(
        "forbidden access",
        HttpStatusCode.Forbidden,
    )

    val token = rawToken.split(" ").last()

    val userId: Int
    if(SessionDao.exists(token).also { userId = it } >= 0){
        val isValid = UserDao.getRole(userId) == Datas.ID_SELLER
        if(isValid){
            onValid(token)
            return
        }
    }
    call.simpleRespond("forbidden access", HttpStatusCode.Forbidden)
}