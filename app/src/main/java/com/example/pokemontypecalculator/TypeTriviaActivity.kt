package com.example.pokemontypecalculator

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemontypecalculator.databinding.ActivityTypeTriviaBinding


//TODO Activity more like fragment amiright
class TypeTriviaActivity : AppCompatActivity() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_trivia)

        layoutManager = LinearLayoutManager(this)

        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        adapter = RecyclerAdapter()
        recyclerView.adapter = adapter

        val actionBar = supportActionBar
        actionBar!!.title = "Pokémon typing fun facts!"
        actionBar.setDisplayHomeAsUpEnabled(true)

        /*val binding = ActivityTypeTriviaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val triviaCardView = binding.triviaCardView*/

        // Adjusts background based on whether night mode is on or not
        /*
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                println("night mode is on")
                triviaCardView.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector_night)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                println("night mode is not on")
                triviaCardView.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector)
            }
        }*/


    } // End of onCreate
}