package com.mrhuo.plugins

import cn.hutool.core.io.FileUtil
import cn.hutool.json.JSONUtil
import cn.hutool.log.StaticLog
import cn.hutool.script.ScriptUtil
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
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import javax.script.Invocable


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
            scriptApiRoute()
            return@routing
        }

        // Basic 认证
        if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_BASIC) {
            authenticate("auth-basic") {
                database2apiRoute()
                scriptApiRoute()
            }
        }

        // JWT 认证
        if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_JWT) {
            // TODO: JWT 认证提供了一个登录接口
            userLoginRoute()
            authenticate("auth-jwt") {
                database2apiRoute()
                scriptApiRoute()
            }
        }
    }
}

class ExtApiContextRequest(private val call: ApplicationCall) {
    val uri = call.request.uri
    val method = call.request.httpMethod.value
    val headers = call.request.headers.flattenEntries().toMap()
    val query = call.request.queryParameters.flattenEntries().toMap()
    var body: Any = Any()

    init {
        body = runBlocking {
            try {
                when (call.request.headers["Content-Type"]) {
                    "application/x-www-form-urlencoded", "x-www-form-urlencoded", "multipart/form-data" -> {
                        return@runBlocking call.receiveParameters().flattenEntries().toMap<String, String>()
                    }
                    "application/json" -> {
                        val params = call.receiveText()
                        if (JSONUtil.isTypeJSONArray(params)) {
                            return@runBlocking JSONUtil.parseArray(params).toList()
                        }
                        return@runBlocking JSONUtil.parseObj(params).toMap()
                    }
                    else -> {
                        return@runBlocking call.receiveText()
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                return@runBlocking null
            }
        } ?: Any()
    }
}

object ExtApiScriptEngineDbTool {
    fun query(sql: String): Any {
        StaticLog.info("ExtApiScriptEngineDbTool.query: $sql")
        return Database2Api.getDbStructureHelper().getDbInstance().query(sql)
    }

    fun queryOne(sql: String): Any? {
        StaticLog.info("ExtApiScriptEngineDbTool.queryOne: $sql")
        return Database2Api.getDbStructureHelper().getDbInstance().queryOne(sql)
    }

    fun exec(sql: String): Int {
        StaticLog.info("ExtApiScriptEngineDbTool.exec: $sql")
        return Database2Api.getDbStructureHelper().getDbInstance().execute(sql)
    }
}

object ExtApiScriptEngine {
    val extApiDir = File(Database2Api.getDataDir(), "ext")

    /**
     * 执行扩展 API
     */
    fun exec(call: ApplicationCall, jsFileName: String): Any? {
        try {
            val file = File(extApiDir, jsFileName)
            if (!file.exists()) {
                throw Exception("ext api [${jsFileName}] not exists")
            }
            val script = file.readText()
            val engine = ScriptUtil.getJavaScriptEngine()
            engine.put("db", ExtApiScriptEngineDbTool)
            engine.put("context", ExtApiContextRequest(call))
            engine.eval(script)
            val invocable = engine as Invocable?
            return invocable?.invokeFunction("main")
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }
}

private fun Route.scriptApiRoute() {
    if (!Database2Api.isEnabledExtApi()) {
        return
    }
    val prefix = Database2Api.getApiPrefix()
    FileUtil.listFileNames(ExtApiScriptEngine.extApiDir.absolutePath)
        .filter { it.endsWith(".js") }
        .map { it.substring(0, it.length - ".js".length) }
        .forEach { scriptFile ->
            if (scriptFile.startsWith("get_")) {
                val apiName = scriptFile.substring("get_".length)
                if (apiName.isNotEmpty()) {
                    val apiUrl = "/${prefix}/ext/${apiName}"
                    get(apiUrl) {
                        val result = ExtApiScriptEngine.exec(call, "get_${apiName}.js")
                        call.respond(R.ok(result))
                    }
                    StaticLog.info("Database2Api.scriptApiRoute: 创建扩展API[GET:${apiUrl}]成功")
                }
            }
            if (scriptFile.startsWith("post_")) {
                val apiName = scriptFile.substring("post_".length)
                if (apiName.isNotEmpty()) {
                    val apiUrl = "/${prefix}/ext/${apiName}"
                    post(apiUrl) {
                        val result = ExtApiScriptEngine.exec(call, "post_${apiName}.js")
                        call.respond(R.ok(result))
                    }
                    StaticLog.info("Database2Api.scriptApiRoute: 创建扩展API[POST:${apiUrl}]成功")
                }
            }
        }

//    get("/script") {
//        val script = "db.query(\"SELECT *, LENGTH(title) title_len FROM tb_articles\")"
//        val engine = ScriptUtil.getJavaScriptEngine()
//        engine.put("db", DbTool)
//        val result = engine.eval(script)
//        println("result=${result}")
////        // 由ScriptEngines实现的接口，其方法允许调用以前已执行的脚本中的过程。
////        val inv = engine as Invocable
////        // 调用全局函数，并传入参数
////        inv.invokeFunction("def", "测试")
//        call.respond(
//            R.ok(result)
//        )
//    }
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

