package sidev.kuliah.pos.uts.app.ecommerce.util

import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemDisplay

object TestData {
    val sellerDetail1 = Dummy.sellerDetail1
    val sellerDetail2 = Dummy.sellerDetail2
    val sellerPswd1 = Dummy.sellerPswd1
    val sellerPswd2 = Dummy.sellerPswd2

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
    val loginData1 = arrayOf(
            "email" to sellerDetail1.user.email,
            "password" to sellerPswd1,
    )
    val loginData2 = arrayOf(
            "email" to sellerDetail2.user.email,
            "password" to sellerPswd2,
    )

    val postItemData1 = Dummy.items1
    val postItemData2 = Dummy.items2

    val itemDisplays1 = Dummy.items1.map {
        ItemDisplay(it.id, it.name, it.price, 0)
    }
    val itemDisplays2 = Dummy.items2.map {
        ItemDisplay(it.id, it.name, it.price, 0)
    }
}