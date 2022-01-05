package com.perozzi_package.pokemontypecalculator.adapters

import androidx.recyclerview.widget.DiffUtil

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