package sidev.kuliah.pos.uts.app.ecommerce.routes

import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemStockDao
import sidev.kuliah.pos.uts.app.ecommerce.routes.get
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleRespond
import java.lang.IllegalStateException

object AppRoutes {
}

interface Child<T> {
    val parent: T?
}

interface AppRoute: Child<AppRoute> {
    override val parent: AppRoute?
    val method: HttpMethod
    fun completeUrl(): String = if(parent != null) "${parent!!.completeUrl()}/${url()}" else url()
    fun completeUrlWithParam(vararg pairs: Pair<String, String>): String =
            "${completeUrl()}?${pairs.asList().formUrlEncode()}"
    fun completeUrlWithPath(vararg pairs: Pair<String, Any>): String {
        var res = completeUrl()
        for((key, value) in pairs){
            res = res.replaceFirst("{$key}", value.toString())
        }
        return res
    }
    fun url(): String
    suspend fun doOp(pipeline: PipelineContext<Unit, ApplicationCall>): Boolean = true
}

fun AppRoute.routeOf(
        url: String,
        method: HttpMethod = HttpMethod.Get,
        op: suspend (PipelineContext<Unit, ApplicationCall>.() -> Boolean) = { true }
): AppRoute = object: AppRoute {
    private fun PipelineContext<Unit, ApplicationCall>.getDefaultExcHandler() = Thread.UncaughtExceptionHandler { t, e ->
        GlobalScope.launch {
            call.respondText("UNCAUGHT ERROR: $e")
            throw e
        }
    }
    override val parent: AppRoute? = this@routeOf
    override val method: HttpMethod = method
    override fun url(): String = url
    override suspend fun doOp(pipeline: PipelineContext<Unit, ApplicationCall>): Boolean {
        Thread.setDefaultUncaughtExceptionHandler(pipeline.getDefaultExcHandler())
        return op(pipeline)
    }
}

fun AppRoute.get(
        url: String,
        op: suspend (PipelineContext<Unit, ApplicationCall>.() -> Boolean) = { true }
): AppRoute = routeOf(url, HttpMethod.Get, op)

fun AppRoute.post(
        url: String,
        op: suspend (PipelineContext<Unit, ApplicationCall>.() -> Boolean) = { true }
): AppRoute = routeOf(url, HttpMethod.Post, op)

fun AppRoute.delete(
        url: String,
        op: suspend (PipelineContext<Unit, ApplicationCall>.() -> Boolean) = { true }
): AppRoute = routeOf(url, HttpMethod.Delete, op)

fun AppRoute.put(
        url: String,
        op: suspend (PipelineContext<Unit, ApplicationCall>.() -> Boolean) = { true }
): AppRoute = routeOf(url, HttpMethod.Put, op)



fun Route.route(route: AppRoute, build: Route.() -> Unit){
    route(route.completeUrl(), build)
}
fun Route.get(route: AppRoute){
    get(route.url()){
        route.doOp(this)
    }
}
fun Route.post(route: AppRoute){
    post(route.url()){
        route.doOp(this)
    }
}
fun Route.delete(route: AppRoute){
    delete(route.url()){
        route.doOp(this)
    }
}
fun Route.put(route: AppRoute){
    put(route.url()){
        route.doOp(this)
    }
}
fun Route.patch(route: AppRoute){
    patch(route.url()){
        route.doOp(this)
    }
}
fun Route.head(route: AppRoute){
    head(route.url()){
        route.doOp(this)
    }
}
fun Route.options(route: AppRoute){
    options(route.url()){
        route.doOp(this)
    }
}

fun Route.register(route: AppRoute) = when(val method = route.method) {
    HttpMethod.Get -> get(route)
    HttpMethod.Post -> post(route)
    HttpMethod.Delete -> delete(route)
    HttpMethod.Put -> put(route)
    HttpMethod.Patch -> patch(route)
    HttpMethod.Head -> head(route)
    HttpMethod.Options -> options(route)
    else -> throw IllegalStateException("Unknown HttpMethod ($method)")
}