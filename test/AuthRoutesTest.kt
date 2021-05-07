package sidev.kuliah.pos.uts.app.ecommerce

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
    fun _1signupTest() {
        withTestApplication({ module(testing = true, recreateTable = true) }) {
            val call = request(AuthRoutes.SignUp) {
                setBody(TestData.signupData.toJsonString())
            }
            call.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            //Verify
            val user = TestData.user
            val userFromQuery = UserDao.readById(user.user.id)

            assertNotNull(userFromQuery)
            assertEquals(user, userFromQuery)
        }
    }

    @Test
    fun _2loginTest() {
        withTestApplication({ module(testing = true) }) {
            val call = request(AuthRoutes.Login, *TestData.loginData)
            call.apply {
                val _token = response.content

                println("AWAL token= $_token")
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(_token)
                assertEquals(Const.TOKEN_LEN, _token.length)
                assert(_token.isNotBlank())

                token = _token
            }

            //Verify
            val hasLoggedIn = SessionDao.hasLoggedIn(TestData.user.user.id)
            assert(hasLoggedIn)
        }
    }

    @Test
    fun _3logoutTest() {
        println("token= $token")
        withTestApplication({ module(testing = true) }) {
            val call = request(AuthRoutes.Logout, *TestData.loginData) {
                addHeader(HttpHeaders.Authorization, token)
            }
            call.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            //Verify
            val hasLoggedIn = SessionDao.hasLoggedIn(TestData.user.user.id)
            assert(!hasLoggedIn)
        }
    }
}
