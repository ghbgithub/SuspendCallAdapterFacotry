package com.hongbo.retrofit.calladapterfacotry


data class ResultDataWrap<T>(
    val `data`: T,
    val errorCode: Int,
    val errorMsg: String,
)

object ResultDataStatus{
    const val SUCCESS = 0
}



