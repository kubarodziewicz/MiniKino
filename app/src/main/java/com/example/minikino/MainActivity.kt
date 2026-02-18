package com.example.minikino

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.minikino.databinding.ActivityMainBinding
import com.example.minikino.model.MovieEntry
import kotlinx.serialization.json.Json
import java.io.File

class MainActivity : AppCompatActivity() {

    private val PREFS_NAME = "minikino_prefs"
    private val KEY_LAST_GENRE = "last_genre"
    private val KEY_LAST_RATING = "last_rating"
    private lateinit var binding: ActivityMainBinding
    private var movieList = mutableListOf<MovieEntry>()
    private var movieTitles = mutableListOf<String>()
    private lateinit var listAdapter: ArrayAdapter<String>

    private val editMovieLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val updatedMovie = if (Build.VERSION.SDK_INT >= 33) {
                //  to kod dla nowszych telefonów
                data?.getSerializableExtra("UPDATED_MOVIE", MovieEntry::class.java)
            } else {
                // to kod do wykonania w przypadku starszych wersji
                @Suppress("DEPRECATION")
                data?.getSerializableExtra(Keys.UPDATED_MOVIE) as? MovieEntry
            }
            val position = data?.getIntExtra(Keys.RETURN_POSITION, -1) ?: -1

            if (updatedMovie != null && position != -1) {
                movieList[position] = updatedMovie

                movieTitles[position] = updatedMovie.title

                listAdapter.notifyDataSetChanged()

                saveMoviesToJsonFile()
            }
        }
    }

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

        val genres = resources.getStringArray(R.array.movie_genres)
        val genreAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)

        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genreSpinner.adapter = genreAdapter


        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val lastGenre = prefs.getString(KEY_LAST_GENRE, null)
        lastGenre?.let {
            val index = genres.indexOf(it)
            if(index >= 0) {
                binding.genreSpinner.setSelection(index)
            }
        }

        val lastRating = prefs.getFloat(KEY_LAST_RATING, 0f)
        binding.ratingBar.rating = lastRating


        movieList = mutableListOf<MovieEntry>()
        movieTitles = mutableListOf<String>()
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, movieTitles)

        binding.movieListView.adapter = listAdapter

        binding.movieListView.setOnItemClickListener{_, _, position, _ ->
            val selectedMovie = movieList[position]

            val intent = Intent(this, EditMovieActivity::class.java)
            intent.putExtra("EXTRA_MOVIE", selectedMovie)
            intent.putExtra("EXTRA_POSITION", position)

            editMovieLauncher.launch(intent)
        }

        binding.movieListView.setOnItemLongClickListener{_, _, position, _ ->
            movieTitles.removeAt(position)
            movieList.removeAt(position)

            listAdapter.notifyDataSetChanged()
            Toast.makeText(this, "Usunięto element ${position+1}", Toast.LENGTH_SHORT).show()
            saveMoviesToJsonFile()
            true
        }


        binding.saveBtn.setOnClickListener {
            saveMoviesToJsonFile()
        }

        binding.addMovieBtn.setOnClickListener {
            val title = binding.titleEt.text.toString()
            val genre = binding.genreSpinner.selectedItem.toString()
            val rating = binding.ratingBar.rating

            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            prefs.edit{
                putString(KEY_LAST_GENRE, genre)
                putFloat(KEY_LAST_RATING, rating)
            }

            if(title.isNotBlank()) {
                val newMovie = MovieEntry(title, genre, rating)
                movieList.add(newMovie)
                movieTitles.add("$title - $genre ($rating)")
                listAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Dodano $title", Toast.LENGTH_SHORT).show()

                binding.titleEt.text.clear()
                binding.ratingBar.rating = 0f
                binding.genreSpinner.setSelection(0)
            }else {
                Toast.makeText(this, "Podaj tytuł filmu", Toast.LENGTH_SHORT).show()
            }

        }
        loadMoviesFromJsonFile()
    }

    fun saveMoviesToJsonFile() {
        try {
            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(movieList)

            val file = File(filesDir, "movies.json")
            file.writeText(jsonString)


            Toast.makeText(
                this,
                "Zapisano ${movieList.size} filmów do josona",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Błąd zapisu pliku!",
                Toast.LENGTH_SHORT
            ).show()

            e.printStackTrace()
        }
    }


    fun loadMoviesFromJsonFile() {
        try {
            val file = File(filesDir, "movies.json")
            if(!file.exists()) return

            val jsonString = file.readText()
            val json = Json {ignoreUnknownKeys}
            val loadedList = json.decodeFromString<List<MovieEntry>>(jsonString)

            movieList.clear()
            movieList.addAll(loadedList)

            movieTitles.clear()
            movieTitles.addAll(
                movieList.map {
                    "${it.title} - ${it.genre} (${it.rating})"
                }
            )

            listAdapter.notifyDataSetChanged()

        } catch(e: Exception) {
            Toast.makeText(this, "Wystąpił błąd", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }


}