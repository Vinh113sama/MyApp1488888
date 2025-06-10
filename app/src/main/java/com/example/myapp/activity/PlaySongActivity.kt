package com.example.myapp.activity

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp.R
import com.example.myapp.databinding.ActivityPlaySongBinding
import com.example.myapp.process.getsong.Song
import java.util.Locale

class PlaySongActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaySongBinding
    private lateinit var playlist: ArrayList<Song>
    private var currentPosition: Int = 0
    private var isFavorite = false
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playlist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("playlist", Song::class.java) ?: arrayListOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("playlist") ?: arrayListOf()
        }
        currentPosition = intent.getIntExtra("position", 0)

        if (playlist.isNotEmpty()) {
            val currentSong = playlist[currentPosition]
            binding.tvSongName.text = currentSong.title
            binding.tvArtistName.text = currentSong.artist.name
            binding.tvTimeMax.text = formatDuration(currentSong.duration)

        } else {
            binding.tvSongName.text = "Không có bài hát"
        }

        binding.imgbtnNext.setOnClickListener {
            nextSong()
        }
        binding.imgbtnPlayback.setOnClickListener {
            previousSong()
        }
        binding.imgbtnReplay.setOnClickListener {
            loadSong(currentPosition)
        }
        binding.imgbtnShuffle.setOnClickListener {
            val randomIndex = (0 until playlist.size).random()
            loadSong(randomIndex)
        }
        binding.imgbtnBack.setOnClickListener {
            finish()
        }
        binding.imgbtnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            saveFavoriteStatus(playlist[currentPosition].id, isFavorite)
            updateFavoriteIcon()
        }
        binding.imgbtnPlay.setOnClickListener {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
                binding.imgbtnPlay.setImageResource(R.drawable.ic_play)
            } else {
                if (mediaPlayer == null) {
                    playSong(playlist[currentPosition].url)
                } else {
                    mediaPlayer?.start()
                    isPlaying = true
                    binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
                }
            }
        }
    }

    private fun playSong(url: String) {
        mediaPlayer?.release()
        isPlaying = false
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                it.start()
                this@PlaySongActivity.isPlaying = true
                binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    private fun loadSong(position: Int) {
        val currentSong = playlist[position]
        binding.tvSongName.text = currentSong.title
        binding.tvArtistName.text = currentSong.artist.name
        binding.tvTimeMax.text = formatDuration(currentSong.duration)

        isPlaying = false
        isFavorite = loadFavoriteStatus(currentSong.id)
        updateFavoriteIcon()
        playSong(currentSong.url)
    }

    private fun updateFavoriteIcon() {
        val iconRes = if (isFavorite) R.drawable.ic_delete_favorite else R.drawable.ic_add_favorite
        binding.imgbtnFavorite.setImageResource(iconRes)
    }

    private fun loadFavoriteStatus(songId: Int): Boolean {
        val prefs = getSharedPreferences("favorites", MODE_PRIVATE)
        return prefs.getBoolean(songId.toString(), false)
    }

    private fun saveFavoriteStatus(songId: Int, isFavorite: Boolean) {
        val prefs = getSharedPreferences("favorites", MODE_PRIVATE)
        prefs.edit().putBoolean(songId.toString(), isFavorite).apply()
    }

    private fun formatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    private fun PlaySongActivity.nextSong() {
        if (playlist.isNotEmpty()) {
            currentPosition = (currentPosition + 1) % playlist.size
            loadSong(currentPosition)
        }
    }

    private fun PlaySongActivity.previousSong() {
        if (playlist.isNotEmpty()) {
            if (currentPosition - 1 < 0) {
                currentPosition = playlist.size - 1
            } else currentPosition -= 1
        }
        loadSong(currentPosition)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
