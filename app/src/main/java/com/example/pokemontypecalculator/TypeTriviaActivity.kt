package com.example.pokemontypecalculator

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.pokemontypecalculator.databinding.ActivityMainBinding
import com.example.pokemontypecalculator.databinding.ActivityTypeTriviaBinding


//TODO Activity more like fragment amiright
class TypeTriviaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_trivia)

        val actionBar = supportActionBar
        actionBar!!.title = "Pokémon typing fun facts"
        actionBar.setDisplayHomeAsUpEnabled(true)

        // Adjusts background based on whether night mode is on or not
        val binding = ActivityTypeTriviaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val triviaLinearLayout = binding.triviaLinearLayout
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                println("night mode is on")
                triviaLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector_night)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                println("night mode is not on")
                triviaLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector)
            }
        }


    } // End of onCreate
}