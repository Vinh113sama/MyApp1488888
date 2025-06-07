package com.example.myapp.process.getsong

import retrofit2.http.GET

interface ApiService {
    @GET("resources/braniumapis/song.json")
    suspend fun getSongs(): SongResponse

}