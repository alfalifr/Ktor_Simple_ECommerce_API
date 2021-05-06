package sidev.kuliah.pos.uts.app.ecommerce.data.db

import org.ktorm.database.Database


object DbConfig {
    const val hostname = "127.0.0.1"
    const val port = "3306"
    const val dbName = "mytestdb"
    const val uname = "admin"
    const val pswd = ""
    const val url = "jdbc:mysql://$hostname:$port/$dbName?user=$uname&password=$pswd&useSSL=false"

    fun connectDb(){
        Database.connect(url)
    }
}