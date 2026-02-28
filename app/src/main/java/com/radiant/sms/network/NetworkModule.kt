package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.data.TokenStore
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /**
     * Set your production base URL here.
     * IMPORTANT: Must end with "/"
     */
    private const val BASE_URL = "https://basic.bd-d.online/"

    /**
     * Used inside Composables:
     * val api = remember { NetworkModule.api(ctx) }
     */
    fun api(ctx: Context): ApiService {
        val tokenStore = TokenStore(ctx.applicationContext)
        return createApiService { tokenStore.getTokenSync() }
    }

    /**
     * Used by AuthViewModel
     */
    fun createApiService(tokenProvider: () -> String?): ApiService {

        // 🔎 Logging Interceptor (for debugging 404 etc.)
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(logging) // 👈 LOGS FULL REQUEST + RESPONSE
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
