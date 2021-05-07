package sidev.kuliah.pos.uts.app.ecommerce.routes

import io.ktor.http.*


object TransactionRoutes: AppRoute {
    override val parent: AppRoute? = null
    override val method: HttpMethod = HttpMethod.Get
    override fun url(): String = "trans"

    object Buy: AppRoute by post("buy", {
        true
    })
    object Approve: AppRoute by post("approve", {
        true
    })
    object Pay: AppRoute by post("pay", {
        true
    })
}