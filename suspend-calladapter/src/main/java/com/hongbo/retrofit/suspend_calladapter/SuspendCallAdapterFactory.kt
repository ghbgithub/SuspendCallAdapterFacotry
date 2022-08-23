package com.hongbo.retrofit.suspend_calladapter

import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 适配返回类型为 HttpResult<T>，且为suspend修饰的挂起函数callback
/**
 * param：res->接口返回的json字符串，type->接口返回的json数据类型
 * return:boolean->返回ture表示业务上成功获取到数据，String->接口返回的成功/失败的提示语
*/
 */

/**
 * apiSuccessCheckCall： 设置接口返回是否成功的回调
 * param：res->接口返回的json字符串，type->接口返回的json数据类型
 * return:Pair<Boolean,String>
 * first->返回ture调用方得到的类型为HttpResult.Success;false->HttpResult.ApiError
 * second->返回给调用方的提示语
 */
class SuspendCallAdapterFactory(
    private val apiSuccessCheckCall:(res:Any,type:Type)->Pair<Boolean,String> = {_,_ -> Pair(true,"success")}
)  : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {

        //协程挂起函数默认返回值是Call<*>，如果不满足该条件，那么返回null让retrofit选择其他家伙来处理
//        getRawType是提取的实例class，如果我们的Call<T> 他得到的就是Call.class
        if (Call::class.java != getRawType(returnType)) {
            return null
        }

        //检查Call内部的泛型是否包含了其他泛型 ParameterizedType意思就是类型内部包含泛型。eg:Call<User>
        check(returnType is ParameterizedType) {
            "return type must be HttpResult<*> or HttpResult<out *> for Call<*> check"
        }

        //获取Call类包裹的第一个泛型。也就是Call<HttpResult<T>>里面的HttpResult<T>
        val responseType = getParameterUpperBound(0, returnType)

        //Call类包裹的第一个泛型不是HttpResult类，那么返回null，让retrofit选择其他 CallAdapter.Factory
        if (getRawType(responseType) != HttpResult::class.java) {
            return null
        }

        //确保HttpResult内部包的泛型其还包裹另外一层泛型，比如 HttpResult<*>
        check(responseType is ParameterizedType) { "return type must be HttpResult<*> or HttpResult<out *> for HttpResult<*> check" }

        //获取HttpResult类包裹的第一个泛型
        val successBodyType = getParameterUpperBound(0, responseType)

        return object : CallAdapter<Any, Call<HttpResult<Any>>>{
            override fun responseType(): Type {
                return  successBodyType
            }

            override fun adapt(call: Call<Any>): Call<HttpResult<Any>> {
                return SuspendHttpCall(call){
                    apiSuccessCheckCall(it,successBodyType)
                }
            }
        }
    }
}