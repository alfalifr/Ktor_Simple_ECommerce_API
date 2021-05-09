package sidev.kuliah.pos.uts.app.ecommerce.unittesting

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
import sidev.kuliah.pos.uts.app.ecommerce.data.model.UserDetail
import sidev.kuliah.pos.uts.app.ecommerce.module
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

        @JvmStatic
        fun signupTest(signupData: Map<String, Any>, user: UserDetail, dropFirst: Boolean = true){
            withTestApplication({ module(testing = true, recreateTable = dropFirst) }) {
                request(AuthRoutes.SignUp) {
                    setBody(signupData.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }

                //Verify
                val userFromQuery = UserDao.readById(user.user.id)
                assertNotNull(userFromQuery)

                assertEquals(user.user, userFromQuery.user)
                assertEquals(user.roleId, userFromQuery.roleId)
                assertEquals(user.pswdHash, userFromQuery.pswdHash)
            }
        }

        @JvmStatic
        fun loginTest(user: UserDetail, userPswd: String): String {
            return withTestApplication({ module(testing = true) }) {
                var token: String? = null
                request(
                        AuthRoutes.Login,
                        Const.KEY_EMAIL to user.user.email,
                        Const.KEY_PSWD to userPswd,
                ).apply {
                    kotlin.test.assertEquals(HttpStatusCode.OK, response.status())

                    token = response.content
                    assertNotNull(token)
                    assertEquals(Const.TOKEN_LEN, token!!.length)
                    assert(token!!.isNotBlank())
                }

                //Verify
                val hasLoggedIn = SessionDao.hasLoggedIn(user.user.id)
                assert(hasLoggedIn)
                token!!
            }
        }
    }

    @Test
    fun _1_1signupSuccessTest() = signupTest(TestData.signupData_seller1, TestData.sellerDetail1)

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
        token = loginTest(TestData.sellerDetail1, TestData.loginData1[1].second)
    }
/*
    {
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
 */

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
