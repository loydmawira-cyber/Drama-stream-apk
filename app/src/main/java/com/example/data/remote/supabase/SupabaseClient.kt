package com.example.data.remote.supabase

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    val isConfigured: Boolean
        get() {
            val url = BuildConfig.SUPABASE_URL
            val key = BuildConfig.SUPABASE_ANON_KEY
            return url.isNotBlank() &&
                    !url.contains("your-project") &&
                    key.isNotBlank() &&
                    !key.contains("placeholder")
        }

    val supabaseUrl: String
        get() {
            val raw = BuildConfig.SUPABASE_URL.trim()
            return if (raw.isNotBlank() && raw.startsWith("http")) {
                if (raw.endsWith("/")) raw else "$raw/"
            } else {
                "https://your-project.supabase.co/"
            }
        }

    val anonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY.trim()

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(supabaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val authApi: SupabaseAuthApi by lazy {
        retrofit.create(SupabaseAuthApi::class.java)
    }

    val restApi: SupabaseRestApi by lazy {
        retrofit.create(SupabaseRestApi::class.java)
    }
}
