package com.perozzi_package.pokemontypecalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FunFactAdapter(private var arrayListForFunFacts: ArrayList<FunFact>):
    RecyclerView.Adapter<FunFactAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunFactAdapter.ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_type_trivia_layout, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun getItemCount(): Int { return arrayListForFunFacts.size }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.itemTitle.text = arrayListForFunFacts[position].funFactTitle
        holder.itemFact.text = arrayListForFunFacts[position].funFactText
    }

    inner class ItemHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var itemTitle: TextView = itemView.findViewById(R.id.funFactTitle)
        var itemFact: TextView = itemView.findViewById(R.id.funFactText)
    }

}