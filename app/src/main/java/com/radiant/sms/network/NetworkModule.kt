package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.AppConfig
import com.radiant.sms.data.TokenStore
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    fun api(ctx: Context): ApiService {
        val tokenStore = TokenStore(ctx.applicationContext)
        return createApiService { tokenStore.getTokenSync() }
    }

    fun createApiService(tokenProvider: () -> String?): ApiService {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(logging) // ✅ logs full request/response
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
            .baseUrl(AppConfig.BASE_URL) // ✅ uses AppConfig
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
