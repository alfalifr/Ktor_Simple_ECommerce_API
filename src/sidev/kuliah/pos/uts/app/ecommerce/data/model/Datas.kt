package sidev.kuliah.pos.uts.app.ecommerce.data.model

import sidev.kuliah.pos.uts.app.ecommerce.util.Const

object Datas {
    const val ID_SELLER = 1
    const val ID_BUYER = 2

    val roles = arrayOf(
        Role(ID_SELLER, Const.ROLE_SELLER),
        Role(ID_BUYER, Const.ROLE_BUYER),
    )
}