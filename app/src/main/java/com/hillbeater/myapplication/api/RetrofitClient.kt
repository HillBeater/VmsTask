package com.hillbeater.myapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://hpzenith.com/Admin/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

