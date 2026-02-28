package com.radiant.sms.network

import android.util.Log
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /**
     * IMPORTANT:
     * - Base URL MUST end with a slash.
     * - If your endpoints already contain "api/..." then keep BASE_URL without "api/".
     */
    private const val BASE_URL = "https://basic.bd-d.online/"

    fun createApiService(tokenProvider: () -> String?): ApiService {

        // OkHttp built-in logger (prints request/response)
        val httpLogger = HttpLoggingInterceptor { msg ->
            Log.d("API_HTTP", msg)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)

            // 1) BODY logging
            .addInterceptor(httpLogger)

            // 2) Our interceptor: adds headers + prints URL + status code
            .addInterceptor { chain ->
                val token = tokenProvider()?.trim()

                val reqBuilder = chain.request().newBuilder()
                    .header("Accept", "application/json")

                if (!token.isNullOrBlank()) {
                    reqBuilder.header("Authorization", "Bearer $token")
                }

                val request = reqBuilder.build()
                Log.d("API_HTTP", "➡️ ${request.method} ${request.url}")

                val response = chain.proceed(request)
                Log.d("API_HTTP", "⬅️ ${response.code} ${request.url}")

                response
            }
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
