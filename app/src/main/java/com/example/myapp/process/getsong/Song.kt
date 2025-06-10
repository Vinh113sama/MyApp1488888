package com.example.myapp.process.getsong

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Int,
    val title: String,
    val titleNormalized: String,
    val duration: Int,
    val url: String,
    val imageUrl: String?,
    val artistId: Int,
    val createdAt: String,
    val artist: Artist,
) : Parcelable

@Parcelize
data class Artist(
    val id: Int,
    val name: String,
    val nameNormalized: String,
    val imageUrl: String?,
    val createdAt: String
) : Parcelable