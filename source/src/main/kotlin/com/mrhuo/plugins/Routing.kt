package com.mrhuo.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mrhuo.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.freemarker.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*

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

        if (!Database2Api.isEnabledApiAuth()) {
            database2apiRoute()
            return@routing
        }

        // Basic 认证
        if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_BASIC) {
            authenticate("auth-basic") {
                database2apiRoute()
            }
        }

        // JWT 认证
        if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_JWT) {
            // TODO: JWT 认证提供了一个登录接口
            userLoginRoute()
            authenticate("auth-jwt") {
                database2apiRoute()
            }
        }
    }
}

/**
 * 用户登录接口
 */
private fun Route.userLoginRoute() {
    val prefix = Database2Api.getApiPrefix()
    post("/${prefix}/api-user-login") {
        val user = call.receive<LoginUser>()
        // Check username and password
        if (!Database2Api.getApiAuthUsers().any { user.username == it.first && user.password == it.second }) {
            call.respond(
                R.error("登录失败，用户名或密码错误")
            )
            return@post
        }
        val token = JWT.create()
            .withIssuer(Database2Api.JWT_ISSUER)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + Database2Api.JWT_EXPIRED_AT))
            .sign(Algorithm.HMAC256(Database2Api.JWT_SECRET))
        call.respond(
            R.ok(hashMapOf("token" to token))
        )
    }
}

/**
 * API 路由
 */
private fun Route.database2apiRoute() {
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
            call.respond(
                R.ok(Database2Api.getTableDataPaged(tableName, model))
            )
        }
        get("/${prefix}/${tableName}/all") {
            call.respond(
                R.ok(Database2Api.getTableData(tableName))
            )
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
            call.respond(
                R.ok(Database2Api.getTableDataById(table.tableName, idField, obj))
            )
        }
    }
}

data class LoginUser(val username: String, val password: String)