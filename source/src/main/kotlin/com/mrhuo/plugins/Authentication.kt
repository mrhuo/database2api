package com.mrhuo.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.mrhuo.Database2Api
import com.mrhuo.R
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuthentication() {
    if (!Database2Api.isEnabledApiAuth()) {
        return
    }
    install(Authentication) {
        val prefix = Database2Api.getApiPrefix()

        // Basic 授权
        if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_BASIC) {
            basic("auth-basic") {
                realm = "Access to the '/${prefix}' path"
                validate { credentials ->
                    if (verifyCredentials(credentials)) {
                        UserIdPrincipal(credentials.name)
                    } else {
                        null
                    }
                }
            }
        }

        //JWT 授权
        if (Database2Api.getApiAuthType() == Database2Api.AUTH_TYPE_JWT) {
            jwt("auth-jwt") {
                realm = "Access to the '/${prefix}' path"
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
}

/**
 * 验证系统中的用户名密码
 */
private fun verifyCredentials(credentials: UserPasswordCredential): Boolean {
    return Database2Api.getApiAuthUsers().any {
        credentials.name == it.first && credentials.password == it.second
    }
}

