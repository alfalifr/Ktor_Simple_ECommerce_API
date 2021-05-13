package util

import com.google.gson.Gson
import io.ktor.http.*
import org.junit.Test
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Datas
import sidev.kuliah.pos.uts.app.ecommerce.routes.*
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.Dummy
import sidev.kuliah.pos.uts.app.ecommerce.util.TestData
import sidev.kuliah.pos.uts.app.ecommerce.util.Util
import java.io.File
import java.io.PrintWriter
import kotlin.test.assertEquals

class CobTest {
    @Test
    fun gson(){
        val map = mapOf("halo" to "ok", "heya" to 1)
        val json = Util.getJsonForCurl(map)
        println(json)
    }

    @Test
    fun curl(){
        val url = Util.postDockerCurl("http://127.1.0.1:8080/auth/signup", TestData.signupData_buyer)
        val url2 = Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_buyer)
        println(url)
        println(url2)
        assertEquals(url, url2)
    }

    @Test
    fun writeDbConfCurl(){
        Util.printRouteCurls("route_curl_default.txt") {
            it += Util.dockerCurl(DbConfigRoutes.RecreateAll)
            it += Util.dockerCurl(DefaultRoutes)
        }
    }
    @Test
    fun writeAuthCurl(){
        Util.printRouteCurls("route_curl_auth.txt") {
            it += Util.dockerCurl(DbConfigRoutes.RecreateAll)
            it += Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_seller1)
            it += Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_seller2)
            it += Util.dockerCurl(AuthRoutes.Login, params = listOf(*TestData.loginData1).formUrlEncode())
            it += Util.dockerCurl(AuthRoutes.Logout, headers = arrayOf(HttpHeaders.Authorization to "<token>"))
        }
    }
    @Test
    fun writeItemCurl(){
        Util.printRouteCurls("route_curl_item.txt") {
            it += Util.dockerCurl(DbConfigRoutes.RecreateAll)
            it += Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_seller1)
            it += Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_seller2)
            it += Util.dockerCurl(AuthRoutes.Login, params = listOf(*TestData.loginData1).formUrlEncode())
            it += Util.dockerCurl(AuthRoutes.Login, params = listOf(*TestData.loginData2).formUrlEncode())

            it += Util.dockerCurl(ItemRoutes.PostItem, TestData.postItemData1, headers = arrayOf(HttpHeaders.Authorization to "<token>"))
            it += Util.dockerCurl(ItemRoutes.PostItem, TestData.postItemData2, headers = arrayOf(HttpHeaders.Authorization to "<token>"))
            it += Util.dockerCurl(ItemRoutes.DisplayItemByOwner, params = "id=1")
            it += Util.dockerCurl(ItemRoutes.DisplayItemByOwner, params = "id=2")
            it += Util.dockerCurl(ItemRoutes.UpdateStock, TestData.updateStockData1, paths = arrayOf(Const.KEY_ITEM_ID to TestData.updateStockData1), headers = arrayOf(HttpHeaders.Authorization to "<token>"))
            it += Util.dockerCurl(ItemRoutes.UpdateStock, TestData.updateStockData2, paths = arrayOf(Const.KEY_ITEM_ID to TestData.updateStockData2), headers = arrayOf(HttpHeaders.Authorization to "<token>"))
        }
    }
    @Test
    fun writePaymentCurl(){
        Util.printRouteCurls("route_curl_payment.txt") {
            it += Util.dockerCurl(DbConfigRoutes.RecreateAll)
            it += Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_buyer)
            it += Util.dockerCurl(PaymentRoutes.Topup, TestData.topupData2, headers = arrayOf(HttpHeaders.Authorization to Datas.SECRET_PAYMENT))
        }
    }
    @Test
    fun writeTransCurl(){
        Util.printRouteCurls("route_curl_trans.txt") {
            it += Util.dockerCurl(DbConfigRoutes.RecreateAll)
            it += Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_seller1)
            it += Util.dockerCurl(AuthRoutes.SignUp, TestData.signupData_seller2)
            it += Util.dockerCurl(AuthRoutes.Login, params = listOf(*TestData.loginData1).formUrlEncode())
            it += Util.dockerCurl(AuthRoutes.Login, params = listOf(*TestData.loginData2).formUrlEncode())

            it += Util.dockerCurl(ItemRoutes.PostItem, TestData.postItemData1, headers = arrayOf(HttpHeaders.Authorization to "<seller1_token>"))
            it += Util.dockerCurl(ItemRoutes.PostItem, TestData.postItemData2, headers = arrayOf(HttpHeaders.Authorization to "<seller2_token>"))
            it += Util.dockerCurl(ItemRoutes.DisplayItemByOwner, params = "id=1")
            it += Util.dockerCurl(ItemRoutes.DisplayItemByOwner, params = "id=2")
            it += Util.dockerCurl(ItemRoutes.UpdateStock, TestData.updateStockData1, paths = arrayOf(Const.KEY_ITEM_ID to TestData.updateStockData1), headers = arrayOf(HttpHeaders.Authorization to "<token>"))
            it += Util.dockerCurl(ItemRoutes.UpdateStock, TestData.updateStockData2, paths = arrayOf(Const.KEY_ITEM_ID to TestData.updateStockData2), headers = arrayOf(HttpHeaders.Authorization to "<token>"))


            it += Util.dockerCurl(TransactionRoutes.Order, TestData.buyData1_1, headers = arrayOf(HttpHeaders.Authorization to "<buyer_token>"))
            it += Util.dockerCurl(TransactionRoutes.Order, TestData.buyData2_1, headers = arrayOf(HttpHeaders.Authorization to "<buyer_token>"))
            it += Util.dockerCurl(TransactionRoutes.Approve, headers = arrayOf(HttpHeaders.Authorization to "<seller1_token>"))
            it += Util.dockerCurl(TransactionRoutes.Approve, headers = arrayOf(HttpHeaders.Authorization to "<seller2_token>"))

            it += Util.dockerCurl(TransactionRoutes.Approve, headers = arrayOf(HttpHeaders.Authorization to "<seller2_token>"))
        }
    }
}