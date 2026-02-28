package com.radiant.sms.network

import android.content.Context
import android.util.Log
import com.radiant.sms.data.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    /** Keep trailing slash. */
    private const val BASE_URL = "https://basic.bd-d.online/"

    /**
     * Main entry point used by Composables / screens:
     * val api = remember { NetworkModule.api(context) }
     */
    fun api(ctx: Context): ApiService {
        val tokenStore = TokenStore(ctx.applicationContext)
        return createApiService { tokenStore.getTokenSync() }
    }

    /**
     * Used by ViewModels where token comes from memory or storage.
     * Provide tokenProvider which returns the latest token string or null.
     */
    fun createApiService(tokenProvider: () -> String?): ApiService {

        // --- Logger (optional) ---
        val httpLogger = HttpLoggingInterceptor().apply {
            // BODY is ok for debug, but can be noisy.
            // Use BASIC if you prefer.
            level = HttpLoggingInterceptor.Level.BODY
        }

        // --- Adds headers + Authorization ---
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()

            val reqBuilder = original.newBuilder()
                .header("Accept", "application/json")

            val rawToken = tokenProvider()?.trim()

            if (!rawToken.isNullOrBlank()) {
                // ✅ Prevent "Bearer Bearer xxx"
                val authValue =
                    if (rawToken.startsWith("Bearer ", ignoreCase = true)) rawToken
                    else "Bearer $rawToken"

                reqBuilder.header("Authorization", authValue)
            }

            val req = reqBuilder.build()

            // ✅ Safe debug log (does not print token)
            val hasAuth = req.header("Authorization") != null
            Log.d("NET_AUTH", "→ ${req.method} ${req.url} authHeader=$hasAuth")

            chain.proceed(req)
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(httpLogger)
            .build()

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
