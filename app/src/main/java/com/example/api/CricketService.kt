package com.example.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.Response
import retrofit2.http.Url

interface CricketService {
    @GET
    suspend fun getRssFeed(@Url url: String): ResponseBody

    @GET
    suspend fun checkHtml(@Url url: String): Response<Void>
    @GET
    suspend fun getHtml(@Url url: String): ResponseBody
}
