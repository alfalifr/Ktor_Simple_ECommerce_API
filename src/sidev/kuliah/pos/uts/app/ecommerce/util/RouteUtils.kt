package sidev.kuliah.pos.uts.app.ecommerce.util

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.pipeline.*
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SessionDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Session
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleFailRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleForbiddenRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleRespond


suspend fun PipelineContext<Unit, ApplicationCall>.onSellerRole(
    onValid: suspend PipelineContext<Unit, ApplicationCall>.(session: Session) -> Unit,
) {
    val rawToken = call.request.headers[Const.KEY_AUTH] ?: return call.simpleFailRespond()

    val token = rawToken.split(" ").last()

    val userId: Int
    if(SessionDao.exists(token).also { userId = it } >= 0){
        val roleId = UserDao.getRole(userId)
        println("roleId= $roleId")
        val isValid = roleId == Datas.ID_SELLER
        if(isValid){
            onValid(Session(userId, token))
            return
        }
    }
    call.simpleForbiddenRespond()
}

//suspend fun PipelineContext<Unit, ApplicationCall>.onSellerRole()