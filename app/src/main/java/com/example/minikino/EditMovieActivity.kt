package com.example.minikino

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.minikino.databinding.ActivityEditMovieBinding
import com.example.minikino.model.MovieEntry

@Suppress("INFERRED_TYPE_VARIABLE_INTO_POSSIBLE_EMPTY_INTERSECTION")
class EditMovieActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditMovieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEditMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val genres = resources.getStringArray(R.array.movie_genres)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, genres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genreUpdateSpinner.adapter = adapter

        val movieToEdit = if(Build.VERSION.SDK_INT >= 33) {
            intent.getSerializableExtra("EXTRA_MOVIE", MovieEntry::class.java)
        } else {
            intent.getSerializableExtra("EXTRA_MOVIE") as? MovieEntry
        }

        if(movieToEdit == null) {
            finish()
            return
        }
        binding.titleUpdateEditText.setText(movieToEdit.title)
        binding.updateRatingBar.rating = movieToEdit.rating
        val currentGenreIndex = adapter.getPosition(movieToEdit.genre)
        binding.genreUpdateSpinner.setSelection(currentGenreIndex)

        binding.saveChangesButton.setOnClickListener {
            val newTitle = binding.titleUpdateEditText.text.toString()
            val newGenre = binding.genreUpdateSpinner.selectedItem.toString()
            val newRating = binding.updateRatingBar.rating

            if(newTitle.isNotEmpty()) {
                val updateMovie = MovieEntry(newTitle, newGenre, newRating)
                val returnIntent = Intent()
                returnIntent.putExtra("UPDATED_MOVIE", updateMovie)
                val originalPosition = intent.getIntExtra("EXTRA_POSITION", -1)
                returnIntent.putExtra("POSITION", originalPosition)

                setResult(RESULT_OK, returnIntent)
                finish()

            }else {
                Toast.makeText(this, "Tytul jest wymagany!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}