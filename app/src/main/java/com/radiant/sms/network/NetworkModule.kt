package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.data.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /** Keep trailing slash. */
    private const val BASE_URL = "https://basic.bd-d.online/"

    /** Main entry point used by Composables:
     *  val api = remember { NetworkModule.api(ctx) }
     */
    fun api(ctx: Context): ApiService {
        val tokenStore = TokenStore(ctx.applicationContext)
        return createApiService { tokenStore.getTokenSync() }
    }

    /** Used by ViewModels where token comes from memory or storage. */
    fun createApiService(tokenProvider: () -> String?): ApiService {

        // ✅ HTTP logger (DISABLED)
        val httpLogger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(httpLogger)
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

        // ✅ IMPORTANT: Kotlin adapter so Moshi can parse Kotlin data classes
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
