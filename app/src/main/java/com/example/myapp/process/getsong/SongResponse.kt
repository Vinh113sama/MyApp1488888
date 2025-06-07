package com.example.myapp.process.getsong

import com.google.gson.annotations.SerializedName

data class SongResponse(
    @SerializedName("song")
    val songs: List<Song>
)