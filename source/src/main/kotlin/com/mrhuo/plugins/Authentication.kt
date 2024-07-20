package com.mrhuo.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mrhuo.Database2Api
import com.mrhuo.R
import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking

fun Application.configureAuthentication() {
    if (!Database2Api.isEnabledApiAuth()) {
        return
    }
    install(Authentication) {
        val prefix = Database2Api.getApiPrefix()

        // Basic 授权
        installBasicAuthentication(prefix)

        // JWT 授权
        installJWTAuthentication(prefix)

        // Bearer 授权
        installBearerAuthentication(prefix)
    }
}

/**
 * 安装 Basic 授权
 */
private fun AuthenticationConfig.installBasicAuthentication(apiPrefix: String) {
    if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_BASIC) {
        basic("auth-basic") {
            realm = "Access to the '/${apiPrefix}' path"
            validate { credentials ->
                if (verifyCredentials(credentials)) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
}

/**
 * 安装 JWT 授权
 */
private fun AuthenticationConfig.installJWTAuthentication(apiPrefix: String) {
    if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_JWT) {
        jwt("auth-jwt") {
            realm = "Access to the '/${apiPrefix}' path"
            verifier(
                JWT
                    .require(Algorithm.HMAC256(Database2Api.JWT_SECRET))
                    .withIssuer(Database2Api.JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                val allowUserNames = Database2Api.getApiAuthUsers().map { it.first }
                if (allowUserNames.contains(credential.payload.getClaim("username").asString())) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    R.error("Token is not valid or has expired")
                )
            }
        }
    }
}

/**
 * 安装 Bearer 授权
 */
private fun AuthenticationConfig.installBearerAuthentication(apiPrefix: String) {
    if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_BEARER) {
        bearer("auth-bearer") {
            realm = "Access to the '/${apiPrefix}' path"
            authHeader { call ->
                val header = call.request.headers
                if (!header.contains("Authorization")) {
                    runBlocking {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            R.error("Token is missing")
                        )
                    }
                    return@authHeader null
                }
                val realm = header["Authorization"]?.split("Bearer ")?.get(1) ?: ""
                return@authHeader HttpAuthHeader.bearerAuthChallenge(realm, realm)
            }
            authenticate { tokenCredential ->
                val allowTokens = Database2Api.getApiAuthUsers().map { it.second }
                if (allowTokens.contains(tokenCredential.token)) {
                    UserIdPrincipal(tokenCredential.token)
                } else {
                    null
                }
            }
        }
    }
}

/**
 * 验证系统中的用户名密码
 */
private fun verifyCredentials(credentials: UserPasswordCredential): Boolean {
    return Database2Api.getApiAuthUsers().any {
        credentials.name == it.first && credentials.password == it.second
    }
}

