package com.radiant.sms.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.squareup.moshi.Moshi
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /**
     * ✅ Set your production base URL here.
     * Keep trailing slash.
     */
    private const val BASE_URL = "https://basic.bd-d.online/"

    /**
     * Create ApiService with optional Bearer token provider.
     * AuthViewModel already calls:
     *   NetworkModule.createApiService { cachedToken }
     */
    fun createApiService(tokenProvider: () -> String?): ApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = tokenProvider()?.trim()
                val reqBuilder = chain.request().newBuilder()
                    .header("Accept", "application/json")

                if (!token.isNullOrBlank()) {
                    reqBuilder.header("Authorization", "Bearer $token")
                }

                chain.proceed(reqBuilder.build())
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
