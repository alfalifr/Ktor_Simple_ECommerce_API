package sidev.kuliah.pos.uts.app.ecommerce.routes

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.parse
import kotlinx.serialization.serializer
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.User
import sidev.kuliah.pos.uts.app.ecommerce.data.model.UserDetail

fun Route.authRoutes(){
    route("/auth") {
        post("/signup") {
            val body = call.receiveText()
            val gson = Gson()

            val user= gson.fromJson(body, User::class.java)
            val userDetail= gson.fromJson(body, UserDetail::class.java).copy(user = user)

            var e: Exception?= null
            var newId: Int?= null
            val success = UserDao.insert(userDetail, {
                e = it
            }) {
                newId = it
            }
            call.respondText("success= $success, newId= $newId, e= $e, userDetail= $userDetail")
        }
    }
}