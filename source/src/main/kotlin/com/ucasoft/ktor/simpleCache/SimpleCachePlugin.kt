package com.ucasoft.ktor.simpleCache

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlin.time.Duration

class SimpleCachePluginConfig {

    lateinit var queryKeys: List<String>

    var invalidateAt: Duration? = null
}

val SimpleCachePlugin = createRouteScopedPlugin(name = "SimpleCachePlugin", ::SimpleCachePluginConfig) {
    val provider = application.plugin(SimpleCache).config.provider ?: error("Add one cache provider!")
    val isResponseFromCacheKey = AttributeKey<Boolean>("isResponseFromCacheKey")
    onCall {
        val cache = provider.loadCache(buildKey(it.request, pluginConfig.queryKeys))
        if (cache != null) {
            it.attributes.put(isResponseFromCacheKey, true)
            it.respond(cache)
        }
    }
    onCallRespond { call, body ->
        if ((call.response.status() ?: HttpStatusCode.OK) >= HttpStatusCode.BadRequest) {
            provider.badResponse()
        }
        else if (!call.attributes.contains(isResponseFromCacheKey)) {
            provider.saveCache(buildKey(call.request, pluginConfig.queryKeys), body, pluginConfig.invalidateAt)
        }
    }
}

private fun buildKey(request: ApplicationRequest, queryKeys: List<String>): String {
    val keys =
        if (queryKeys.isEmpty()) request.queryParameters else request.queryParameters.filter { key, _ -> key in queryKeys }
    val key = "${request.path()}?${
        keys.entries().sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value.sorted().joinToString(",")}" }
    }"
    return key.trimEnd('?')
}