package com.example.myapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentSongListBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.Song
import com.example.myapp.process.getsong.SongAdapter
import kotlinx.coroutines.launch


class SongListFragment : Fragment() {
    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter
  //  private val songList = mutableListOf<Song>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongAdapter()
        binding.rcPopular.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPopular.adapter = adapter

        adapter.setOnItemClickListener { song, position ->
            val intent = Intent(requireContext(), PlaySongActivity::class.java)
            intent.putParcelableArrayListExtra("playlist", ArrayList(adapter.currentList))
            intent.putExtra("song", song)
            intent.putExtra("position", position)
            startActivity(intent)
        }
        fetchSongs()
    }

    private fun fetchSongs() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getSongs()
                val songs = response.data
                adapter.submitList(songs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}