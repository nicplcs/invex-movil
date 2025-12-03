package com.example.appinterface.Api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitInstance {
   private const val BASE_URL = "https://dog.ceo/api/"
    private const val BASE_URL_APIKOTLIN = "http://10.0.2.2:8080/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    val api2kotlin: ApiServicesKotlin by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_APIKOTLIN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServicesKotlin::class.java)
    }

    // Versión con autenticación JWT
    private var retrofitWithAuth: Retrofit? = null

    fun getApi(context: Context): ApiServicesKotlin {
        if (retrofitWithAuth == null) {
            val client = OkHttpClient.Builder()
                .addInterceptor(JwtInterceptor(context))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofitWithAuth = Retrofit.Builder()
                .baseUrl(BASE_URL_APIKOTLIN)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofitWithAuth!!.create(ApiServicesKotlin::class.java)
    }

}