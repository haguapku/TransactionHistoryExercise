package com.example.transactionhistoryexercise.api

import com.example.transactionhistoryexercise.util.NetWorkUtil
import okhttp3.Interceptor
import okhttp3.Response

class BaseIntercepter: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        var request = chain.request()
        if (!NetWorkUtil.isNetWorkConnected()) {
            val maxStale = 28 * 24 * 60 * 60
            request = request.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .build()
        }
        return chain.proceed(request)
    }
}