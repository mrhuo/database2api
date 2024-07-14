package com.mrhuo

import cn.hutool.core.util.StrUtil
import cn.hutool.db.Db
import cn.hutool.db.Entity
import cn.hutool.db.Page
import cn.hutool.db.PageResult
import cn.hutool.db.ds.simple.SimpleDataSource
import cn.hutool.db.meta.MetaUtil
import cn.hutool.db.meta.Table
import cn.hutool.db.sql.Direction
import cn.hutool.db.sql.Order
import javax.sql.DataSource

/**
 * 数据库结构助手
 */
class DbStructureHelper(
    private val connectionString: String,
    private val user: String,
    private val password: String
) {
    companion object {
        private var mTableList: List<Table>? = null
    }
    private val dataSource: DataSource

    init {
        dataSource = SimpleDataSource(connectionString, user, password)
    }

    /**
     * 获取所有表名
     */
    fun getTablesNames(): List<String> {
        return MetaUtil.getTables(dataSource)
    }

    /**
     * 获取表结构
     */
    fun getTable(tableName: String): Table {
        return MetaUtil.getTableMeta(dataSource, tableName)
    }

    /**
     * 获取所有表的表结构
     */
    fun getTables(includes: List<String> = listOf(), ignored: List<String> = listOf()): List<Table> {
        if (mTableList != null) {
            return mTableList!!
        }
        var tableNames = getTablesNames()
        if (includes.isNotEmpty()) {
            tableNames = tableNames.filter { includes.contains(it) }
        }
        if (ignored.isNotEmpty()) {
            tableNames = tableNames.filterNot { ignored.contains(it) }
        }
        mTableList = tableNames.map { tableName ->
            getTable(tableName)
        }
        return mTableList!!
    }

    fun getDbInstance(): Db {
        return Db.use(dataSource)
    }

    /**
     * 获取表数据
     */
    fun getTableData(tableName: String): List<Entity> {
        return Db.use(dataSource).find(Entity.create(tableName))
    }

    /**
     * 获取表数据
     */
    fun getTableDataById(tableName: String, idField: String, value: String?): List<Entity> {
        return Db.use(dataSource).find(Entity.create(tableName).set(idField, value))
    }

    /**
     * 获取表数据，分页显示
     */
    fun getTableDataPaged(
        tableName: String,
        dataModel: QueryDataModel
    ): PageResult<Entity> {
        var order: Order? = null
        if (dataModel.orderField != null && dataModel.sort != null) {
            order = Order(
                dataModel.orderField,
                if ("desc".contentEquals(dataModel.sort, true)) Direction.DESC else Direction.ASC
            )
        }
        val _page = dataModel.page ?: 0
        val _limit = dataModel.limit ?: 10
        val entity = Entity.create(tableName)
        if (StrUtil.isNotEmpty(dataModel.columns)) {
            entity.setFieldNames(dataModel.columns!!.split(","))
        }
        return Db.use(dataSource)
            .page(
                entity,
                if (order == null) Page(_page, _limit) else Page(_page, _limit, order),
            )
    }
}