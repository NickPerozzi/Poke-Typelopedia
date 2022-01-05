package com.perozzi_package.pokemontypecalculator.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.perozzi_package.pokemontypecalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}