package com.hillbeater.myapplication.api

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("save_user")
    suspend fun request(@Body userDetail: UserDetail): Response

}