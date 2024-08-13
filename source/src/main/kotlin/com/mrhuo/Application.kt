package com.mrhuo

import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.CharsetUtil
import cn.hutool.setting.Setting
import com.mrhuo.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.omg.CORBA.Environment
import java.io.File
import java.util.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (handleArgs(args)) {
        return
    }
    Database2Api.init()
    embeddedServer(
        factory = Netty,
        port = Database2Api.getApiPort(),
        host = "0.0.0.0",
        module = Application::module
    ).start(wait = true)
}

fun handleArgs(args: Array<String>): Boolean {
    if (args.isNotEmpty()) {
        println("[database2api]")
        println("------------------------------------------------")
        println("DataBase to API, use database, generate open API")
        println("https://github.com/mrhuo/database2api")
        println("------------------------------------------------")
        if (args[0] == "--help") {
            println("用法：")
            println("\tjava -jar database2api.jar\t\t\t\t\t启动 database2api")
            println("\tjava -jar database2api.jar --help\t\t\t查看此帮助")
            println("\tjava -jar database2api.jar --gen-setting\t快速生成数据库配置")
            return true
        }
        if (args[0] == "--gen-setting") {
            val root = getRootPath()
            val mDataDir = File(root, "data")
            println("-> 正在生成数据库配置...")
            val settingFile = File(mDataDir, "setting.ini")
            val scanner = Scanner(System.`in`)
            if (settingFile.exists()) {
                println("-> 警告:配置文件 ${settingFile.absolutePath} 已存在，是否删除？(Y/N)")
                while (true) {
                    val line = scanner.nextLine()
                    if (line.isEmpty()) {
                        continue
                    }
                    if ("y".equals(line, ignoreCase = true)) {
                        settingFile.delete()
                        println("-> 警告:配置文件 ${settingFile.absolutePath} 已删除")
                        break
                    } else {
                        exitProcess(-1)
                    }
                }
            }
            FileUtil.appendLines(
                Database2Api.DEFAULT_SETTING_FILE_LINES,
                settingFile,
                CharsetUtil.CHARSET_UTF_8
            )
            val settings = Setting(settingFile.absolutePath, CharsetUtil.CHARSET_UTF_8, true)
            var connectionString = ""
            while (connectionString.isEmpty()) {
                println("-> 请输入数据库连接字符串：")
                connectionString = scanner.nextLine()
            }
            println("-> 请输入数据库用户名：")
            val dbUser = scanner.nextLine()
            println("-> 请输入数据库密码：")
            val dbPass = scanner.nextLine()
            println("-> 生成配置成功，保存路径 ${settingFile.absolutePath}")
            settings.set("DB_URL", connectionString)
            settings.set("DB_USER", dbUser)
            settings.set("DB_PWD", dbPass)
            settings.store()
            return true
        }
    }
    return false
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureFreeMarker()
    configureAuthentication()
    configureApiCache()
    configureRouting()
}
