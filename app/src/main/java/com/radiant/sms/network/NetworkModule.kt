package com.radiant.sms.network

import android.util.Log
import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // ✅ IMPORTANT: must end with '/'
    private const val BASE_URL = "https://basic.bd-d.online/"

    fun createApiService(tokenProvider: () -> String?): ApiService {

        // Logs full request/response body to Logcat (if available)
        val httpLogging = HttpLoggingInterceptor { msg ->
            Log.d("HTTP", msg)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Custom interceptor to print the final URL your app is calling
        val debugInterceptor = Interceptor { chain ->
            val original = chain.request()

            val token = tokenProvider()?.trim()
            val builder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")

            if (!token.isNullOrBlank()) {
                builder.header("Authorization", "Bearer $token")
            }

            val req = builder.build()

            Log.d("HTTP_URL", "➡️ ${req.method} ${req.url}")

            val res = chain.proceed(req)

            Log.d("HTTP_URL", "⬅️ ${res.code} ${req.method} ${req.url}")

            res
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(debugInterceptor)
            .addInterceptor(httpLogging)
            .build()

        val moshi = Moshi.Builder().build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
