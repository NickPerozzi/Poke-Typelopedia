package com.perozzi_package.pokemontypecalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


//TODO Activity more like fragment am I right?
class TypeTriviaActivity : AppCompatActivity() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var funFactAdapter: RecyclerView.Adapter<FunFactAdapter.ItemHolder>? = null
    private var arrayListForFunFact:ArrayList<FunFact> ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_trivia)

        val funFactTitles: Array<String> = resources.getStringArray(R.array.fun_fact_titles)
        val funFactContents: Array<String> = resources.getStringArray(R.array.fun_fact_contents)


        layoutManager = LinearLayoutManager(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        arrayListForFunFact = setDataInFunFactList(funFactTitles,funFactContents)
        funFactAdapter = FunFactAdapter(arrayListForFunFact!!)
        recyclerView.adapter = funFactAdapter

        val actionBar = supportActionBar
        actionBar!!.title = "Some fun facts about types!"
        actionBar.setDisplayHomeAsUpEnabled(true)
    }

    private fun setDataInFunFactList(funFactTitles: Array<String>, funFactText: Array<String>):
            ArrayList<FunFact> {
        val items: ArrayList<FunFact> = ArrayList()
        for (i in 0 until 9) {
            items.add(
                FunFact(
                    funFactTitles[i],
                    funFactText[i]
                )
            )
        }
        return items
    }
}