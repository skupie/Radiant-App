package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.AppConfig
import com.radiant.sms.data.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    /**
     * createApiService(tokenProvider)
     * tokenProvider returns the latest token (or null) whenever a request is made.
     */
    fun createApiService(tokenProvider: () -> String?): ApiService {
        val authInterceptor = Interceptor { chain ->
            val token = tokenProvider()
            val requestBuilder = chain.request().newBuilder()

            if (!token.isNullOrBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Convenience helper used by Compose screens:
     * NetworkModule.api(context) -> ApiService
     */
    fun api(context: Context): ApiService {
        val tokenStore = TokenStore(context)
        return createApiService { tokenStore.getTokenSync() }
    }

    /**
     * Utility used by MemberShareDetailsScreen to turn relative paths into full URLs.
     */
    fun absoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return AppConfig.BASE_URL.trimEnd('/') + "/" + path.trimStart('/')
    }
}
