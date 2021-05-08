package sidev.kuliah.pos.uts.app.ecommerce.util

import java.security.MessageDigest

object Const {
    const val KEY_EMAIL = "email"
    const val KEY_PSWD = "password"
    const val KEY_TOKEN = "token"
    const val KEY_AUTH = "Authorization"
    const val KEY_ITEM_ID = "itemId"
    const val KEY_TRANSACTION_ID = "transId"
    const val KEY_USER_ID = "userId"
    const val KEY_COUNT = "count"
    const val KEY_USER = "user"
    const val KEY_TOPUP = "topup"
    const val KEY_STOCK = "stock"
    const val KEY_MESSAGE = "message"
    const val ROLE_SELLER = "Seller"
    const val ROLE_BUYER = "Buyer"
    const val STATUS_ORDER = "Ordered"
    const val STATUS_APPROVE = "Approved"
    const val STATUS_REJECT = "Rejected"
    const val STATUS_PAY = "Paid"
    const val SHA_256 = "SHA-256"
    const val TOKEN_LEN = 20
    const val CONTINUE = 1
    const val SKIP = 0
    const val RETURN = -1
    val DEFAULT_OK_RESPOND = "message" to "ok"

    const val MSG_CONFLICT_TRANS = "there is another incomplete transaction with same item"
    const val MSG_INSUFFICIENT_BALANCE = "insufficient balance"
    const val MSG_INSUFFICIENT_STOCK = "insufficient stock"
    const val MSG_NOT_SELLER_ITEM = "seller does not own the item"
    const val MSG_EMAIL_EXISTS = "email already exists"
    const val MSG_INVALID_EMAIL_PASSWORD = "invalid email or password"
}