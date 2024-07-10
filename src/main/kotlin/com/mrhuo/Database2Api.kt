package com.mrhuo

import cn.hutool.core.io.FileUtil
import cn.hutool.core.io.resource.ResourceUtil
import cn.hutool.core.util.CharsetUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.db.Entity
import cn.hutool.db.PageResult
import cn.hutool.db.meta.Column
import cn.hutool.db.meta.Table
import cn.hutool.json.JSONUtil
import cn.hutool.log.StaticLog
import cn.hutool.setting.Setting
import java.io.File

object Database2Api {
    private var mInit = false
    private lateinit var mDataDir: File
    private lateinit var mSettingFile: File
    private lateinit var mDbStructureHelper: DbStructureHelper
    private var mSetting: Setting? = null
    private var mTableNames: List<String> = listOf()
    private var mTables: List<Table> = listOf()
    private var mApiPort: Int = 8080
    private var mApiPrefix: String = "api"
    private var mEnabledApiIndex: Boolean = true
    private var mEnabledStaticWeb: Boolean = true

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
            listOf(
                "## DEFAULT CONFIG FOR database2api",
                "# API 默认端口",
                "API_PORT=8080",
                "# 生成API的前缀，如设置 api/v1 后，则API变为：http://localhost:{PORT}api/v1/xxxxxx",
                "API_PREFIX=api",
                "# 是否启用 API 文档，地址 http://localhost:{PORT}",
                "API_INDEX_ENABLED=true",
                "# 数据库默认链接地址",
                "DB_URL=jdbc:mysql://localhost:3306/db?useSSL=false&serverTimezone=UTC&charset=utf8mb4",
                "# 数据库用户名",
                "DB_USER=root",
                "# 数据库密码",
                "DB_PWD=",
                "# 生成API的数据表名称，为空则所有的表都生成API",
                "INCLUDE_TABLES=",
                "# 是否启用静态网站，启用后，则创建web目录，放入静态资源即可访问",
                "STATIC_WEB_ENABLED=true",
            ),
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
        mEnabledApiIndex = setting.getBool("API_INDEX_ENABLED", true)
        mEnabledStaticWeb = setting.getBool("STATIC_WEB_ENABLED", true)
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
            mTables = mDbStructureHelper.getTables(includeTableNames)
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
    fun getTableData(tableName: String): List<Entity> {
        return mDbStructureHelper.getTableData(tableName)
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
     * 是否开启静态网站
     */
    fun isEnabledStaticWeb(): Boolean {
        return mEnabledStaticWeb
    }
}

class QueryDataModel {
    var columns: String? = null
    var page: Int? = null
    var limit: Int? = null
    var orderField: String? = null
    var sort: String? = null
}