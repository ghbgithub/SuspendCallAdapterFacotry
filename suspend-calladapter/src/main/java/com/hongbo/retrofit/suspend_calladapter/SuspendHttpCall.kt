package com.hongbo.retrofit.suspend_calladapter

import android.util.Log
import okhttp3.Request
import okio.Timeout
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException
import javax.net.ssl.SSLHandshakeException

/**
 * retrofit协程函数返回结果处理。主要是处理异常情况，避免原有的throw error。
 */
internal class SuspendHttpCall<T>(
    private val delegate: Call<T>,
    private val apiSuccessCheckCall:(res:Any)->Pair<Boolean,String> = {Pair(false,"")}
) : Call<HttpResult<T>> {

    override fun enqueue(callback: Callback<HttpResult<T>>) {
        //自定义响应处理逻辑
        return delegate.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val body = response.body()
                var httpResult: HttpResult<T>? = null
                //===================1、响应成功===================
                if (response.isSuccessful) {
                    body?.apply {
                        //Repo.data不为空
//                        val resJson =  Gson().toJson(this)
                        //使用外面传入的api成功检测逻辑
                        val apiCheckResult = apiSuccessCheckCall(body)
                        if (apiCheckResult.first){
                            httpResult = HttpResult.Success(this, SUCCESS)
                        }else{
                            httpResult = HttpResult.ApiError(msg = apiCheckResult.second)
                        }
                    } ?: run {
                        //响应body是null或者Repo的data为空的时候
                        httpResult = generateFailureHttpResult(IllegalArgumentException("response data is invalid"))
                    }
                    callback.onResponse(
                        this@SuspendHttpCall,
                        Response.success(httpResult)
                    )
                    return
                }
                //===================2、响应失败===================
                onFailure(call, HttpException(response))
            }

            override fun onFailure(call: Call<T>, throwable: Throwable) {
                val result: HttpResult<T> = generateFailureHttpResult(throwable)
                callback.onResponse(this@SuspendHttpCall, Response.success(result))
            }
        })
    }

    override fun isExecuted() = delegate.isExecuted

    override fun clone(): Call<HttpResult<T>> = SuspendHttpCall(
        delegate.clone(),
    )

    override fun isCanceled() = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    override fun execute(): Response<HttpResult<T>> {
        throw UnsupportedOperationException("NetworkResponseCall doesn't support execute")
    }

    override fun request(): Request = delegate.request()


    /**
     * 生成一个接口请求失败，或者中间过程(解析数据)异常的情况的返回体
     */
    fun generateFailureHttpResult(
        throwable: Throwable
    ): HttpResult<Nothing> {
        Log.e(SuspendHttpCall::class.java.simpleName,throwable.message ?: "")
        val httpResult = HttpResult.NoConnectError()
        return when (throwable) {
            is HttpException -> {
                HttpResult.NetworkError(throwable)
            }
            is SocketTimeoutException -> {
                HttpResult.ConnectTimeOutError()
            }
            is SSLHandshakeException -> {
                HttpResult.HandshakeError()
            }
            is JSONException, is ParseException -> {
                HttpResult.JsonParseError(throwable)
            }
            is UnknownHostException, is ConnectException, is SocketException -> {
                val causeMessage = throwable.message
                if (causeMessage != null && causeMessage.contains("Permission denied")) {
                    HttpResult.PermissionDenyError()
                } else if (causeMessage != null && causeMessage.contains("Connection refused")) {
                    HttpResult.ConnectRefuseError()
                } else {
                    HttpResult.NoConnectError()
                }
            }
            else -> {
                HttpResult.UnknownError(throwable)
            }
        }
    }

    companion object{
        private const val SUCCESS = 200
    }
}

