package com.mrhuo

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.CharsetUtil
import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONUtil
import cn.hutool.log.StaticLog
import cn.hutool.setting.Setting
import com.mrhuo.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

/**
 * TODO:
 *  1.接收命令行参数，生成表结构
 *  2.为每个表生成API
 *  3.实现自定义静态网页托管
 */
fun main() {
    Database2Api.init()
    embeddedServer(
        factory = Netty,
        port = Database2Api.getApiPort(),
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureFreeMarker()
    configureRouting()
}
