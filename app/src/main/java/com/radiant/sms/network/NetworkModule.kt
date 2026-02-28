package com.radiant.sms.network

import android.content.Context
import com.radiant.sms.storage.TokenStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    // ✅ Change this to your domain
    private const val BASE_URL = "https://basic.bd-d.online/"

    fun api(context: Context): ApiService {
        val tokenStore = TokenStore(context.applicationContext)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = tokenStore.getToken()
                val req = chain.request().newBuilder()
                    .header("Accept", "application/json")

                if (!token.isNullOrBlank()) {
                    req.header("Authorization", "Bearer $token")
                }
                chain.proceed(req.build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
