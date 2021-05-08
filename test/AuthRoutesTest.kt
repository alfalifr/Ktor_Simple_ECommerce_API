package sidev.kuliah.pos.uts.app.ecommerce

import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Test
import org.junit.runners.MethodSorters
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.SessionDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.routes.AuthRoutes
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.TestData
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.toJsonString
import sidev.kuliah.pos.uts.app.ecommerce.util.TestUtil.request
import kotlin.test.assertNotNull

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class AuthRoutesTest {

    companion object {
        @JvmStatic
        private var token = ""
    }

    @Test
    fun _1_1signupSuccessTest() {
        withTestApplication({ module(testing = true, recreateTable = true) }) {
            val call = request(AuthRoutes.SignUp) {
                setBody(TestData.signupData_seller1.toJsonString())
            }
            call.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            //Verify
            val user = TestData.sellerDetail1
            val userFromQuery = UserDao.readById(user.user.id)

            assertNotNull(userFromQuery)
            assertEquals(user, userFromQuery)
        }
    }
    @Test
    fun _1_2signupConflictTest() {
        withTestApplication({ module(testing = true) }) {
            val call = request(AuthRoutes.SignUp) {
                setBody(TestData.signupData_seller1_2.toJsonString())
            }
            call.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())

                val resp = response.content
                val msg = JsonParser.parseString(resp).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_MESSAGE).asString
                assertNotNull(msg)
                assertEquals(Const.MSG_EMAIL_EXISTS, msg)
            }
        }
    }

    @Test
    fun _2_1loginFailTest() {
        withTestApplication({ module(testing = true) }) {
            val call = request(AuthRoutes.Login, *TestData.loginData1_2)
            call.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())

                val resp = response.content
                val msg = JsonParser.parseString(resp).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_MESSAGE).asString
                assertNotNull(msg)
                assertEquals(Const.MSG_INVALID_EMAIL_PASSWORD, msg)
            }

            //Verify
            val sessions = SessionDao.read()
            assert(sessions.isEmpty())
        }
    }

    @Test
    fun _2_2loginSuccessTest() {
        withTestApplication({ module(testing = true) }) {
            val call = request(AuthRoutes.Login, *TestData.loginData1)
            call.apply {
                val _token = response.content

                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(_token)
                assertEquals(Const.TOKEN_LEN, _token.length)
                assert(_token.isNotBlank())

                token = _token
            }

            //Verify
            val hasLoggedIn = SessionDao.hasLoggedIn(TestData.sellerDetail1.user.id)
            assert(hasLoggedIn)
        }
    }

    @Test
    fun _3logoutTest() {
        withTestApplication({ module(testing = true) }) {
            val call = request(AuthRoutes.Logout, *TestData.loginData1) {
                addHeader(HttpHeaders.Authorization, token)
            }
            call.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            //Verify
            val hasLoggedIn = SessionDao.hasLoggedIn(TestData.sellerDetail1.user.id)
            assert(!hasLoggedIn)
        }
    }
}
