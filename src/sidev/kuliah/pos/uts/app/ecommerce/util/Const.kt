package sidev.kuliah.pos.uts.app.ecommerce.util

import java.security.MessageDigest

object Const {
    const val KEY_EMAIL = "email"
    const val KEY_PSWD = "password"
    const val KEY_TOKEN = "token"
    const val KEY_AUTH = "Authorization"
    const val KEY_ITEM_ID = "itemId"
    const val KEY_USER_ID = "userId"
    const val KEY_USER = "user"
    const val KEY_STOCK = "price"
    const val ROLE_SELLER = "Seller"
    const val ROLE_BUYER = "Buyer"
    const val STATUS_ORDER = "Ordered"
    const val STATUS_APPROVE = "Approved"
    const val STATUS_PAY = "Paid"
    const val SHA_256 = "SHA-256"
    const val TOKEN_LEN = 20
    const val CONTINUE = 1
    const val SKIP = 0
    const val RETURN = -1
    val DEFAULT_OK_RESPOND = "message" to "ok"
}