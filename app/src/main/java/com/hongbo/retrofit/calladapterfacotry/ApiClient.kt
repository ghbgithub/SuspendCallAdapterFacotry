package com.hongbo.retrofit.calladapterfacotry


object ApiClient {
    val api by lazy {
        ApiServiceGetter.getWanandoridApi()
    }
}