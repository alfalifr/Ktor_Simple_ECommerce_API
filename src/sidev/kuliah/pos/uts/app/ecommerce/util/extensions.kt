package sidev.kuliah.pos.uts.app.ecommerce.util

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import kotlinx.css.CSSBuilder
import kotlinx.html.CommonAttributeGroupFacade
import kotlinx.html.FlowOrMetaDataContent
import kotlinx.html.style
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table


data class JsonSampleClass(val hello: String)

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}


fun <T: Comparable<T>> Table.reference(col: Column<T>, name: String? = null): Column<T> = reference(name ?: "${col.table.tableName}_${col.name}", col)