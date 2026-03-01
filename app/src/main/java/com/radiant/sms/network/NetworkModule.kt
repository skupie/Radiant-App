package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.data.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    const val BASE_URL = "https://basic.bd-d.online/"

    private fun authInterceptor(context: Context): Interceptor = Interceptor { chain ->
        val token = TokenStore(context).getTokenSync()
        val req = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(req)
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun api(context: Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor(context))
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(ApiService::class.java)
    }

    fun absoluteUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val p = path.trim()
        if (p.startsWith("http://") || p.startsWith("https://")) return p
        val normalized = if (p.startsWith("/")) p.drop(1) else p
        return BASE_URL + normalized
    }

    // ✅ Backwards compatible wrapper for older code
    fun createApiService(context: Context): ApiService = api(context)
}
