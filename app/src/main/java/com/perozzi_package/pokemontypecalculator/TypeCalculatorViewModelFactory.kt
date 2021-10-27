package com.perozzi_package.pokemontypecalculator

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TypeCalculatorViewModelFactory(
    private val resources: Resources,
    private val app: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TypeCalculatorViewModel::class.java)) return TypeCalculatorViewModel(resources, app) as T
        throw IllegalArgumentException("ViewModel")
    }
}