package sidev.kuliah.pos.uts.app.ecommerce.data.model

data class User(
    val id: Int,
    val name: String,
    val email: String,
)

data class UserDetail(
    val user: User,
    val pswdHash: String,
    val balance: Long,
    //val isActive: Boolean,
    val roleId: Int,
)

/**
 * For sidev.kuliah.pos.uts.app.ecommerce.data holder from form.
 */
data class UserCredential(
    val user: User,
    val pswd: String, //Raw password from form
)