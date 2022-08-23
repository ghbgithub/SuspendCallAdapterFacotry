package com.hongbo.retrofit.calladapterfacotry

import android.bluetooth.BluetoothClass
import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.hongbo.retrofit.suspend_calladapter.SuspendCallAdapterFactory
import com.hongbo.retrofit.suspend_calladapter.Utils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

/**
 * service创建工厂
 */
object ServiceFactory {
    private val okHttpClient by lazy {
        val newBuilder = OkHttpClient().newBuilder()
        newBuilder.connectTimeout(60 * 5, TimeUnit.SECONDS)
        newBuilder.readTimeout(60 * 5,TimeUnit.SECONDS)
        val sslContext: SSLContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(AppX509TrustManager()), SecureRandom())
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
        newBuilder.sslSocketFactory(sslSocketFactory,
            AppX509TrustManager()
        )
        newBuilder.hostnameVerifier { _, _ -> true }
        newBuilder.addInterceptor(HttpLoggingInterceptor{
            Log.i("ServiceFactory",it)
        }.setLevel(HttpLoggingInterceptor.Level.BODY))
    }


    private val builder by lazy {
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(SuspendCallAdapterFactory(){ res: Any, type: Type ->
                if(Utils.getRawType(type) == ResultDataWrap::class.java){
                    val wrapObj = res as ResultDataWrap<*>
                    val success = wrapObj?.let {
                        (wrapObj.errorCode == ResultDataStatus.SUCCESS)
                    }?: kotlin.run {
                        false
                    }
                    val message = wrapObj?.let {
                        wrapObj.errorMsg ?: "获取数据失败"
                    }?: kotlin.run {
                        "获取数据失败"
                    }
                    Pair(success,message)
                }else{
                    Pair(true,"")
                }
            })
    }

    fun <T> create(clazz: Class<T>, baseUrl: String): T {
        return builder.baseUrl(baseUrl)
            .client(okHttpClient.build())
            .build().create(clazz)
    }


    inline fun <reified T> createService(clazz: Class<T>, baseUrl: String): T =
        create(clazz, baseUrl)

}