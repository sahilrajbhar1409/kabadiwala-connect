package com.kabadiwalaconnect.data.backend

import android.content.Context
import com.kabadiwalaconnect.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BackendNetwork {
    fun create(context: Context): BackendApiRepository {
        val session = BackendSessionRepository(context.applicationContext)
        val authInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
            session.token()?.takeIf { it.isNotBlank() }?.let {
                request.header("Authorization", "Bearer $it")
            }
            chain.proceed(request.build())
        }
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
        val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/') + "/"
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)
        return BackendApiRepository(api, session)
    }
}
