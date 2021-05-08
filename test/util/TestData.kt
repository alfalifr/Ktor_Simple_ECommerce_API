package sidev.kuliah.pos.uts.app.ecommerce.util

import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemDisplay
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Transaction

object TestData {
    val sellerDetail1 = Dummy.sellerDetail1
    val sellerDetail2 = Dummy.sellerDetail2
    val buyerDetail = Dummy.buyerDetail
    val sellerPswd1 = Dummy.sellerPswd1
    val sellerPswd2 = Dummy.sellerPswd2
    val buyerPswd = Dummy.buyerPswd

    val signupData_seller1 = mapOf(
            "name" to sellerDetail1.user.name,
            "password" to sellerPswd1,
            "email" to sellerDetail1.user.email,
            "roleId" to sellerDetail1.roleId,
            "balance" to sellerDetail1.balance,
    )
    val signupData_seller2 = mapOf(
            "name" to sellerDetail2.user.name,
            "password" to sellerPswd2,
            "email" to sellerDetail2.user.email,
            "roleId" to sellerDetail2.roleId,
            "balance" to sellerDetail2.balance,
    )
    val signupData_buyer = mapOf(
            "name" to buyerDetail.user.name,
            "password" to buyerPswd,
            "email" to buyerDetail.user.email,
            "roleId" to buyerDetail.roleId,
            "balance" to buyerDetail.balance,
    )
    val loginData1 = arrayOf(
            "email" to sellerDetail1.user.email,
            "password" to sellerPswd1,
    )
    val loginData2 = arrayOf(
            "email" to sellerDetail2.user.email,
            "password" to sellerPswd2,
    )
    val loginDataBuyer = arrayOf(
            "email" to buyerDetail.user.email,
            "password" to buyerPswd,
    )

    val postItemData1 = Dummy.items1
    val postItemData2 = Dummy.items2

    val itemDisplays1 = Dummy.items1.map {
        ItemDisplay(it.id, it.name, it.price, 0)
    }
    val itemDisplays2 = Dummy.items2.map {
        ItemDisplay(it.id, it.name, it.price, 0)
    }

    val updatedStockId1 = itemDisplays1[2].id
    val updatedStockId2 = itemDisplays2[1].id

    val updateStockData1 = mapOf(Const.KEY_STOCK to Dummy.item1_index2_stock)
    val updateStockData2 = mapOf(Const.KEY_STOCK to Dummy.item2_index1_stock)

    val buyData1_1 = mapOf(
            Const.KEY_ITEM_ID to updatedStockId1,
            Const.KEY_COUNT to Dummy.item1_index2_buyCount1,
    )
    val buyData1_2 = mapOf(
            Const.KEY_ITEM_ID to updatedStockId1,
            Const.KEY_COUNT to Dummy.item1_index2_buyCount2,
    )
    val buyData2_1 = mapOf(
            Const.KEY_ITEM_ID to updatedStockId2,
            Const.KEY_COUNT to Dummy.item2_index1_buyCount1,
    )
    val buyData2_2 = mapOf(
            Const.KEY_ITEM_ID to updatedStockId2,
            Const.KEY_COUNT to Dummy.item2_index1_buyCount2,
    )

    val expectedBuyId1_1 = 1
    val expectedBuyId1_2 = 3
    val expectedBuyId2_1 = 2
    val expectedBuyId2_2 = 4

    val expectedBuy1_1 = Transaction(
            expectedBuyId1_1,
            "",
            buyData1_1[Const.KEY_ITEM_ID]!!,
            buyData1_1[Const.KEY_COUNT]!!,
            buyerDetail.user.id,
            sellerDetail1.user.id,
            Datas.ID_BUY,
    )
    val expectedBuy1_2 = Transaction(
            expectedBuyId1_2,
            "",
            buyData1_2[Const.KEY_ITEM_ID]!!,
            buyData1_2[Const.KEY_COUNT]!!,
            buyerDetail.user.id,
            sellerDetail1.user.id,
            Datas.ID_BUY,
    )
    val expectedBuy2_1 = Transaction(
            expectedBuyId2_1,
            "",
            buyData2_1[Const.KEY_ITEM_ID] as Int,
            buyData2_1[Const.KEY_COUNT] as Int,
            buyerDetail.user.id,
            sellerDetail2.user.id,
            Datas.ID_BUY,
    )
    val expectedBuy2_2 = Transaction(
            expectedBuyId2_2,
            "",
            buyData2_2[Const.KEY_ITEM_ID] as Int,
            buyData2_2[Const.KEY_COUNT] as Int,
            buyerDetail.user.id,
            sellerDetail2.user.id,
            Datas.ID_BUY,
    )

    val expectedTransListAfterConflict = listOf(
            expectedBuy1_1, expectedBuy2_1
    )

    val expectedFinalBalance = Dummy.buyerBalanceRemain
}