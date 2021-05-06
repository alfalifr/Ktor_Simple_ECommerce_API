package sidev.kuliah.pos.uts.app.ecommerce.data.dao

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import sidev.kuliah.pos.uts.app.ecommerce.data.db.Users
import sidev.kuliah.pos.uts.app.ecommerce.data.model.User
import sidev.kuliah.pos.uts.app.ecommerce.data.model.UserDetail

object UserDao: SimpleDao<UserDetail, Users> {
    override val table: Users = Users
    override val tableId: Column<EntityID<Int>>? = Users.id

    override fun Users.generateModel(row: ResultRow): UserDetail = UserDetail(
        User(
            row[id].value,
            row[name],
            row[email],
        ),
        row[pswdHash],
        row[balance],
        row[role].value,
    )

    override fun Users.onInsert(insert: InsertStatement<*>, model: UserDetail) {
        val user = model.user
        insert[name] = user.name
        insert[email] = user.email
        insert[pswdHash] = model.pswdHash
        insert[balance] = model.balance
        insert[role] = model.roleId
    }

    fun readProfile(top: Int = 10): List<User> = transaction {
        val q = Users.selectAll().limit(top)
        val list = mutableListOf<User>()
        q.forEach {
            val user = User(
                it[Users.id].value,
                it[Users.name],
                it[Users.email],
            )
            list += user
        }
        list
    }
    fun readProfileById(id: Int): User? = transaction {
        Users.select { Users.id eq id }.firstOrNull()?.let { row ->
            User(
                row[Users.id].value,
                row[Users.name],
                row[Users.email],
            )
        }
    }

    fun updateBalance(id: Int, balance: Long): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.balance] = balance
        } == 1
    }

    fun getBalance(id: Int): Long = transaction {
        Users.select { Users.id eq id }.first()[Users.balance]
    }
    fun getRole(id: Int): Int = transaction {
        Users.select { Users.id eq id }.first()[Users.role].value
    }

    fun exists(email:String, pswdHash: String): Int = transaction {
        Users.select { Users.email eq email and (Users.pswdHash eq pswdHash) }.firstOrNull()?.get(Users.id)?.value
            ?: -1
    }

    fun existsEmail(email:String): Int = transaction {
        Users.select { Users.email eq email }.firstOrNull()?.get(Users.id)?.value
            ?: -1
    }
}