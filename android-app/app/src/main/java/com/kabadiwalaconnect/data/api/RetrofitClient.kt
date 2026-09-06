package com.kabadiwalaconnect.data.api

import android.content.Context
import com.kabadiwalaconnect.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun create(context: Context): BackendApiClient {
        val session = ApiSessionRepository(context.applicationContext)
        val auth = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            session.token()?.takeIf { it.isNotBlank() }?.let { builder.header("Authorization", "Bearer $it") }
            chain.proceed(builder.build())
        }
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder().addInterceptor(auth).addInterceptor(logging).build()
        val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        val api = Retrofit.Builder().baseUrl(baseUrl).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ApiService::class.java)
        return BackendApiClient(api, session)
    }
}
