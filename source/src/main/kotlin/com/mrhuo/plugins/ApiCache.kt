package com.mrhuo.plugins

import cn.hutool.log.StaticLog
import com.mrhuo.Database2Api
import com.ucasoft.ktor.simpleCache.SimpleCache
import com.ucasoft.ktor.simpleMemoryCache.memoryCache
import io.ktor.server.application.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

fun Application.configureApiCache() {
    val ms = Database2Api.getApiCacheTimeout()
    if (!Database2Api.isEnabledGetApiCache()) {
        StaticLog.info("Database2Api.configureApiCache: 已禁用API缓存")
        return
    }
    val cacheTimeout = ms.toDuration(DurationUnit.MILLISECONDS)
    install(SimpleCache) {
        memoryCache {
            invalidateAt = cacheTimeout
        }
    }
    StaticLog.info("Database2Api.configureApiCache: 已开启API缓存，缓存时间：${cacheTimeout}")
}