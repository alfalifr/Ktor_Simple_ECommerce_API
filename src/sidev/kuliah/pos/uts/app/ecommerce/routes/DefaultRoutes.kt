package sidev.kuliah.pos.uts.app.ecommerce.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*

fun Route.defaultRoutes(){
    register(DefaultRoutes)
}

object DefaultRoutes: AppRoute {
    override val parent: AppRoute? = null
    override val method: HttpMethod = HttpMethod.Get
    override fun url(): String = "/"
    override suspend fun doOp(pipeline: PipelineContext<Unit, ApplicationCall>): Boolean = pipeline.run {
        call.respondText("Halo bro!")
        true
    }
}