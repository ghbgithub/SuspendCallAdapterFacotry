package com.hongbo.retrofit.suspend_calladapter

import retrofit2.HttpException
import java.io.Serializable

sealed class HttpResult<out T>(open val code: Int,open val msg:String) : Serializable {
    // discovery

    /**
     * Returns `true` if this instance represents a successful outcome.
     * In this case [isFailure] returns `false`.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns `true` if this instance represents a failed outcome.
     * In this case [isSuccess] returns `false`.
     */
    val isFailure: Boolean get() = this !is Success

    /**
     * Success response with body
     */
    data class Success<T : Any>(val value: T, override val code: Int, override val msg: String = "Success") :
        HttpResult<T>(code, msg) {
        override fun toString(): String {
            return "Success($value)"
        }

        override fun exceptionOrNull(): Throwable? = null
    }

    data class ApiError(override val code: Int = ResultErrorCode.API_ERROR.code, override val msg: String) : HttpResult<Nothing>(code, msg)

    /**
     * For example, json parsing error
     */
    data class UnknownError(
        val throwable: Throwable?,
        override var code: Int = ResultErrorCode.UNKNOWN.code,
        override var msg: String = ResultErrorCode.UNKNOWN.msg
    ) : HttpResult<Nothing>(code, msg) {
        override fun toString(): String {
            super.toString()
            return "UnknownError(throwable:${throwable?.message})"
        }
        override fun exceptionOrNull(): Throwable? = throwable
    }

    data class ConnectTimeOutError(
        override var code: Int = ResultErrorCode.CONNECTION_TIMEOUT.code,
        override var msg: String = ResultErrorCode.CONNECTION_TIMEOUT.msg
    ) : HttpResult<Nothing>(code, msg)

    data class NetworkError(
        val throwable: HttpException,
        override var code: Int = ResultErrorCode.NETWORK_ERROR.code,
        override var msg: String = ResultErrorCode.NETWORK_ERROR.msg
    ) : HttpResult<Nothing>(code, msg){
        init {
            msg = "${msg}(${throwable.code()})"
        }
    }

    data class HandshakeError(
        override var code: Int = ResultErrorCode.HAND_SHAKE_ERROR.code,
        override var msg: String = ResultErrorCode.HAND_SHAKE_ERROR.msg
    ) : HttpResult<Nothing>(code, msg)

    data class PermissionDenyError(
        override var code: Int = ResultErrorCode.PERMISSION_DENIED.code,
        override var msg: String = ResultErrorCode.PERMISSION_DENIED.msg
    ) : HttpResult<Nothing>(code, msg)

    data class ConnectRefuseError(
        override var code: Int = ResultErrorCode.CONNECTION_REFUSED.code,
        override var msg: String = ResultErrorCode.CONNECTION_REFUSED.msg
    ) : HttpResult<Nothing>(code, msg)

    data class NoConnectError(
        override var code: Int = ResultErrorCode.NO_CONNECTION.code,
        override var msg: String = ResultErrorCode.NO_CONNECTION.msg
    ) : HttpResult<Nothing>(code, msg)

    data class JsonParseError(
        val throwable: Throwable,
        override var code: Int = ResultErrorCode.JSON_PARSE_ERROR.code,
        override var msg: String = ResultErrorCode.JSON_PARSE_ERROR.msg
    ) : HttpResult<Nothing>(code, msg){
        init {
            msg = "${msg}(${throwable.message})"
        }
    }

    enum class ResultErrorCode(val code:Int,val msg: String = ""){
        NO_CONNECTION(3000,"网络异常"),
        CONNECTION_TIMEOUT(3001,"连接超时"),
        UNKNOWN(3002,"未知异常"),
        CONNECTION_REFUSED(3003,"服务器连接异常"),
        NETWORK_ERROR(3004,"服务器网络异常"),
        JSON_PARSE_ERROR(3005,"数据解析异常"),
        PERMISSION_DENIED(3006,"需要网络授权"),
        HAND_SHAKE_ERROR(3007,"服务器证书异常"),
        API_ERROR(3008)
    }



    fun getOrNull(): T? = (this as? Success)?.value

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess].
     *
     * This function is a shorthand for `fold(onSuccess = { null }, onFailure = { it })` (see [fold]).
     */
    open fun exceptionOrNull(): Throwable? = null

    companion object {
        fun <T : Any> success(result: T, code: Int,msg: String): HttpResult<T> =
            Success(result, code, msg)

        fun <Nothing> unknownError(
            code: Int = -1,
            message: String? = null,
            throwable: Throwable,
        ): HttpResult<Nothing> =
            UnknownError(throwable,code,message?:"")
    }
}



