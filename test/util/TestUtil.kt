package sidev.kuliah.pos.uts.app.ecommerce.util

import io.ktor.http.*
import io.ktor.server.testing.*
import sidev.kuliah.pos.uts.app.ecommerce.routes.AppRoute

object TestUtil {
    fun TestApplicationEngine.request(
            route: AppRoute,
            vararg params: Pair<String, String>,
            setup: TestApplicationRequest.() -> Unit = {}
    ) = handleRequest(
            route.method,
            if(params.isEmpty()) route.completeUrl()
            else route.completeUrlWithParam(*params),
            setup,
    )
    fun TestApplicationEngine.requestWithPath(
            route: AppRoute,
            vararg paths: Pair<String, String>,
            setup: TestApplicationRequest.() -> Unit = {}
    ) = handleRequest(
            route.method,
            if(paths.isEmpty()) route.completeUrl()
            else route.completeUrlWithPath(*paths),
            setup,
    )
}