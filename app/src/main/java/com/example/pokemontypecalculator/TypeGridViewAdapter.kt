package com.example.pokemontypecalculator

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class TypeGridViewAdapter(var context: Context, var arrayList: ArrayList<TypeGridView>) :
    RecyclerView.Adapter<TypeGridViewAdapter.ItemHolder>() {

    var iconsChar: Int? = 0
    var alphaCar: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.type_table_grid_layout, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        var typeGridView:TypeGridView = arrayList.get(position)

        holder.icons.setImageResource(typeGridView.iconsInGridView!!)
        holder.alphas.text = arrayList[position].textInGridView.toString()

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var icons = itemView.findViewById<ImageView>(R.id.type_table_icons)
        var alphas: TextView = itemView.findViewById<TextView>(R.id.type_table_text)


    }

}