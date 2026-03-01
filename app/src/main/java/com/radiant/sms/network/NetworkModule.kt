package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.AppConfig
import com.radiant.sms.data.TokenStore
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    /**
     * ✅ Restored: createApiService(tokenProvider)
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

        return Retrofit.Builder()
            .baseUrl(AppConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
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
