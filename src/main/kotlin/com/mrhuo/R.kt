package com.mrhuo

import cn.hutool.core.lang.Dict
import cn.hutool.db.PageResult

data class R(val code: Int, val msg: String, val data: Any? = null) {
    companion object {
        const val ERROR_CODE = 500
        const val OK_CODE = 0
        const val OK_MSG = "OK"

        fun ok(code: Int, msg: String = OK_MSG): R {
            return R(code, msg)
        }

        fun ok(msg: String = OK_MSG): R {
            return ok(OK_CODE, msg)
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

        fun error(code: Int, msg: String): R {
            return R(code, msg)
        }

        fun error(msg: String): R {
            return error(ERROR_CODE, msg)
        }

        fun error(exception: Exception): R {
            return error(ERROR_CODE, exception.message ?: "")
        }
    }
}