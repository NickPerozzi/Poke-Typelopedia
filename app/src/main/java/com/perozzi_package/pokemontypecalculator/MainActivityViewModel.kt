package com.perozzi_package.pokemontypecalculator

import androidx.lifecycle.ViewModel

class MainActivityViewModel: ViewModel() {

    fun onesString(): MutableList<String> {
        val table = mutableListOf<String>()
        for (i in 0 until 18) {
            table.add("1.0")
        }
        return table
    }

    fun onesDouble(): MutableList<Double> {
        val table = mutableListOf<Double>()
        for (i in 0 until 18) {
            table.add(1.0)
        }
        return table
    }

    fun onesInt(): MutableList<Int> {
        val table = mutableListOf<Int>()
        for (i in 0 until 18) {
            table.add(1)
        }
        return table
    }
    // BL
    val listOfNonexistentTypeCombinations: List<List<String>> = listOf(
        listOf(Types.Normal.type,Types.Ice.type), // Normal ice
        listOf(Types.Ice.type,Types.Normal.type),
        listOf(Types.Normal.type,Types.Poison.type), // Normal poison
        listOf(Types.Poison.type,Types.Normal.type),
        listOf(Types.Normal.type,Types.Bug.type), // Normal bug
        listOf(Types.Bug.type,Types.Normal.type),
        listOf(Types.Normal.type,Types.Rock.type), // Normal rock
        listOf(Types.Rock.type,Types.Normal.type),
        listOf(Types.Normal.type,Types.Ghost.type), // Normal ghost
        listOf(Types.Ghost.type,Types.Normal.type),
        listOf(Types.Normal.type,Types.Steel.type), // Normal steel
        listOf(Types.Steel.type,Types.Normal.type),
        listOf(Types.Fire.type,Types.Fairy.type), // Fire fairy
        listOf(Types.Fairy.type,Types.Fire.type),
        listOf(Types.Fire.type,Types.Grass.type), // Fire grass
        listOf(Types.Grass.type,Types.Fire.type),
        listOf(Types.Electric.type,Types.Fighting.type), // Fighting electric
        listOf(Types.Fighting.type,Types.Electric.type),
        listOf(Types.Ice.type,Types.Poison.type), // Ice poison
        listOf(Types.Poison.type,Types.Ice.type),
        listOf(Types.Fighting.type,Types.Ground.type), // Fighting ground
        listOf(Types.Ground.type,Types.Fighting.type),
        listOf(Types.Fighting.type,Types.Fairy.type), // Fighting fairy
        listOf(Types.Fairy.type,Types.Fighting.type),
        listOf(Types.Poison.type,Types.Steel.type), // Steel poison
        listOf(Types.Steel.type,Types.Poison.type),
        listOf(Types.Ground.type,Types.Fairy.type), // Fairy ground
        listOf(Types.Fairy.type,Types.Ground.type),
        listOf(Types.Bug.type,Types.Dragon.type), // Bug dragon
        listOf(Types.Dragon.type,Types.Bug.type),
        listOf(Types.Bug.type,Types.Dark.type), // Bug dark
        listOf(Types.Dark.type,Types.Bug.type),
        listOf(Types.Rock.type,Types.Ghost.type), // Rock ghost
        listOf(Types.Ghost.type,Types.Rock.type)
    )

    // BL
    fun doesThisTypingExist(type1: String, type2: String): Boolean {
        val currentTypingPair: List<String> = listOf(type1, type2)
        return currentTypingPair in listOfNonexistentTypeCombinations
    }

}