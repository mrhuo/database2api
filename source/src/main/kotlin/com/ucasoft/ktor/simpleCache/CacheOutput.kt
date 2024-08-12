package com.ucasoft.ktor.simpleCache

import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlin.time.Duration

class CacheOutputSelector : RouteSelector() {

    override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Transparent
}

fun Route.cacheOutput(invalidateAt: Duration? = null, queryKeys: List<String> = emptyList(), build: Route.() -> Unit) : Route {
    val route = createChild(CacheOutputSelector())
    route.install(SimpleCachePlugin) {
        this.invalidateAt = invalidateAt
        this.queryKeys = queryKeys
    }
    route.build()
    return route
}