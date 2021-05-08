package sidev.kuliah.pos.uts.app.ecommerce.data.model

import sidev.kuliah.pos.uts.app.ecommerce.util.Const

object Datas {
    const val ID_SELLER = 1
    const val ID_BUYER = 2

    const val ID_BUY = 1
    const val ID_APPROVE = 2
    const val ID_REJECT = 3
    const val ID_PAY = 4

    const val SECRET_PAYMENT = "ruhiy58gt97unk30io8u97s2m204nb4hhk5o8kgyk139m1kl4jy"

    val roles = arrayOf(
        Role(ID_SELLER, Const.ROLE_SELLER),
        Role(ID_BUYER, Const.ROLE_BUYER),
    )
    val transStatus = arrayOf(
        TransactionStatus(ID_BUY, Const.STATUS_ORDER),
        TransactionStatus(ID_APPROVE, Const.STATUS_APPROVE),
        TransactionStatus(ID_REJECT, Const.STATUS_REJECT),
        TransactionStatus(ID_PAY, Const.STATUS_PAY),
    )
}