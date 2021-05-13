package sidev.kuliah.pos.uts.app.ecommerce.routes

import com.google.gson.Gson
import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SessionDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Session
import sidev.kuliah.pos.uts.app.ecommerce.data.model.User
import sidev.kuliah.pos.uts.app.ecommerce.data.model.UserDetail
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.Util
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleForbiddenRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleInternalErrorRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleNotFoundRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleUnauthRespond

fun Route.authRoutes(){
    route(AuthRoutes){
        register(AuthRoutes.SignUp)
        register(AuthRoutes.Login)
        register(AuthRoutes.Logout)
    }
/*
    route(AuthRoutes.completeUrl()) {
        post(AppRoutes.Auth.SignUp.url()) {
            val body = call.receiveText()
            val gson = Gson()

            val user= gson.fromJson(body, User::class.java)

            if(UserDao.existsEmail(user.email) < 0){
                val userDetailInit= gson.fromJson(body, UserDetail::class.java)
                val json = JsonParser.parseString(body).asJsonObject

                println("user= $user")
                println("userDetailInit= $userDetailInit")

                val pswd = json.getAsJsonPrimitive("password").asString // not hashed cuz it is from form
                val pswdHash = Util.sha256(pswd)

                val userDetail = userDetailInit.copy(user = user, pswdHash = pswdHash)

                var e: Exception?= null
                var newId: Int?= null
                val success = UserDao.insert(userDetail, {
                    e = it
                }) {
                    newId = it
                }
                if(success){
                    call.simpleOkRespond()
                } else {
                    call.simpleRespond("internal error", HttpStatusCode.InternalServerError)
                }
            } else {
                call.simpleRespond("email already exists", HttpStatusCode.BadRequest)
            }
        }
        get(AppRoutes.Auth.Login.url()) {
            val params = call.parameters
            val email = params[Const.KEY_EMAIL] ?: return@get call.respondText(
                "Expecting for email",
                status = HttpStatusCode.BadRequest,
            )
            val pswd = params[Const.KEY_PSWD] ?: return@get call.respondText(
                "Expecting for password",
                status = HttpStatusCode.BadRequest,
            )
            val pswdHash = Util.sha256(pswd)

            var userId: Int
            if(UserDao.exists(email, pswdHash).also { userId = it } > -1){
                if(!SessionDao.hasLoggedIn(userId)){
                    var token: String
                    do {
                        token = Util.randomStr()
                    } while(SessionDao.exists(token) >= 0)

                    val session = Session(userId, token)
                    SessionDao.insert(session)
                    call.respond(token)
                } else {
                    call.simpleRespond("the user has currently logged in", HttpStatusCode.Forbidden)
                }
            } else {
                call.simpleRespond("email or password invalid", HttpStatusCode.NotFound)
            }
        }
        post(AppRoutes.Auth.Logout.url()){
            val headers = call.request.headers
            val rawToken = headers[Const.KEY_AUTH] ?: return@post call.simpleRespond(
                "forbidden access",
                HttpStatusCode.Forbidden
            )
            val token = rawToken.split(" ").last()

            if(SessionDao.exists(token) >= 0){
                if(SessionDao.deleteByToken(token)){
                    call.simpleOkRespond()
                } else {
                    call.simpleRespond("internal error while logout", HttpStatusCode.InternalServerError)
                }
            } else {
                call.simpleRespond("token not found", HttpStatusCode.NotFound)
            }
        }
    }
 */
}


object AuthRoutes: AppRoute {
    override val parent: AppRoute? = null
    override val method: HttpMethod = HttpMethod.Get
    override fun url(): String = "auth"

    object SignUp: AppRoute by post("signup", {
        val body = call.receiveText()
        val gson = Gson()

        println("SignUp body= $body")

        val user= gson.fromJson(body, User::class.java)

        if(UserDao.existsEmail(user.email) < 0){
            val userDetailInit= gson.fromJson(body, UserDetail::class.java)
            val json = JsonParser.parseString(body).asJsonObject

            println("user= $user")
            println("userDetailInit= $userDetailInit")

            val pswd = json.getAsJsonPrimitive("password").asString // not hashed cuz it is from form
            val pswdHash = Util.sha256(pswd)

            val userDetail = userDetailInit.copy(user = user, pswdHash = pswdHash)
            println("userDetail= $userDetail")

            var e: Exception?= null
            var newId: Int?= null
            val success = UserDao.insert(userDetail, {
                e = it
            }) {
                newId = it
            }
            if(success){
                call.simpleOkRespond()
            } else {
                call.simpleInternalErrorRespond()
            }
        } else {
            call.simpleRespond(Const.MSG_EMAIL_EXISTS, HttpStatusCode.Conflict)
        }
        true
    })

    object Login: AppRoute by get("login", {
        val params = call.parameters
        val email = params[Const.KEY_EMAIL] ?: return@get false.also {
            call.respondText(
                    "Expecting for email",
                    status = HttpStatusCode.BadRequest,
            )
        }
        val pswd = params[Const.KEY_PSWD] ?: return@get false.also {
            call.respondText(
                "Expecting for password",
                status = HttpStatusCode.BadRequest,
            )
        }
        val pswdHash = Util.sha256(pswd)

        println("Login params= $params")
        println("Login email= $email")
        println("Login pswd= $pswd")
        println("Login pswdHash= $pswdHash")

        var userId: Int
        if(UserDao.exists(email, pswdHash).also { userId = it } > -1){
            if(!SessionDao.hasLoggedIn(userId)){
                var token: String
                do {
                    token = Util.randomStr()
                } while(SessionDao.exists(token) >= 0)

                val session = Session(userId, token)
                SessionDao.insert(session)
                call.respond(token)
            } else {
                call.simpleForbiddenRespond("the user has currently logged in")
            }
        } else {
            call.simpleNotFoundRespond(Const.MSG_INVALID_EMAIL_PASSWORD)
        }
        true
    })

    object Logout: AppRoute by get("logout", {
        val headers = call.request.headers
        val rawToken = headers[Const.KEY_AUTH] ?: return@get false.also {
            call.simpleUnauthRespond()
        }
        val token = rawToken.split(" ").last()

        if(SessionDao.exists(token) >= 0){
            if(SessionDao.deleteByToken(token)){
                call.simpleOkRespond()
            } else {
                call.simpleInternalErrorRespond()
            }
        } else {
            call.simpleNotFoundRespond()
        }
        true
    })
}