package sidev.kuliah.pos.uts.app.ecommerce.util

import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Item
import sidev.kuliah.pos.uts.app.ecommerce.data.model.User
import sidev.kuliah.pos.uts.app.ecommerce.data.model.UserDetail

object Dummy {
    val seller1 = User(1, "ayu", "a@a.a")
    val seller2 = User(2, "mella", "mel@a.a")
    val buyer = User(3, "mas", "m@a.a")
    val sellerPswd1 = "ereh"
    val sellerPswd2 = "ereho"
    val buyerPswd = "ereh"

    val sellerBalanceInit1 = 1_500L
    val sellerBalanceInit2 = 1_700L

    val sellerDetail1 = UserDetail(
            seller1,
            Util.sha256(sellerPswd1),
            sellerBalanceInit1,
            Datas.ID_SELLER
    )
    val sellerDetail2 = UserDetail(
            seller2,
            Util.sha256(sellerPswd2),
            sellerBalanceInit2,
            Datas.ID_SELLER
    )
    val buyerDetail by lazy {
        UserDetail(
                buyer,
                Util.sha256(buyerPswd),
                buyerBalanceInit,
                Datas.ID_BUYER
        )
    }

    val items1 by lazy {
        listOf(
                Item(1, "Buku", 1_500, seller1.id),
                Item(2, "Nasi", 2_500, seller1.id),
                Item(3, "Bata", 2_700, seller1.id),
        )
    }
    val items2 by lazy {
        listOf(
                Item(4, "Sepatu", 25_400, seller2.id),
                Item(5, "Bebek", 2_760, seller2.id),
        )
    }

    val item1_index2_stock = 10
    val item2_index1_stock = 23

    val item1_index2_buyCount1 = 7
    val item1_index2_buyCount2 = 4

    val item2_index1_buyCount1 = 9
    val item2_index1_buyCount2 = 12

    val totalPriceBuy1_1 = items1[2].price * item1_index2_buyCount1
    val totalPriceBuy2_1 = items2[1].price * item2_index1_buyCount1

    val buyerTotalPriceBuyBalance1_1 = items1[2].price * 9
    val buyerTotalPriceBuyBalance2_1 = items2[1].price * 15

    val buyerBalanceInit = buyerTotalPriceBuyBalance1_1 + buyerTotalPriceBuyBalance2_1
    val buyerBalanceRemain = buyerBalanceInit -
            totalPriceBuy1_1 -
            totalPriceBuy2_1

    val sellerBalance1_1 = sellerBalanceInit1 + totalPriceBuy1_1
    val sellerBalance2_1 = sellerBalanceInit2 + totalPriceBuy2_1


    //private fun getBuyerBalance(): Long = items1.
}