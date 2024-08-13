package com.mrhuo

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.CharsetUtil
import cn.hutool.core.util.RandomUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.db.Entity
import cn.hutool.db.PageResult
import cn.hutool.db.meta.Column
import cn.hutool.db.meta.Table
import cn.hutool.json.JSONUtil
import cn.hutool.log.StaticLog
import cn.hutool.setting.Setting
import java.io.File
import java.time.Duration
import javax.xml.crypto.Data

object Database2Api {
    const val AUTH_TYPE_BASIC = "Basic"
    const val AUTH_TYPE_JWT = "JWT"
    const val AUTH_TYPE_BEARER = "Bearer"

    val DEFAULT_SETTING_FILE_LINES = listOf(
        "## DEFAULT CONFIG FOR database2api",
        "# API 默认端口",
        "API_PORT=8080",
        "# 生成API的前缀，如设置 api/v1 后，则API变为：http://localhost:{PORT}/api/v1/xxxxxx",
        "API_PREFIX=api",
        "# 是否启用 API 文档，地址 http://localhost:{PORT}",
        "API_INDEX_ENABLED=true",
        "# 是否启用接口授权访问功能，默认不启用",
        "API_AUTH_ENABLED=false",
        "# 接口授权访问，支持：Basic, JWT, Bearer。为空不启用",
        "API_AUTH_TYPE=",
        "# 接口允许访问的用户名密码列表，用户名和密码之间英文冒号隔开，多用户英文逗号分隔",
        "API_AUTH_USERS=admin:123456,user:1234",
        "# Bearer 授权时应配置为[tag:token]，tag表示这个token的归属，可为空(冒号不能省略)。",
        "# API_AUTH_USERS=A公司:123,B公司:456,:789",
        "# 数据库默认链接地址",
        "DB_URL=jdbc:mysql://localhost:3306/db?useSSL=false&serverTimezone=UTC&charset=utf8mb4",
        "# 数据库用户名",
        "DB_USER=root",
        "# 数据库密码",
        "DB_PWD=",
        "# 生成API的数据表名称，为空则所有的表都生成API，多个使用英文逗号分割",
        "INCLUDE_TABLES=",
        "# 需要忽略的数据表名称，如果不为空，则指定的表名被过滤，多个使用英文逗号分割",
        "IGNORED_TABLES=",
        "# 是否启用静态网站，启用后，则创建web目录，放入静态资源即可访问",
        "STATIC_WEB_ENABLED=true",
        "# 是否开启扩展API，允许用户使用JS代码使用自定义SQL查询数据库",
        "EXT_API_ENABLED=true",
        "# 是否开启表结构API，默认为false",
        "SCHEMA_API_ENABLED=false",
        "# 是否开启GET类API缓存，默认为true。对表的CREATE,UPDATE,DELETE操作都会使缓存失效",
        "GET_API_CACHE=true",
        "# GET类API缓存时间，默认30s",
        "GET_API_CACHE_TIMEOUT=30000",
    )

    val JWT_SECRET: String = RandomUtil.randomString(32)
    val JWT_ISSUER: String = "Database2Api"
    val JWT_EXPIRED_AT: Long = Duration.ofDays(2).toMillis()

    private val AUTH_TYPES = listOf(AUTH_TYPE_BASIC, AUTH_TYPE_JWT, AUTH_TYPE_BEARER)
    private var mInit = false
    private lateinit var mDataDir: File
    private lateinit var mSettingFile: File
    private lateinit var mDbStructureHelper: DbStructureHelper
    private var mSetting: Setting? = null
    private var mTableNames: List<String> = listOf()
    private var mTables: List<Table> = listOf()
    private var mApiPort: Int = 8080
    private var mApiPrefix: String = "api"
    private var mApiAuthEnabled: Boolean = false
    private var mApiAuthType: String = ""
    private var mApiAuthUsers: List<Pair<String, String>> = listOf()
    private var mEnabledApiIndex: Boolean = true
    private var mEnabledStaticWeb: Boolean = true
    private var mEnabledExtApi: Boolean = true
    private var mEnabledSchemaApi: Boolean = false
    private var mEnabledGetApiCache: Boolean = true
    private var mGetApiCacheTimeout: Long = 30 * 1000L

    /**
     * 初始化
     */
    fun init() {
        if (mInit) {
            StaticLog.info("Database2Api: 已初始化，无需重复初始化")
            return
        }
        StaticLog.info("Database2Api: 开始初始化")
        val root = getRootPath()
        mDataDir = File(root, "data")
        if (!mDataDir.exists()) {
            mDataDir.mkdirs()
            StaticLog.info("Database2Api: 已创建数据目录[${mDataDir.absolutePath}]")
        }
        mSettingFile = File(mDataDir, "setting.ini")
        if (!mSettingFile.exists()) {
            generateDefaultSetting()
            StaticLog.info("Database2Api: 已生成默认配置文件[${mSettingFile.absolutePath}]，如生成错误，请修改配置文件中的数据库连接字符串")
        }
        initApiSetting()
        initDbSetting()
        StaticLog.info("Database2Api: 初始化全部成功")
        mInit = true
    }

