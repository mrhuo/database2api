package com.mrhuo

import cn.hutool.core.lang.Dict
import cn.hutool.db.PageResult

data class R(val code: Int, val message: String, val data: Any? = null) {
    companion object {
        const val ERROR_CODE = 500
        const val OK_CODE = 0
        const val OK_MSG = "OK"

        fun ok(code: Int, message: String = OK_MSG): R {
            return R(code, message)
        }

        fun ok(message: String = OK_MSG): R {
            return ok(OK_CODE, message)
        }

        fun ok(data: Any?): R {
            return R(OK_CODE, OK_MSG, data)
        }

        fun ok(pageResult: PageResult<*>): R {
            val dict = Dict.create()
                .set("page", pageResult.page)
                .set("pageSize", pageResult.pageSize)
                .set("totalPage", pageResult.totalPage)
                .set("total", pageResult.total)
                .set("list", pageResult.toList())
            return ok(dict)
        }

        fun error(code: Int, message: String): R {
            return R(code, message)
        }

        fun error(message: String): R {
            return error(ERROR_CODE, message)
        }

        fun error(exception: Exception): R {
            return error(ERROR_CODE, exception.message ?: "")
        }
    }
}