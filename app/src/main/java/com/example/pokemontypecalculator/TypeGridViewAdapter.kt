package com.example.pokemontypecalculator

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TypeGridViewAdapter(private var arrayList: ArrayList<TypeGridView>) :
    RecyclerView.Adapter<TypeGridViewAdapter.ItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.type_table_grid_layout, parent, false)
        return ItemHolder(itemHolder)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        val typeGridView:TypeGridView = arrayList[position]

        holder.icons.setImageResource(typeGridView.iconsInGridView!!)
        holder.alphas.text = arrayList[position].textInGridView.toString()
        typeGridView.backgroundColorInGridView?.let { holder.backgrounds?.setBackgroundColor(it) }
        typeGridView.textColorInGridView?.let { holder.alphas.setTextColor(it) }

    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var icons = itemView.findViewById<ImageView>(R.id.type_table_icons)
        var alphas: TextView = itemView.findViewById<TextView>(R.id.type_table_text)
        var backgrounds: RelativeLayout? = itemView.findViewById<RelativeLayout>(R.id.type_table_relative_layout)


    }

}