package sidev.kuliah.pos.uts.app.ecommerce.util

import com.github.javafaker.Faker
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Item
import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemDisplay
import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemStock
import java.security.MessageDigest

object Util {
    val sha256
        get()= MessageDigest.getInstance(Const.SHA_256)
    private val maskChar = arrayOf('?', '#')
    val faker = Faker()

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

    fun simpleRespond(value: Any): Map<String, Any> = mapOf("message" to value)
    suspend fun ApplicationCall.simpleRespond(value: Any, status: HttpStatusCode = HttpStatusCode.OK): Unit = respond(status, mapOf("message" to value))
    suspend fun ApplicationCall.simpleFailRespond(): Unit = simpleRespond("not ok", HttpStatusCode.BadRequest)
    suspend fun ApplicationCall.simpleOkRespond(): Unit = simpleRespond("ok")
}