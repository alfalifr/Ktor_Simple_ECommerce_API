package unittesting

import com.google.gson.JsonParser
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.module
import sidev.kuliah.pos.uts.app.ecommerce.routes.AuthRoutes
import sidev.kuliah.pos.uts.app.ecommerce.routes.PaymentRoutes
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.TestData
import sidev.kuliah.pos.uts.app.ecommerce.util.TestUtil.request
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.toJsonString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class PaymentRoutesTest {

    companion object {
        @JvmStatic
        private var currentBalance = 0L

        @BeforeClass
        @JvmStatic
        fun setupAccountFirst(){
            withTestApplication({ module(testing = true, recreateTable = true) }) {
                request(AuthRoutes.SignUp) {
                    setBody(TestData.signupData_buyer.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())

                    //Verify
                    val userFromQuery = UserDao.readById(1)
                    assertNotNull(userFromQuery)
                    assertEquals(TestData.buyerDetail.user.email, userFromQuery.user.email)
                    assertEquals(TestData.buyerDetail.user.name, userFromQuery.user.name)
                    assertEquals(TestData.buyerDetail.pswdHash, userFromQuery.pswdHash)
                    assertEquals(TestData.buyerDetail.roleId, userFromQuery.roleId)
                }
            }
        }

        @JvmStatic
        fun topupSuccessTest(topupData: Map<String, Any>, expectedBalanceAfter: Long){
            withTestApplication({ module(testing = true) }) {
                request(PaymentRoutes.Topup) {
                    addHeader(HttpHeaders.Authorization, Datas.SECRET_PAYMENT)
                    setBody(topupData.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())

                    //Verify
                    val balanceFromQuery = UserDao.getBalance(topupData[Const.KEY_USER_ID] as Int)

                    assert(balanceFromQuery >= 0)
                    assertEquals(expectedBalanceAfter, balanceFromQuery)

                    currentBalance = balanceFromQuery
                }
            }
        }
    }

    @Test
    fun _1topupSuccessTest() = topupSuccessTest(TestData.topupData1, TestData.expectedTopupBalance1)

    @Test
    fun _2topupNegativeTest(){
        withTestApplication({ module(testing = true) }) {
            request(PaymentRoutes.Topup) {
                addHeader(HttpHeaders.Authorization, Datas.SECRET_PAYMENT)
                setBody(TestData.topupData2.toJsonString())
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val resp = response.content
                assertNotNull(resp)
                val msg = JsonParser.parseString(resp).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_MESSAGE).asString

                assertEquals(Const.MSG_POSITIVE_TOPUP, msg)

                //Verify
                val balanceFromQuery = UserDao.getBalance(TestData.topupTarget)
                //Balance is positive but doesn't change
                assert(balanceFromQuery >= 0)
                assertEquals(currentBalance, balanceFromQuery)
            }
        }
    }

    @Test
    fun _3topupUnauthTest(){
        withTestApplication({ module(testing = true) }) {
            request(PaymentRoutes.Topup) {
                setBody(TestData.topupData1.toJsonString())
            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())

                //Verify
                val balanceFromQuery = UserDao.getBalance(TestData.topupTarget)
                //Balance is positive but doesn't change
                assert(balanceFromQuery >= 0)
                assertEquals(currentBalance, balanceFromQuery)
            }
        }
    }

}