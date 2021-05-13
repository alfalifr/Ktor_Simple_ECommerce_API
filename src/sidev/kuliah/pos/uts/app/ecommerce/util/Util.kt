package sidev.kuliah.pos.uts.app.ecommerce.util

import com.github.javafaker.Faker
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import sidev.kuliah.pos.uts.app.ecommerce.routes.AppRoute
import sidev.kuliah.pos.uts.app.ecommerce.routes.AuthRoutes
import sidev.kuliah.pos.uts.app.ecommerce.routes.DbConfigRoutes
import java.io.File
import java.io.PrintWriter
import java.security.MessageDigest

object Util {
    val sha256
        get()= MessageDigest.getInstance(Const.SHA_256)
    private val maskChar = arrayOf('?', '#')
    val faker = Faker()
    val gson by lazy { Gson() }

    fun sha256(raw: String): String = bytesToHex(sha256.digest(raw.toByteArray()))

    // From https://www.baeldung.com/sha-256-hashing-java
    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }

    fun randomStr(len: Int = Const.TOKEN_LEN): String {
        val b = StringBuilder(len)
        repeat(len){
            b.append(maskChar.random())
        }
        return faker.bothify(b.toString())
    }
/*
    fun String.replacePlaceholder(vararg vals: String): String {
        replace("\\{.+\\}".toRegex(), vals)
    }
 */
    fun Map<*, *>.toJsonString(): String = gson.toJson(this)

    fun dockerCurlString(method: HttpMethod, url: String, data: String?=null, vararg headers: Pair<Any, Any>): String {
        val headerStr = if(headers.isNotEmpty()) "-H \"${headers.joinToString(separator = ";") { "${it.first}:${it.second}" }}\""
        else ""

        val dataStr = if(data?.isNotBlank() == true) "-d $data" else ""

        //"docker exec -it ktor-run curl -H \"Content-Type: application/json\" --request POST -d \"{\\\"name\\\":\\\"ayu3\\\",\\\"password\\\":\\\"ereh\\\",\\\"email\\\":\\\"a3@a.a\\\",\\\"roleId\\\":\\\"1\\\"}\" http://127.1.0.1:8080/auth/signup"

        return "docker exec -it ${Const.DOCKER_CONTAINER_NAME} curl --request ${method.value} $headerStr $dataStr \"$url\""
    }

    fun dockerCurl(route: AppRoute, data: Any? = null,
                   paths: Array<out Pair<String, Any>> = emptyArray(),
                   params: String? = null,
                   vararg headers: Pair<Any, Any> = arrayOf("Content-Type" to "application/json")
    ): String {
        val usedParams = if(params != null) "?$params" else ""
        val specUrl = if(paths.isEmpty()) route.completeUrl() else route.completeUrlWithPath(*paths)
        val url = Const.URL_BASE +"/$specUrl$usedParams"
        return dockerCurl(route.method, url, data, *headers)
    }
    fun dockerCurl(method: HttpMethod, url: String, data: Any? = null, vararg headers: Pair<Any, Any>): String = dockerCurlString(
            method, url, if(data != null) getJsonForCurl(data) else null, *headers
    )
    fun getDockerCurl(url: String, data: Any? = null, vararg headers: Pair<Any, Any>): String = dockerCurl(
            HttpMethod.Get, url, data, *headers
    )
    fun postDockerCurl(url: String, data: Any? = null, vararg headers: Pair<Any, Any> = arrayOf("Content-Type" to "application/json")): String = dockerCurl(
            HttpMethod.Post, url, data, *headers
    )

    fun getJsonForCurl(any: Any): String {
        //val map = mapOf("halo" to "ok", "heya" to 1)
        var json = Gson().toJson(any) ?: ""
        var i = -1
        while(++i < json.length) {
            val ch = json[i]
            if(ch == '"') {
                json = json.substring(0, i) +"\\\"" +json.substring(i+1)
                i++
            }
        }
        //println("\"$json\"")
        return "\"$json\""
    }

    fun printRouteCurls(fileName: String, onWrite: (MutableList<String>) -> Unit){
        val file = File("_out/$fileName")
        file.parentFile.mkdirs()
        val pw = PrintWriter(file)
        val container = mutableListOf<String>()
        onWrite(container)
        for(curl in container){
            pw.println(curl)
        }
        pw.flush()
        pw.close()
    }
    //fun postDockerUrl()

    fun simpleRespond(value: Any): Map<String, Any> = mapOf(Const.KEY_MESSAGE to value)
    suspend fun ApplicationCall.simpleRespond(value: Any, status: HttpStatusCode = HttpStatusCode.OK): Unit = respond(status, mapOf("message" to value))
    suspend fun ApplicationCall.simpleBadReqRespond(msg: String = "bad request"): Unit = simpleRespond(msg, HttpStatusCode.BadRequest)
    suspend fun ApplicationCall.simpleUnauthRespond(msg: String = "unauthorized"): Unit = simpleRespond(msg, HttpStatusCode.Unauthorized)
    suspend fun ApplicationCall.simpleOkRespond(msg: String = "ok"): Unit = simpleRespond(msg)
    suspend fun ApplicationCall.simpleForbiddenRespond(msg: String = "forbidden access"): Unit = simpleRespond(msg, HttpStatusCode.Forbidden)
    suspend fun ApplicationCall.simpleInternalErrorRespond(msg: String = "internal error"): Unit = simpleRespond(msg, HttpStatusCode.InternalServerError)
    suspend fun ApplicationCall.simpleConflictRespond(msg: String = "conflicting value"): Unit = simpleRespond(msg, HttpStatusCode.Conflict)
    suspend fun ApplicationCall.simpleNotFoundRespond(msg: String = "not found"): Unit = simpleRespond(msg, HttpStatusCode.NotFound)
}