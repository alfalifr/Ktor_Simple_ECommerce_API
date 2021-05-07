package sidev.kuliah.pos.uts.app.ecommerce.util

object TestData {
    internal var token = ""

    val user = Dummy.sellerDetail
    val userPswd = Dummy.sellerPswd
    val signupData = mapOf(
            "name" to user.user.name,
            "password" to userPswd,
            "email" to user.user.email,
            "roleId" to user.roleId,
            "balance" to user.balance,
    )
    val loginData = arrayOf(
            "email" to user.user.email,
            "password" to userPswd,
    )

    val postItemData = Dummy.items
}