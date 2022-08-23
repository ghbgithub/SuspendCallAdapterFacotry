package com.hongbo.retrofit.calladapterfacotry

import com.hongbo.retrofit.suspend_calladapter.HttpResult
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface WanandoridApi {

    @GET("article/list/{page}/json")
    suspend fun articleList(@Path("page") page:Int = 0) : HttpResult<ResultDataWrap<ArticleData>>
}