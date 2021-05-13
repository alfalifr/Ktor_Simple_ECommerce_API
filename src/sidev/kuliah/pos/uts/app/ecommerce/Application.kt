package sidev.kuliah.pos.uts.app.ecommerce

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
//import org.jetbrains.exposed.sql.SchemaUtils
import org.slf4j.event.Level
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.RoleDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.TransactionStatusDao
import sidev.kuliah.pos.uts.app.ecommerce.data.db.*
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.routes.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false, recreateTable: Boolean = false) {

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        json()
    }

    registerDefaultRoutes()
    initDb(recreateTable)
    //if(recreateTable)
    initDbConfig()
    registerRoutes()

/*
    val client = HttpClient() {
        install(Auth) {
        }
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }
    runBlocking {
        // Sample for making a HTTP Client request
        /*
        val message = client.post<JsonSampleClass> {
            url("http://127.0.0.1:8080/path/to/endpoint")
            contentType(ContentType.Application.Json)
            body = JsonSampleClass(hello = "world")
        }
        */
    }
// */
}


fun initDb(dropFirst: Boolean = false){
    //val url = "jdbc:mysql://admin@127.0.0.1:3306/mytestdb" //?useUnicode=true&serverTimezone=UTC"
    val url = "jdbc:mysql://mysql1:3306/mytestdb" //?useUnicode=true&serverTimezone=UTC"
    //val url = "jdbc:mysql://127.0.0.1:3306/mytestdb" //?useUnicode=true&serverTimezone=UTC"
    //val url = "jdbc:mysql://root:web@localhost:3306/mytestdb?useUnicode=true&serverTimezone=UTC"
    val driver = "com.mysql.cj.jdbc.Driver"
    val pswd = "abc123"
    val user = "admin"
    val host = "127.0.0.1"
    //val pswd = ""
    //val driver = "com.mysql.jdbc.Driver"
    Database.connect(url, driver, password = pswd, user = user)
    //com.mysql.jdbc.Driver
    //Database.connect("jdbc:mysql://127.0.0.1:3306/mytestdb", driver = driver, user = "admin")

    //DbConfig.connectDb()

    transaction {
        if(dropFirst){
            SchemaUtils.drop(
                Users, Roles, Items, ItemStocks, Sessions, Transactions, TransactionStatuss,
            )
        }
        SchemaUtils.create(
            Users, Roles, Items, ItemStocks, Sessions, Transactions, TransactionStatuss,
        )
    }
}

fun Application.registerRoutes(){
    routing {
        authRoutes()
        itemRoutes()
        transactionRoutes()
        paymentRoutes()
        dbConfigRoutes()
    }
}

fun Application.registerDefaultRoutes(){
    routing {
        defaultRoutes()
    }
}