    /**
     * 生成默认配置文件
     */
    private fun generateDefaultSetting() {
        FileUtil.appendLines(
            DEFAULT_SETTING_FILE_LINES,
            mSettingFile,
            CharsetUtil.CHARSET_UTF_8
        )
    }

    /**
     * 初始化 API 配置
     */
    private fun initApiSetting() {
        StaticLog.info("Database2Api: 开始初始化 API 配置")
        val setting = getSetting()
        mApiPort = setting.getInt("API_PORT", 8080)
        mApiPrefix = setting.getStr("API_PREFIX", "api")
        mApiAuthEnabled = setting.getBool("API_AUTH_ENABLED", false)
        mApiAuthType = setting.getStr("API_AUTH_TYPE", "")
        mEnabledApiIndex = setting.getBool("API_INDEX_ENABLED", true)
        mEnabledStaticWeb = setting.getBool("STATIC_WEB_ENABLED", true)
        mEnabledExtApi = setting.getBool("EXT_API_ENABLED", true)
        mEnabledSchemaApi = setting.getBool("SCHEMA_API_ENABLED", false)
        mEnabledGetApiCache = setting.getBool("GET_API_CACHE", true)
        mGetApiCacheTimeout = setting.getLong("GET_API_CACHE_TIMEOUT", 30 * 1000L)
        // 是否启用静态网站
        if (mEnabledStaticWeb) {
            val webDir = File(mDataDir, "web")
            if (!webDir.exists()) {
                webDir.mkdirs()
                StaticLog.info("Database2Api: 创建静态网站目录[${webDir.absolutePath}]成功")
            }
            val webIndex = File(webDir, "index.html")
            if (!webIndex.exists()) {
                val indexFileUrl = ResourceUtil.getResource("templates/web-index.html")
                webIndex.writeText(indexFileUrl.readText())
            }
            StaticLog.info("Database2Api: 静态网站主页[http://127.0.0.1:${mApiPort}/web/index.html]")
        } else {
            StaticLog.info("Database2Api: 已禁用静态网站功能")
        }
        // 是否启用 API 授权访问
        if (mApiAuthEnabled && StrUtil.isNotEmpty(mApiAuthType) && AUTH_TYPES.contains(mApiAuthType)) {
            val userListStr = setting.getStr("API_AUTH_USERS", "")
            if (userListStr.isNullOrEmpty()) {
                throw Exception("Database2Api: 您开启了API授权访问，但是没有配置用户名和密码列表[API_AUTH_USERS]")
            }
            StaticLog.info("Database2Api: 已启用API授权访问功能，API授权方式[${mApiAuthType}]")
            val userList: MutableList<Pair<String, String>> = mutableListOf()
            userListStr.split(",").map {
                val userPwdPair = it.split(":")
                val username = userPwdPair[0]
                val userpass = userPwdPair[1]
                if (StrUtil.isNotEmpty(username) && StrUtil.isNotEmpty(userpass)) {
                    userList.add(username to userpass)
                    StaticLog.info("Database2Api: 允许用户[${username}]使用密码[${userpass}]访问")
                } else {
                    StaticLog.warn("Database2Api: 请检查API授权访问用户配置[${it}]是否正确")
                }
            }
            mApiAuthUsers = userList
        } else {
            StaticLog.info("Database2Api: 已禁用API授权访问功能")
        }
        // 是否启用扩展 API
        if (mEnabledExtApi) {
            StaticLog.info("Database2Api: 已启用扩展API功能")
            val extApiDir = File(mDataDir, "ext")
            if (!extApiDir.exists()) {
                extApiDir.mkdirs()
                StaticLog.info("Database2Api: 创建扩展API目录[${extApiDir.absolutePath}]成功")
            }
        } else {
            StaticLog.info("Database2Api: 已禁用扩展API功能")
        }
    }

    /**
     * 初始化数据库配置
     */
    private fun initDbSetting() {
        StaticLog.info("Database2Api: 开始初始化数据库")
        val setting = getSetting()
        val dbUrl = setting.getStr("DB_URL", "")
        val dbUser = setting.getStr("DB_USER", "")
        val dbPwd = setting.getStr("DB_PWD", "")

        StaticLog.info("Database2Api: 使用链接字符串[${dbUrl}]")
        mDbStructureHelper = DbStructureHelper(dbUrl, dbUser, dbPwd)

        // 初始化表名
        this.getTablesNames()

        // 初始化表结构
        this.getAllTable()
    }

    /**
     * 获取配置
     */
    fun getSetting(): Setting {
        if (mSetting == null) {
            mSetting = Setting(mSettingFile.absolutePath, CharsetUtil.CHARSET_UTF_8, true)
        }
        return mSetting!!
    }

    fun getDbStructureHelper(): DbStructureHelper {
        return mDbStructureHelper
    }

    /**
     * 获取数据目录
     */
    fun getDataDir(): File {
        return mDataDir
    }

