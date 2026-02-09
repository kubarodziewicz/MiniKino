package com.example.minikino

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.minikino.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val genres = listOf<String>("Akcja", "Komedia", "Dramat", "Sci-Fi", "Skibidi", "Fantasy", "Horror")
        val genreAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)

        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genreSpinner.adapter = genreAdapter
    }
}