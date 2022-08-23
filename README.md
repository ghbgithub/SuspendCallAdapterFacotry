# retrofit supend calladapter
处理Android中使用retrofit调用接口时由于异常（404，没授权、json解析等异常）导致的闪退，不再需要try-catch。接口调用异常统一封装判断处理并返回给调用方。
## 使用方法：

可以传入一个回调用于判断业务接口是否成功，res->接口返回的实体，Type->返回的实体类型；return:Pair.first->true成功，Pair.second->业务接口提示信息。
使用工具类判断实体类的类型：if(Utils.getRawType(type) == ResultDataWrap::class.java)
``` kotlin
//retrofit
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
```

``` kotlin
//调用
lifecycleScope.launch {
	when(val res = ApiClient.api.articleList()){
		is HttpResult.Success -> {
			val result = res.value
		}
		else ->{
			val msg = res.msg
		}
	}
}
```

