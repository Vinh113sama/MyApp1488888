package com.example.myapp.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.ActivityHomeBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var imgbtnMenu: ImageButton
    private lateinit var imgbtnSearch: ImageButton
    private lateinit var edtSearch: EditText
    private lateinit var tvPopularSongs: TextView
    private lateinit var adapter: SongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initViews()
        setupView()
        fetchSongs()

    }

    private fun setupView() {
        imgbtnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

    }

    private fun initViews() {
        imgbtnMenu = binding.imgbtnMenu
        imgbtnSearch = binding.imgbtnSearch
        edtSearch = binding.edtSearch
        tvPopularSongs = binding.lablePopularSongs
        adapter = SongAdapter()
        binding.rcPopurlar.layoutManager = LinearLayoutManager(this)
        binding.rcPopurlar.adapter = adapter
    }

    private fun fetchSongs() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getSongs()
                val songs = response.songs
                adapter.submitList(songs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}