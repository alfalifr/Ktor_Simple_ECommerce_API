package sidev.kuliah.pos.uts.app.ecommerce.util

import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Item
import sidev.kuliah.pos.uts.app.ecommerce.data.model.User
import sidev.kuliah.pos.uts.app.ecommerce.data.model.UserDetail

object Dummy {
    val seller = User(1, "ayu", "a@a.a")
    val buyer = User(2, "mas", "m@a.a")
    val sellerPswd = "ereh"
    val buyerPswd = "ereh"

    val sellerDetail = UserDetail(
            seller,
            Util.sha256(sellerPswd),
            1_500,
            Datas.ID_SELLER
    )
    val buyerDetail = UserDetail(
            buyer,
            Util.sha256(buyerPswd),
            123_000,
            Datas.ID_SELLER
    )

    val items = listOf(
            Item(1, "Buku", 1_500, seller.id),
            Item(2, "Nasi", 2_500, seller.id),
            Item(3, "Bata", 2_700, seller.id),
    )
}