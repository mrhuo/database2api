package com.mrhuo.plugins

import cn.hutool.json.JSONUtil
import freemarker.cache.ClassTemplateLoader
import io.ktor.server.application.*
import io.ktor.server.freemarker.*

fun Application.configureFreeMarker() {
    install(FreeMarker) {
        setSharedVariable("Json", FreeMarkerJsonUtil)
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
}

object FreeMarkerJsonUtil {
    /**
     * 转化到 JSON
     */
    @JvmStatic
    fun toJson(obj: Any?): String? {
        return JSONUtil.toJsonStr(obj)
    }
}