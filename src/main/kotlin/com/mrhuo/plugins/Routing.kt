package com.mrhuo.plugins

import com.mrhuo.Database2Api
import com.mrhuo.GITHUB_URL
import com.mrhuo.QueryDataModel
import com.mrhuo.getRootPath
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        // 默认主页
        get("/") {
            if (Database2Api.isEnabledApiIndex()) {
                call.respond(
                    FreeMarkerContent(
                        template = "api-index.ftl",
                        model = mapOf("tables" to Database2Api.getAllTable())
                    )
                )
            } else {
                call.respondText("Welcome use database2api. $GITHUB_URL")
            }
        }

        // 静态网站
        if (Database2Api.isEnabledStaticWeb()) {
            val staticWebFile = File(getRootPath(), "data/web")
            staticFiles("/web", staticWebFile, "index.html")
        }

        val tables = Database2Api.getTablesNames()
        val prefix = Database2Api.getApiPrefix()

        tables.forEach { tableName ->
            post("/${prefix}/${tableName}") {
                call.respondText("新增${tableName}")
            }
            delete("/${prefix}/${tableName}/{id}") {
                val id = call.parameters["id"]
                call.respondText("删除${id}")
            }
            put("/${prefix}/${tableName}") {
                call.respondText("更新${tableName}")
            }
            get("/${prefix}/${tableName}/paged") {
                val columns = call.request.queryParameters["columns"] ?: ""
                val page = call.request.queryParameters["page"]?.toIntOrNull()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull()
                val orderBy = call.request.queryParameters["orderBy"]
                val sort = call.request.queryParameters["sort"]
                val model = QueryDataModel().apply {
                    this.columns = columns
                    this.page = page
                    this.limit = limit
                    this.orderField = orderBy
                    this.sort = sort
                }
                call.respond(Database2Api.getTableDataPaged(tableName, model))
            }
            get("/${prefix}/${tableName}/all") {
                call.respond(Database2Api.getTableData(tableName))
            }
            get("/${prefix}/${tableName}/{id}") {
                val table = Database2Api.getAllTable().first() { it.tableName == tableName }
                if (table.columns.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.InternalServerError, "表[${table.tableName}]没有任何的列，无法按照ID查询")
                    return@get
                }
                // 默认是第一个字段
                var idField = table.columns.first().name
                if (table.pkNames.isNotEmpty()) {
                    idField = table.pkNames.first()
                }
                val obj = call.parameters["id"]
                call.respond(Database2Api.getTableDataById(table.tableName, idField, obj))
            }
        }
    }
}