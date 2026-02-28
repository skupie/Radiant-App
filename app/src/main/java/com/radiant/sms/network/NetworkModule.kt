package com.radiant.sms.network

import com.radiant.sms.BuildConfig
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    // Make sure this ends with "/"
    private const val BASE_URL = "https://basic.bd-d.online/"

    fun createApiService(tokenProvider: () -> String?): ApiService {

        // Logging interceptor
        val httpLogger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY   // Show logs in debug
            } else {
                HttpLoggingInterceptor.Level.NONE   // Hide logs in release
            }
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(httpLogger)
            .addInterceptor { chain ->
                val token = tokenProvider()?.trim()

                val requestBuilder = chain.request().newBuilder()
                    .header("Accept", "application/json")

                if (!token.isNullOrBlank()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }

                chain.proceed(requestBuilder.build())
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
