package com.perozzi_package.pokemontypecalculator.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.R

class FunFactAdapter(private var arrayListForFunFacts: ArrayList<FunFact>):
    RecyclerView.Adapter<FunFactAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.fun_fact_item_layout, parent, false)
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