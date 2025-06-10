package com.example.myapp.process.getsong

import retrofit2.http.GET

interface ApiService {
    @GET("api/song")
    suspend fun getSongs(): SongResponse
}