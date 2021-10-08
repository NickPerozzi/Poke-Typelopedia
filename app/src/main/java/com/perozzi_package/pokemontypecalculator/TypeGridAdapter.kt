package com.perozzi_package.pokemontypecalculator

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TypeGridAdapter :
    ListAdapter<TypeGrid, TypeGridAdapter.ItemHolder>(TypeGridDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.type_table_grid_layout, parent, false)
        return ItemHolder(itemHolder)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val typeGrid:TypeGrid = getItem(position)
        holder.icons.setImageResource(typeGrid.iconsInGridView!!)
        holder.alphas.text = getItem(position).textInGridView
        typeGrid.backgroundColorInGridView?.let { holder.backgrounds?.setBackgroundColor(it) }
        typeGrid.textColorInGridView?.let { holder.alphas.setTextColor(it) }
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icons: ImageView = itemView.findViewById(R.id.type_table_icons)
        var alphas: TextView = itemView.findViewById(R.id.type_table_text)
        var backgrounds: RelativeLayout? = itemView.findViewById(R.id.type_table_relative_layout)
    }
}

class TypeGridDiffUtil : DiffUtil.ItemCallback<TypeGrid>() {
    override fun areItemsTheSame(oldItem: TypeGrid, newItem: TypeGrid): Boolean {
        val sameBackgroundColors = oldItem.backgroundColorInGridView == newItem.backgroundColorInGridView
        val sameTextColors = oldItem.textColorInGridView == newItem.textColorInGridView
        val sameIcons = oldItem.iconsInGridView == newItem.iconsInGridView
        val sameText = oldItem.textInGridView == newItem.textInGridView
        return sameBackgroundColors && sameTextColors && sameIcons && sameText
    }

    override fun areContentsTheSame(oldItem: TypeGrid, newItem: TypeGrid): Boolean {
        return oldItem == newItem
    }
}