package com.hongbo.retrofit.calladapterfacotry

/**
 * 各类不同url地址的service获取类
 */
object ApiServiceGetter {
    fun getWanandoridApi():WanandoridApi = ServiceFactory.createService(WanandoridApi::class.java, "https://www.wanandroid.com/")
}