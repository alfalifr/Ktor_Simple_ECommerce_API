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

    val sellerDetail1 = UserDetail(
            seller1,
            Util.sha256(sellerPswd1),
            1_500,
            Datas.ID_SELLER
    )
    val sellerDetail2 = UserDetail(
            seller2,
            Util.sha256(sellerPswd2),
            1_700,
            Datas.ID_SELLER
    )
    val buyerDetail = UserDetail(
            buyer,
            Util.sha256(buyerPswd),
            123_000,
            Datas.ID_SELLER
    )

    val items1 = listOf(
            Item(1, "Buku", 1_500, seller1.id),
            Item(2, "Nasi", 2_500, seller1.id),
            Item(3, "Bata", 2_700, seller1.id),
    )
    val items2 = listOf(
            Item(4, "Sepatu", 25_400, seller2.id),
            Item(5, "Bebek", 2_760, seller2.id),
    )
}