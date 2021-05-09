package sidev.kuliah.pos.uts.app.ecommerce.unittesting

import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemDao
import sidev.kuliah.pos.uts.app.ecommerce.data.dao.ItemStockDao
import sidev.kuliah.pos.uts.app.ecommerce.data.model.Item
import sidev.kuliah.pos.uts.app.ecommerce.data.model.ItemDisplay
import sidev.kuliah.pos.uts.app.ecommerce.data.model.User
import sidev.kuliah.pos.uts.app.ecommerce.module
import sidev.kuliah.pos.uts.app.ecommerce.routes.AuthRoutes
import sidev.kuliah.pos.uts.app.ecommerce.routes.ItemRoutes
import sidev.kuliah.pos.uts.app.ecommerce.util.Const
import sidev.kuliah.pos.uts.app.ecommerce.util.TestData
import sidev.kuliah.pos.uts.app.ecommerce.util.TestUtil.request
import sidev.kuliah.pos.uts.app.ecommerce.util.TestUtil.requestWithPath
import sidev.kuliah.pos.uts.app.ecommerce.util.Util
import sidev.kuliah.pos.uts.app.ecommerce.util.Util.toJsonString
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ItemRoutesTest {
    companion object {
        @JvmStatic
        private var tokenSeller1 = ""
        @JvmStatic
        private var tokenSeller2 = ""

        @BeforeClass
        @JvmStatic
        fun loginFirst(){
            withTestApplication({ module(testing = true, recreateTable = true) }) {
                //Seller 1
                request(AuthRoutes.SignUp) {
                    setBody(TestData.signupData_seller1.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                request(AuthRoutes.Login, *TestData.loginData1).apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    tokenSeller1 = response.content!!
                }

                //Seller 2
                request(AuthRoutes.SignUp) {
                    setBody(TestData.signupData_seller2.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                request(AuthRoutes.Login, *TestData.loginData2).apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    tokenSeller2 = response.content!!
                }
            }
        }

        @JvmStatic
        fun postItemTest(seller: User, token: String, items: List<Item>){
            withTestApplication({ module(testing = true) }) {
                val call = request(ItemRoutes.PostItem) {
                    addHeader(HttpHeaders.Authorization, token)
                    setBody(Util.gson.toJson(items))
                }
                call.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }

                //Verify
                val itemFromQuery = ItemDao.readAllByOwner(seller.id)
                assert(items.containsAll(itemFromQuery))
            }
        }

        @JvmStatic
        fun updateItemStock(
                updatedStockId: Int,
                expectedUpdateStockData: Map<String, Int>,
                sellerToken: String,
        ) {
            withTestApplication({ module(testing = true) }) {
                requestWithPath(ItemRoutes.UpdateStock, Const.KEY_ITEM_ID to updatedStockId) {
                    addHeader(HttpHeaders.Authorization, sellerToken)
                    setBody(expectedUpdateStockData.toJsonString())
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }

                //Verify
                val itemStock = ItemStockDao.readByItemId(updatedStockId)

                assertNotNull(itemStock)
                assertEquals(expectedUpdateStockData[Const.KEY_STOCK], itemStock.count)
            }
        }
    }

    @Test
    fun _1_1postItemTest() = postItemTest(TestData.sellerDetail1.user, tokenSeller1, TestData.postItemData1)
    @Test
    fun _1_2postItemTest() = postItemTest(TestData.sellerDetail2.user, tokenSeller2, TestData.postItemData2)


    // User.id is changed cuz it follows the order of signup.
    @Test
    fun _2_1displayByOwner() = _2displayByOwner(TestData.sellerDetail1.user.copy(id = 1), TestData.itemDisplays1)
    @Test
    fun _2_2displayByOwner() = _2displayByOwner(TestData.sellerDetail2.user.copy(id = 2), TestData.itemDisplays2)

    private fun _2displayByOwner(seller: User, expectedItemDisplays: List<ItemDisplay>){
        withTestApplication({ module(testing = true) }) {
            requestWithPath(
                    ItemRoutes.DisplayItemByOwner,
                    Const.KEY_USER_ID to seller.id.toString(),
            ).apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val itemDisplays = parseToItemDisplays(response.content)
                println("expectedItemDisplays = $expectedItemDisplays")

                assertEquals(expectedItemDisplays.size, itemDisplays.size)
                assertEquals(expectedItemDisplays, itemDisplays)
            }
        }
    }

    @Test
    fun _3displayItems() {
        withTestApplication({ module(testing = true) }) {
            request(ItemRoutes.DisplayItem).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val itemDisplays = parseToItemDisplays(response.content)
                assertEquals(
                        TestData.itemDisplays1 + TestData.itemDisplays2,
                        itemDisplays
                )
            }
        }
    }

    private fun parseToItemDisplays(str: String?): List<ItemDisplay> {
        println("resp= $str")

        assertNotNull(str)
        assert(str.startsWith("[") && str.endsWith("]"))

        val itemDisplays = mutableListOf<ItemDisplay>()
        val jsonArray = JsonParser.parseString(str).asJsonArray
        for(e in jsonArray){
            val obj= e.asJsonObject
            val itemDisplay = Util.gson.fromJson(obj, ItemDisplay::class.java)
            itemDisplays += itemDisplay
        }
        println("itemDisplays = $itemDisplays")
        return itemDisplays
    }

    @Test
    fun _4_1updateItemStock() = updateItemStock(TestData.updatedStockId1, TestData.updateStockData1, tokenSeller1)
    @Test
    fun _4_2updateItemStock() = updateItemStock(TestData.updatedStockId2, TestData.updateStockData2, tokenSeller2)
}