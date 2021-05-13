package sidev.kuliah.pos.uts.app.ecommerce.routes

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.RoleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.TransactionStatusDao
import sidev.kuliah.pos.uts.app.ecommerce.data.db.*
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleInternalErrorRespond
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.simpleOkRespond

fun Route.dbConfigRoutes() {
    route(DbConfigRoutes) {
        register(DbConfigRoutes.RecreateAll)
    }
}

object DbConfigRoutes : AppRoute {
    override val parent: AppRoute? = null
    override val method: HttpMethod = HttpMethod.Get
    override fun url(): String = "dbconf"

    object RecreateAll: AppRoute by delete("all", {
        try {
            transaction {
                SchemaUtils.drop(
                        Users, Roles, Items, ItemStocks, Sessions, Transactions, TransactionStatuss,
                )
                SchemaUtils.create(
                        Users, Roles, Items, ItemStocks, Sessions, Transactions, TransactionStatuss,
                )
                initDbConfig()
            }
            call.simpleOkRespond()
            true
        } catch (e: Exception) {
            call.simpleInternalErrorRespond(e.toString())
            false
        }
    })
}


fun initDbConfig(){
    RoleDao.batchInsert(*Datas.roles)
    TransactionStatusDao.batchInsert(*Datas.transStatus)
}