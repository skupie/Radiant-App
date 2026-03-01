package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.AppConfig
import com.radiant.sms.data.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    fun createApiService(tokenProvider: () -> String?): ApiService {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val defaultHeaders = Interceptor { chain ->
            val token = tokenProvider()

            val req = chain.request().newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("User-Agent", "RadiantSMS-Android")
                .apply {
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                }
                .build()

            chain.proceed(req)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(defaultHeaders)
            .addInterceptor(logger)
            .build()

        // ✅ FIX: Moshi must support Kotlin classes that don't have @JsonClass(generateAdapter = true)
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    fun api(context: Context): ApiService {
        val tokenStore = TokenStore(context)
        return createApiService { tokenStore.getTokenSync() }
    }

    fun absoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return AppConfig.BASE_URL.trimEnd('/') + "/" + path.trimStart('/')
    }
}
