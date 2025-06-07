package com.example.myapp.process.getsong

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("album")
    val album: String,

    @SerializedName("artist")
    val artist: String,

    @SerializedName("source")
    val source: String,

    @SerializedName("image")
    val image: String,

    @SerializedName("duration")
    val duration: Int
)
