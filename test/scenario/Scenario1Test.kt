package sidev.kuliah.pos.uts.app.ecommerce.scenario

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import sidev.kuliah.pos.uts.app.ecommerce.data.model.*
import sidev.kuliah.pos.uts.app.ecommerce.unittesting.AuthRoutesTest
import sidev.kuliah.pos.uts.app.ecommerce.unittesting.ItemRoutesTest
import sidev.kuliah.pos.uts.app.ecommerce.unittesting.TransactionRoutesTest
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.TestData
import sidev.kuliah.pos.uts.app.ecommerce.util.Util
import unittesting.PaymentRoutesTest
import java.time.Instant

private object Data {
    val buyerInitBalance = 1_200L
    val buyerPswd = "abc123"
    val buyerDetail = UserDetail(
            User(
                    2,
                    "ayu",
                    "a@a.a",
            ),
            Util.sha256(buyerPswd),
            buyerInitBalance,
            Datas.ID_BUYER,
    )
    val buyerSignupData = mapOf(
            Const.KEY_NAME to buyerDetail.user.name,
            Const.KEY_EMAIL to buyerDetail.user.email,
            Const.KEY_PSWD to buyerPswd,
            Const.KEY_ROLE_ID to buyerDetail.roleId,
            Const.KEY_BALANCE to buyerDetail.balance,
    )

    val sellerInitBalance = 100L
    val sellerPswd = "def123"
    val sellerDetail = UserDetail(
            User(
                    1,
                    "mella",
                    "m@a.a",
            ),
            Util.sha256(sellerPswd),
            sellerInitBalance,
            Datas.ID_SELLER,
    )
    val sellerSignupData = mapOf(
            Const.KEY_NAME to sellerDetail.user.name,
            Const.KEY_EMAIL to sellerDetail.user.email,
            Const.KEY_PSWD to sellerPswd,
            Const.KEY_ROLE_ID to sellerDetail.roleId,
            Const.KEY_BALANCE to sellerDetail.balance,
    )
/*
    val signupData_seller1 = mapOf(
            "name" to TestData.sellerDetail1.user.name,
            "password" to TestData.sellerPswd1,
            "email" to TestData.sellerDetail1.user.email,
            "roleId" to TestData.sellerDetail1.roleId,
            "balance" to TestData.sellerDetail1.balance,
    )
 */

    val postItems = listOf(
            Item(1, "Baju", 1_700, sellerDetail.user.id),
            Item(2, "Buku", 3_400, sellerDetail.user.id),
            Item(3, "Rumah", 17_200, sellerDetail.user.id),
    )

    val updatedStockId1 = 1
    val updatedStockId2 = 2

    val updateStockData1 = mapOf(Const.KEY_STOCK to 10)
    val updateStockData2 = mapOf(Const.KEY_STOCK to 23)

    val topupData = mapOf(
            Const.KEY_TOPUP to postItems[updatedStockId1].price * 8,
            Const.KEY_USER_ID to buyerDetail.user.id,
    )

    val expectedAfterTopup = buyerInitBalance + topupData[Const.KEY_TOPUP] as Long


    val buyCount1 = 7
    val buyTotalPrice1 = postItems[updatedStockId1-1].price * buyCount1
    val buyData1 = mapOf(
            Const.KEY_ITEM_ID to updatedStockId1,
            Const.KEY_COUNT to buyCount1,
    )

    val expectedTrans1 = Transaction(1, "", updatedStockId1, buyCount1, buyerDetail.user.id, sellerDetail.user.id, Datas.ID_BUY)
    val expectedSellerBalanceFinal = sellerInitBalance + buyTotalPrice1
    val expectedBuyerBalanceFinal = expectedAfterTopup - buyTotalPrice1
}

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class Scenario1SuccessTest {
    companion object {
        @JvmStatic
        private var tokenSeller = ""
        @JvmStatic
        private var tokenBuyer = ""
    }

    @Test
    fun `1_1 seller signup`() = AuthRoutesTest.signupTest(Data.sellerSignupData, Data.sellerDetail)
    @Test
    fun `1_2 seller login`() {
        tokenSeller = AuthRoutesTest.loginTest(Data.sellerDetail, Data.sellerPswd)
        println("tokenSeller= $tokenSeller")
    }

    @Test
    fun `2_1 buyer signup`() = AuthRoutesTest.signupTest(Data.buyerSignupData, Data.buyerDetail, false)
    @Test
    fun `2_2 buyer login`() {
        tokenBuyer = AuthRoutesTest.loginTest(Data.buyerDetail, Data.buyerPswd)
    }

    @Test
    fun `3_1 seller post item`() = ItemRoutesTest.postItemTest(Data.sellerDetail.user, tokenSeller, Data.postItems)
    @Test
    fun `3_2 seller update stock`() = ItemRoutesTest.updateItemStock(Data.updatedStockId1, Data.updateStockData1, tokenSeller)

    @Test
    fun `4_1 buyer order but insufficient balance`() = TransactionRoutesTest.orderBadBusinessLogicTest(Data.buyData1, Const.MSG_INSUFFICIENT_BALANCE, tokenBuyer)
    @Test
    fun `4_2 buyer topup`() = PaymentRoutesTest.topupSuccessTest(Data.topupData, Data.expectedAfterTopup)
    @Test
    fun `4_3 buyer order`() = TransactionRoutesTest.orderSuccessTest(Data.buyData1, Data.expectedTrans1, tokenBuyer)
    @Test
    fun `4_4 seller approve`() = TransactionRoutesTest.approveSuccessTest(Data.expectedTrans1.id, tokenSeller)
    @Test
    fun `4_5 buyer pay`() = TransactionRoutesTest.paySuccessTest(Data.expectedTrans1.id, Data.expectedSellerBalanceFinal, Data.expectedBuyerBalanceFinal, tokenBuyer)
}