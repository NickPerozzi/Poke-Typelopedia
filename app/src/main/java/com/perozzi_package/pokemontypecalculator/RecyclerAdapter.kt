package com.perozzi_package.pokemontypecalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerAdapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var titles: Array<String> = arrayOf(
        "Gen I: Ghost-type coding error",
        "Gen I: Displayed text error 1",
        "Gen I: Displayed text error 2",
        "Gen II: Dark and Steel types added",
        "Gen II: Bug-Poison interactions changed",
        "Gen II: Fire-Ice interactions changed",
        "Gen VI: Fairy type added",
        "Gen VI: Ghost-Steel and Dark-Steel interactions changed",
        "About this app"
    )
    private var facts: Array<String> = arrayOf(
        "In Generation I, Ghost-type moves were intended to be super effective against Psychic-type Pokémon, but due to a coding error they instead did not effect them.",
                "In Generation I, if an attack was super effective against one type but not very effective against the other (eg. Dig against Venusaur), the Pokémon would receive neutral damage, but the game would erroneously display \"not very effective\" or \"super effective\".",
                "In Generation I, if an attack was super effective or not very effective against one type but did no damage against another type (eg. Dig against Charizard), the Pokémon would never receive any damage but the game will erroneously state that the attack \"missed\".",
                "Dark and Steel types were introduced in 2000 with Generation II (Gold/Silver/Crystal).",
                "In Generation I, Bug-type moves were super effective against Poison-type Pokémon and vice versa. Starting in Generation II, Bug-type moves were not very effective, and Poison-type moves would do neutral damage in return.",
                "In Generation I, Ice-type moves were neutral against Fire-type Pokémon. In Generation II onwards, they were not very effective.",
                "Fairy type was introduced in 2013 with Generation VI (X/Y).",
                "In Generations II through V, Ghost-type and Dark-type moves were not very effective against Steel-type Pokémon. From Generation VI onward, these types do neutral damage.",
                "This app was created by Nick Perozzi. This is my first coding endeavor, and I am open to ways to improve the app. If you have any questions or concerns feel free to reach out to me at perozzi.n@gmail.com. Thanks for your time, and I hope this app can be useful to you!"
    )
    //private var titles: Array<String> = resources.getStringArray(R.array.fun_fact_titles)
    //private var facts: Array<String> = resources.getStringArray(R.array.fun_fact_contents)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.activity_type_trivia_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return titles.size
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.ViewHolder, position: Int) {
        holder.itemTitle.text = titles[position]
        holder.itemFact.text = facts[position]
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var itemTitle: TextView
        var itemFact: TextView

        init {
            itemTitle = itemView.findViewById(R.id.funFactTitle)
            itemFact = itemView.findViewById(R.id.funFactText)
        }
    }

}