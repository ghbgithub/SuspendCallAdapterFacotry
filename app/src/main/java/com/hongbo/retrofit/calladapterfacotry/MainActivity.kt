package com.hongbo.retrofit.calladapterfacotry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.hongbo.retrofit.calladapterfacotry.databinding.ActivityMainBinding
import com.hongbo.retrofit.suspend_calladapter.HttpResult
import com.hongbo.retrofit.suspend_calladapter.SuspendCallAdapterFactory
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.button.setOnClickListener {
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
        }

    }
}