package sidev.kuliah.pos.uts.app.ecommerce.unittesting

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
import sidev.kuliah.pos.uts.app.ecommerce.module
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
            ItemRoutesTest.postItemTest(TestData.sellerDetail1.user, tokenSeller1, TestData.postItemData1)
            ItemRoutesTest.postItemTest(TestData.sellerDetail2.user, tokenSeller2, TestData.postItemData2)
            // Item Stock
            ItemRoutesTest.updateItemStock(TestData.updatedStockId1, TestData.updateStockData1, tokenSeller1)
            ItemRoutesTest.updateItemStock(TestData.updatedStockId2, TestData.updateStockData2, tokenSeller2)
        }

        @JvmStatic
        fun orderSuccessTest(
                buyData: Map<String, Int>,
                expectedTrans: Transaction,
                buyerToken: String,
        ) {
            withTestApplication({ module(testing = true) }) {
                request(TransactionRoutes.Order) {
                    addHeader(HttpHeaders.Authorization, buyerToken)
                    setBody(buyData.toJsonString())
                }.apply {
                    println("response.content = ${response.content}")
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

        @JvmStatic
        fun approveSuccessTest(transId: Int, sellerToken: String) {
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

        @JvmStatic
        fun paySuccessTest(transId: Int, expectedSellerBalance: Long, expectedBuyerBalance: Long, buyerToken: String) {
            withTestApplication({ module(testing = true) }) {
                requestWithPath(TransactionRoutes.Pay, Const.KEY_TRANSACTION_ID to transId) {
                    addHeader(HttpHeaders.Authorization, buyerToken)
                }.apply {
                    println("paySuccessTest() response.content= ${response.content}")
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

        @JvmStatic
        fun orderBadBusinessLogicTest(buyData: Map<String, Int>, expectedMsg: String, buyerToken: String) {
            withTestApplication({ module(testing = true) }) {
                request(TransactionRoutes.Order) {
                    addHeader(HttpHeaders.Authorization, buyerToken)
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

    @Test
    fun _1_1orderSuccessTest() = orderSuccessTest(TestData.buyData1_1, TestData.expectedBuy1_1, tokenBuyer)
    @Test
    fun _1_2orderSuccessTest() = orderSuccessTest(TestData.buyData2_1, TestData.expectedBuy2_1, tokenBuyer)

    @Test
    fun _2_1orderConflictTest() = _2orderConflictTest(TestData.buyData1_3)
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
    fun _3_1approveSuccessTest() = approveSuccessTest(TestData.expectedBuyId1_1, tokenSeller1)
    @Test
    fun _3_3approveSuccessTest() = approveSuccessTest(TestData.expectedBuyId2_1, tokenSeller2)


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
    fun _4_1paySuccessTest() = paySuccessTest(TestData.expectedBuyId1_1, TestData.expectedSellerBalance1_1, TestData.expectedBuyerBalance1, tokenBuyer)
    @Test
    fun _4_2paySuccessTest() = paySuccessTest(TestData.expectedBuyId2_1, TestData.expectedSellerBalance2, TestData.expectedBuyerBalance2, tokenBuyer)


    @Test
    fun _5_1order2SuccessTest() = orderSuccessTest(TestData.buyData1_2, TestData.expectedBuy1_2, tokenBuyer)
    @Test
    fun _5_2approve2SuccessTest() = approveSuccessTest(TestData.expectedBuyId1_2, tokenSeller1)
    @Test
    fun _5_3pay2SuccessTest() = paySuccessTest(TestData.expectedBuyId1_2, TestData.expectedSellerBalance1_2, TestData.expectedBuyerBalance3, tokenBuyer)

    @Test
    fun _6_1orderOutOfStockTest() = orderBadBusinessLogicTest(TestData.buyData1_3, Const.MSG_INSUFFICIENT_STOCK, tokenBuyer)
    @Test
    fun _6_2orderInsufficientBalanceTest() = orderBadBusinessLogicTest(TestData.buyData2_2, Const.MSG_INSUFFICIENT_BALANCE, tokenBuyer)

}