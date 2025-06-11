package com.example.myapp.activity

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
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
    private var isShuffle = true
    private val handler = android.os.Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaySongBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initPlaylist()
        setupUI()
        setupEvent()
    }

    private fun playSong(url: String) {
        runnable?.let { handler.removeCallbacks(it) }
        mediaPlayer?.release()
        isPlaying = false
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                it.start()
                this@PlaySongActivity.isPlaying = true
                binding.imgbtnPlay.setImageResource(R.drawable.ic_pause)
                startSeekBarUpdate()
            }
        }
        mediaPlayer?.setOnCompletionListener {
            runnable?.let { handler.removeCallbacks(it) }
            if (!isShuffle) {
                val randomPosition = (0 until playlist.size).random()
                loadSong(randomPosition)
            } else {
                nextSong()
            }
        }
    }


    private fun initPlaylist() {
        playlist = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("playlist", Song::class.java) ?: arrayListOf()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("playlist") ?: arrayListOf()
        }
        currentPosition = intent.getIntExtra("position", 0)
    }

    private fun setupUI() {
        if (playlist.isNotEmpty()) {
            val currentSong = playlist[currentPosition]
            binding.tvSongName.text = currentSong.title
            binding.tvArtistName.text = currentSong.artist.name
            binding.tvArtistName.isSelected = true
            Glide.with(this)
                .load(currentSong.imageUrl)
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.img_avatar_default)
                .into(binding.imgSong)
            //   binding.tvTimeMax.text = formatDuration(mediaPlayer!!.duration)

            playSong(currentSong.url)
        } else {
            binding.tvSongName.text = buildString {
                append("@string/no_song")
            }
        }
    }

    private fun setupEvent() {
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
            isShuffle = !isShuffle
            if (isShuffle) {
                binding.imgbtnShuffle.setImageResource(R.drawable.ic_circuit)
            } else {
                binding.imgbtnShuffle.setImageResource(R.drawable.ic_shuffle)
            }
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
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (seekBar != null && mediaPlayer != null && mediaPlayer!!.duration > 0) {
                    val durationInSeconds = mediaPlayer!!.duration / 1000
                    seekBar.max = durationInSeconds
                    runnable?.let { handler.removeCallbacks(it) }
                }

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startSeekBarUpdate()
            }
        })
    }

    private fun loadSong(position: Int) {
        val currentSong = playlist[position]
        binding.tvSongName.text = currentSong.title
        binding.tvArtistName.text = currentSong.artist.name
        Glide.with(this)
            .load(currentSong.imageUrl)
            .placeholder(R.drawable.ic_music_note)
            .error(R.drawable.img_avatar_default)
            .into(binding.imgSong)
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

    @SuppressLint("UseKtx")
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

    private fun startSeekBarUpdate() {
        mediaPlayer?.let { player ->
            binding.tvTimeMax.text = formatDuration((mediaPlayer!!.duration)/1000)
            binding.seekBar.max = player.duration / 1000

            runnable = Runnable {
                val currentPosition = player.currentPosition / 1000
                binding.seekBar.progress = currentPosition
                binding.tvTimeCurrent.text = formatDuration(currentPosition)
                runnable?.let { handler.postDelayed(it, 1000) }
            }
            runnable?.let { handler.post(it) }
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