    /**
     * 获取数据表名
     */
    fun getTablesNames(): List<String> {
        if (mTableNames.isNotEmpty()) {
            return mTableNames
        }
        val tableNamesJsonFile = File(mDataDir, "table_names.json")
        if (tableNamesJsonFile.exists()) {
            mTableNames = JSONUtil.toList(tableNamesJsonFile.readText(), String::class.java)
        } else {
            mTableNames = mDbStructureHelper.getTablesNames()
            tableNamesJsonFile.writeText(JSONUtil.toJsonStr(mTableNames))
            StaticLog.info("Database2Api: 获取到数据表名称[${mTableNames.joinToString()}]，共${mTableNames.size}个")
            StaticLog.info("Database2Api: 已保存到文件[${tableNamesJsonFile.absolutePath}]")
        }
        return mTableNames
    }

    /**
     * 获取所有表结构
     */
    fun getAllTable(): List<Table> {
        if (mTables.isNotEmpty()) {
            return mTables
        }
        val tableStructureJsonFile = File(mDataDir, "tables.json")
        if (tableStructureJsonFile.exists()) {
            val json = tableStructureJsonFile.readText()
            val ja = JSONUtil.parseArray(json)
            val tables: MutableList<Table> = mutableListOf()
            for (i in 0 until ja.size) {
                val jo = ja.getJSONObject(i)
                val table = JSONUtil.toBean(jo, Table::class.java)
                val columns = jo.getJSONArray("columns")
                for (c in 0 until columns.size) {
                    val columnJo = columns.getJSONObject(c)
                    val column = JSONUtil.toBean(columnJo, Column::class.java)
                    column.columnDef = ""
                    table.setColumn(column)
                }
                tables.add(table)
            }
            mTables = tables
        } else {
            val setting = getSetting()
            val includeTables = setting.getStr("INCLUDE_TABLES", "")
            val includeTableNames: MutableList<String> = mutableListOf()
            if (StrUtil.isNotEmpty(includeTables)) {
                includeTableNames.addAll(includeTables.split(","))
            }
            val ignoredTables = setting.getStr("IGNORED_TABLES", "")
            val ignoredTableNames: MutableList<String> = mutableListOf()
            if (StrUtil.isNotEmpty(ignoredTables)) {
                ignoredTableNames.addAll(ignoredTables.split(","))
            }
            mTables = mDbStructureHelper.getTables(includeTableNames, ignoredTableNames)
            mTables.forEach { table ->
                table.columns.forEach { column ->
                    // 解决格式化成JSON后出现的问题
                    column.columnDef = ""
                }
            }
            tableStructureJsonFile.writeText(JSONUtil.toJsonStr(mTables))
            StaticLog.info("Database2Api: 获取到所有数据表的表结构")
            StaticLog.info("Database2Api: 已保存到文件[${tableStructureJsonFile.absolutePath}]")
        }
        return mTables
    }

    /**
     * 分页查询数据
     */
    fun getTableDataPaged(tableName: String, dataModel: QueryDataModel): PageResult<Entity> {
        return mDbStructureHelper.getTableDataPaged(tableName, dataModel)
    }

    /**
     * 返回数据列表
     */
    fun getTableData(tableName: String, query: String? = null): List<Entity> {
        return mDbStructureHelper.getTableData(tableName, query)
    }

    /**
     * 返回数据列表
     */
    fun getTableDataById(tableName: String, idField: String, value: String?): List<Entity> {
        return mDbStructureHelper.getTableDataById(tableName, idField, value)
    }

    /**
     * 获取 API 端口
     */
    fun getApiPort(): Int {
        return mApiPort
    }

    /**
     * 获取 API 前缀
     */
    fun getApiPrefix(): String {
        return mApiPrefix
    }

    /**
     * 是否开启 API 文档
     */
    fun isEnabledApiIndex(): Boolean {
        return mEnabledApiIndex
    }

    /**
     * 是否开启 API 授权访问
     */
    fun isEnabledApiAuth(): Boolean {
        return mApiAuthEnabled
    }

    /**
     * API 授权访问方式，如：Basic,JWT
     */
    fun getApiAuthType(): String {
        return mApiAuthType
    }

    /**
     * API 授权访问的用户列表
     */
    fun getApiAuthUsers(): List<Pair<String, String>> {
        return mApiAuthUsers
    }

    /**
     * 是否开启静态网站
     */
    fun isEnabledStaticWeb(): Boolean {
        return mEnabledStaticWeb
    }

    /**
     * 是否启用扩展 API
     */
    fun isEnabledExtApi(): Boolean {
        return mEnabledExtApi
    }

    /**
     * 是否开启表结构 API
     */
    fun isEnabledSchemaApi(): Boolean {
        return mEnabledSchemaApi
    }

    /**
     * 是否开启 GET API 缓存
     */
    fun isEnabledGetApiCache(): Boolean {
        return mEnabledGetApiCache
    }

    /**
     * GET API 缓存时间
     */
    fun getApiCacheTimeout(): Long {
        return mGetApiCacheTimeout
    }
}

class QueryDataModel {
    var columns: String? = null
    var page: Int? = null
    var limit: Int? = null
    var orderField: String? = null
    var sort: String? = null
    var query: String? = null
}