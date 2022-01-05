package com.perozzi_package.pokemontypecalculator.ui

import android.app.Application
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.perozzi_package.pokemontypecalculator.R
import com.perozzi_package.pokemontypecalculator.adapters.FunFact
import com.perozzi_package.pokemontypecalculator.adapters.FunFactAdapter

class FunFactViewModel(private val resources: Resources, app: Application) :
    ViewModel() {

    private val titles: Array<String> = resources.getStringArray(R.array.fun_fact_titles)
    private val contents: Array<String> = resources.getStringArray(R.array.fun_fact_contents)

    private fun setDataInFunFactList(funFactTitles: Array<String>, funFactText: Array<String>):
            ArrayList<FunFact> {
        val items: ArrayList<FunFact> = ArrayList()
        for (i in funFactTitles.indices) {
            items.add(
                FunFact(
                    funFactTitles[i],
                    funFactText[i]
                )
            )
        }
        return items
    }

    val funFactAdapter = FunFactAdapter(setDataInFunFactList(titles,contents))

    var backgroundColor: Drawable? =
        when (this.resources.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> ContextCompat.getDrawable(
                app,
                R.drawable.main_header_selector_night
            )
            Configuration.UI_MODE_NIGHT_NO -> ContextCompat.getDrawable(
                app,
                R.drawable.main_header_selector
            )
            else -> ContextCompat.getDrawable(app, R.drawable.main_header_selector)
        }
}