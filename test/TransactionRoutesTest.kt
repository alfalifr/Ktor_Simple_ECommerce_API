package sidev.kuliah.pos.uts.app.ecommerce

import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.TransactionDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.UserDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Transaction
import sidev.kuliah.pos.uts.app.ecommerce.routes.AuthRoutes
import sidev.kuliah.pos.uts.app.ecommerce.routes.TransactionRoutes
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.TestData
import sidev.kuliah.pos.uts.app.ecommerce.util.TestUtil.request
import sidev.kuliah.pos.uts.app.ecommerce.util.TestUtil.requestWithPath
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.toJsonString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class TransactionRoutesTest {
    companion object {
        @JvmStatic
        private var tokenSeller1 = ""
        @JvmStatic
        private var tokenSeller2 = ""
        @JvmStatic
        private var tokenBuyer = ""

        @JvmStatic
        private var currentTransCount = 0

        @BeforeClass
        @JvmStatic
        fun setupData(){
            withTestApplication({ module(testing = true, recreateTable = true) }) {
                //Seller 1
                request(AuthRoutes.SignUp) {
                    setBody(TestData.signupData_seller1.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                request(AuthRoutes.Login, *TestData.loginData1).apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    tokenSeller1 = response.content!!
                }

                //Seller 2
                request(AuthRoutes.SignUp) {
                    setBody(TestData.signupData_seller2.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                request(AuthRoutes.Login, *TestData.loginData2).apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    tokenSeller2 = response.content!!
                }

                //Buyer 1
                request(AuthRoutes.SignUp) {
                    setBody(TestData.signupData_buyer.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                request(AuthRoutes.Login, *TestData.loginDataBuyer).apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    tokenBuyer = response.content!!
                }
            }
            // Items
            ItemRoutesTest._1postItemTest(TestData.sellerDetail1.user, tokenSeller1, TestData.postItemData1)
            ItemRoutesTest._1postItemTest(TestData.sellerDetail2.user, tokenSeller2, TestData.postItemData2)
            // Item Stock
            ItemRoutesTest._4updateItemStock(TestData.updatedStockId1, TestData.updateStockData1, tokenSeller1)
            ItemRoutesTest._4updateItemStock(TestData.updatedStockId2, TestData.updateStockData2, tokenSeller2)
        }
    }

    @Test
    fun _1_1orderSuccessTest() = _1orderSuccessTest(TestData.buyData1_1, TestData.expectedBuy1_1)
    @Test
    fun _1_2orderSuccessTest() = _1orderSuccessTest(TestData.buyData2_1, TestData.expectedBuy2_1)
/*
    @Test
    fun _1_2_1orderTest() = _1orderSuccessTest(TestData.buyData1_2, null, HttpStatusCode.Conflict, Const.MSG_CONFLICT_TRANS)
    @Test
    fun _1_2_2orderTest() = _1orderSuccessTest(TestData.buyData2_2, null, HttpStatusCode.Conflict, Const.MSG_CONFLICT_TRANS)
 */

    fun _1orderSuccessTest(
            buyData: Map<String, Int>,
            expectedTrans: Transaction,
    ) {
        withTestApplication({ module(testing = true) }) {
            request(TransactionRoutes.Order) {
                addHeader(HttpHeaders.Authorization, tokenBuyer)
                setBody(buyData.toJsonString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val resp = response.content
                val transIdStr = JsonParser.parseString(resp).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_TRANSACTION_ID).asString
                assertNotNull(transIdStr)

                val transId = transIdStr.toInt()

                //Verify
                val trans = TransactionDao.readById(transId)
                assertNotNull(trans)
                assertEquals(expectedTrans, trans)
                currentTransCount++
            }
        }
    }

    @Test
    fun _2_1orderConflictTest() = _2orderConflictTest(TestData.buyData1_2)
    @Test
    fun _2_2orderConflictTest() = _2orderConflictTest(TestData.buyData2_2)

    fun _2orderConflictTest(buyData: Map<String, Int>) {
        withTestApplication({ module(testing = true) }) {
            request(TransactionRoutes.Order) {
                addHeader(HttpHeaders.Authorization, tokenBuyer)
                setBody(buyData.toJsonString())
            }.apply {
                assertEquals(HttpStatusCode.Conflict, response.status())

                val resp = response.content
                val msg = JsonParser.parseString(resp).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_MESSAGE).asString
                assertNotNull(msg)
                assertEquals(Const.MSG_CONFLICT_TRANS, msg)

                //Verify
                val trans = TransactionDao.read()
                assertEquals(currentTransCount, trans.size)
                assertEquals(TestData.expectedTransListAfterConflict, trans)
            }
        }
    }

    @Test
    fun _3_1approveSuccessTest() = _3approveSuccessTest(TestData.expectedBuyId1_1, tokenSeller1)
    @Test
    fun _3_3approveSuccessTest() = _3approveSuccessTest(TestData.expectedBuyId2_1, tokenSeller2)

    fun _3approveSuccessTest(transId: Int, sellerToken: String) {
        withTestApplication({ module(testing = true) }) {
            requestWithPath(TransactionRoutes.Approve, Const.KEY_TRANSACTION_ID to transId) {
                addHeader(HttpHeaders.Authorization, sellerToken)
            }.apply {
                println("Approve response.content = ${response.content}")
                assertEquals(HttpStatusCode.OK, response.status())

                //Verify
                val trans = TransactionDao.readById(transId)
                assertNotNull(trans)
                assertEquals(trans.status, Datas.ID_APPROVE)
            }
        }
    }

    @Test
    fun _3_2approveForbiddenTest() {
        withTestApplication({ module(testing = true) }) {
            requestWithPath(TransactionRoutes.Approve, Const.KEY_TRANSACTION_ID to TestData.expectedBuyId2_1) {
                addHeader(HttpHeaders.Authorization, tokenSeller1)
            }.apply {
                println("Approve response.content = ${response.content}")
                assertEquals(HttpStatusCode.Forbidden, response.status())

                val resp = response.content
                assertNotNull(resp)
                val jsonObj = JsonParser.parseString(resp).asJsonObject
                val msg = jsonObj.getAsJsonPrimitive(Const.KEY_MESSAGE).asString

                assertEquals(Const.MSG_NOT_SELLER_ITEM, msg)

                //Verify
                val trans = TransactionDao.readById(TestData.expectedBuyId2_1)
                assertNotNull(trans)
                assertEquals(trans.status, Datas.ID_BUY)
            }
        }
    }

    @Test
    fun _4_1paySuccessTest() = _4paySuccessTest(TestData.expectedBuyId1_1, TestData.expectedSellerBalance1, TestData.expectedBuyerBalance1)
    @Test
    fun _4_2paySuccessTest() = _4paySuccessTest(TestData.expectedBuyId2_1, TestData.expectedSellerBalance2, TestData.expectedBuyerBalance2)

    fun _4paySuccessTest(transId: Int, expectedSellerBalance: Long, expectedBuyerBalance: Long) {
        withTestApplication({ module(testing = true) }) {
            requestWithPath(TransactionRoutes.Pay, Const.KEY_TRANSACTION_ID to transId) {
                addHeader(HttpHeaders.Authorization, tokenBuyer)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())

                //Verify
                val trans = TransactionDao.readById(transId)
                assertNotNull(trans)
                assertEquals(Datas.ID_PAY, trans.status)

                val seller = UserDao.readById(trans.seller)
                val buyer = UserDao.readById(trans.buyer)

                assertNotNull(seller)
                assertNotNull(buyer)
                assertEquals(expectedSellerBalance, seller.balance)
                assertEquals(expectedBuyerBalance, buyer.balance)
            }
        }
    }

    @Test
    fun _5_1orderOutOfStockTest() = _5orderBadBusinessLogicTest(TestData.buyData1_2, Const.MSG_INSUFFICIENT_STOCK)

    @Test
    fun _5_2orderInsufficientBalanceTest() = _5orderBadBusinessLogicTest(TestData.buyData2_2, Const.MSG_INSUFFICIENT_BALANCE)

    fun _5orderBadBusinessLogicTest(buyData: Map<String, Int>, expectedMsg: String) {
        withTestApplication({ module(testing = true) }) {
            request(TransactionRoutes.Order) {
                addHeader(HttpHeaders.Authorization, tokenBuyer)
                setBody(buyData.toJsonString())
            }.apply {
                assertEquals(HttpStatusCode.BadRequest, response.status())

                val resp = response.content
                val msg = JsonParser.parseString(resp).asJsonObject
                        .getAsJsonPrimitive(Const.KEY_MESSAGE).asString
                assertNotNull(msg)
                assertEquals(expectedMsg, msg)

                //Verify
                val trans = TransactionDao.read()
                assertEquals(currentTransCount, trans.size)
                //assertEquals(TestData.expectedTransListAfterConflict, trans)
            }
        }
    }

}