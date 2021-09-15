package com.perozzi_package.pokemontypecalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


//TODO Activity more like fragment amiright
class TypeTriviaActivity : AppCompatActivity() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_trivia)

        layoutManager = LinearLayoutManager(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        adapter = RecyclerAdapter()
        recyclerView.adapter = adapter

        val actionBar = supportActionBar
        actionBar!!.title = "Pokémon typing fun facts!"
        actionBar.setDisplayHomeAsUpEnabled(true)

        /*val binding = ActivityTypeTriviaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val triviaCardView = binding.triviaCardView*/

    } // End of onCreate
}