package com.example.transactionhistoryexercise.api

import okhttp3.Interceptor
import okhttp3.Response

class HttpCacheInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val response = chain.proceed(request)
        val maxAge = 60
        response.newBuilder()
            .removeHeader("Pragma")
            .addHeader("Cache-Control", "public, max-age=$maxAge")
            .build()
        return response
    }
}