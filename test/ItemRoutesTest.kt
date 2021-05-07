package sidev.kuliah.pos.uts.app.ecommerce

import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemDao
import sidev.kuliah.pos.uts.app.ecommerce.routes.AuthRoutes
import sidev.kuliah.pos.uts.app.ecommerce.routes.ItemRoutes
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.TestData
import sidev.kuliah.pos.uts.app.ecommerce.util.TestUtil.request
import sidev.kuliah.pos.uts.app.ecommerce.util.Util
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.toJsonString
import kotlin.test.assertEquals

class ItemRoutesTest {
    companion object {
        @JvmStatic
        private var token = ""

        @BeforeClass
        @JvmStatic
        fun loginFirst(){
            withTestApplication({ module(testing = true, recreateTable = true) }) {
                request(AuthRoutes.SignUp) {
                    setBody(TestData.signupData.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                request(AuthRoutes.Login, *TestData.loginData).apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    token = response.content!!
                }
            }
        }
    }

    @Test
    fun _1postItemTest(){
        withTestApplication({ module(testing = true) }) {
            val call = request(ItemRoutes.PostItem) {
                addHeader(HttpHeaders.Authorization, token)
                setBody(Util.gson.toJson(TestData.postItemData))
            }
            call.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            //Verify
            val items = TestData.postItemData
            val itemFromQuery = ItemDao.readAllByOwner(TestData.user.user.id)
            assert(items.containsAll(itemFromQuery))
        }
    }
}