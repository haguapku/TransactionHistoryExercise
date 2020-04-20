package com.example.transactionhistoryexercise.api

import com.example.transactionhistoryexercise.MainApplication
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import java.io.File
import java.util.concurrent.TimeUnit

const val BASE_URL = "https://www.dropbox.com"

interface ApiService {

    @Streaming
    @GET("s/tewg9b71x0wrou9/data.json?dl=1")
    suspend fun fetchLatestDataWithFixedUrl(): Response<ResponseBody>

    companion object {
        fun create(): ApiService = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(createOkHttpClient())
            .build().create(ApiService::class.java)

        private fun createOkHttpClient(): OkHttpClient {
            val cache = Cache(File(MainApplication.instance.cacheDir, "httpCache"), (1024 * 1024 * 100).toLong())
            return OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(BaseIntercepter())
                .addNetworkInterceptor(HttpCacheInterceptor())
                .readTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build()
        }
    }
}