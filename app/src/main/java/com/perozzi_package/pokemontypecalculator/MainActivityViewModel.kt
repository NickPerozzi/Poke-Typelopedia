package com.perozzi_package.pokemontypecalculator

import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type

class MainActivityViewModel: ViewModel() {

    var pogoTime = false
    var arrayOfTypeIcons: MutableList<Int> = mutableListOf(
        R.drawable.bug_icon,
        R.drawable.dark_icon,
        R.drawable.dragon_icon,
        R.drawable.electric_icon,
        R.drawable.fairy_icon,
        R.drawable.fighting_icon,
        R.drawable.fire_icon,
        R.drawable.flying_icon,
        R.drawable.ghost_icon,
        R.drawable.grass_icon,
        R.drawable.ground_icon,
        R.drawable.ice_icon,
        R.drawable.normal_icon,
        R.drawable.poison_icon,
        R.drawable.psychic_icon,
        R.drawable.rock_icon,
        R.drawable.steel_icon,
        R.drawable.water_icon
    )

    private val listOfNonexistentTypeCombinations: List<List<String>> = listOf(
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

    private lateinit var typeMatchups: Map<Types, Map<String, Double>>

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


    fun doesThisTypingExist(type1: String, type2: String): Boolean {
        val currentTypingPair: List<String> = listOf(type1, type2)
        return currentTypingPair in listOfNonexistentTypeCombinations
    }

    // BL
    fun fetchJson() {
        val url = "https://pogoapi.net/api/v1/type_effectiveness.json"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                val typeToken: Type =
                    object : TypeToken<Map<Types, Map<String, Double>>>() {}.type
                typeMatchups = gson.fromJson(body, typeToken)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to call request")
            }
        })
    }

    fun attackingEffectivenessCalculator(attacker: String): MutableList<Double> {
        if (attacker == "(choose)" || attacker == Types.NoType.type) { return onesDouble() }

        var dictOfSelectedTypes: Map<String, Double> = emptyMap()

        for (moveType in Types.values()) {
            if (attacker == moveType.type && attacker != Types.NoType.type) {
                dictOfSelectedTypes = typeMatchups.getValue(moveType)
            }
        }
        return dictOfSelectedTypes.values.toMutableList()
    }
    fun defendingEffectivenessCalculator(defender: String): MutableList<Double> {
        if (defender == "(choose)" || defender == "[none]" || defender == Types.NoType.type) {
            return onesDouble()
        }
        var dictOfSelectedTypes: Map<String, Double>
        val listOfDefendingMatchupCoefficients: MutableList<Double> = arrayListOf()
        for (moveType in Types.values()) {
            if (moveType != Types.NoType) {
                dictOfSelectedTypes = typeMatchups.getValue(moveType)
                dictOfSelectedTypes[defender]?.let { listOfDefendingMatchupCoefficients.add(it) }
            }
        }
        return listOfDefendingMatchupCoefficients
    }
    fun defendingWithTwoTypesCalculator(type1: String, type2: String): MutableList<Double> {
        val defenderType1List = defendingEffectivenessCalculator(type1)
        val defenderType2List = defendingEffectivenessCalculator(type2)
        val defenderNetEffectivenessList: MutableList<Double> = mutableListOf()
        // @@@ktg find a way to simplify this
        // Just use PoGo numbers
        // @@@nap believe this comment is dated but there is still room to improve efficiency
        for (i in 0 until 18) {
            val types: List<Double> = listOf(defenderType1List[i],defenderType2List[i])
            when (pogoTime) {
                true -> {
                    when (types) {
                        listOf(1.6, 1.6) -> defenderNetEffectivenessList.add(2.56)
                        listOf(1.6, 1.0) -> defenderNetEffectivenessList.add(1.6)
                        listOf(1.0, 1.6) -> defenderNetEffectivenessList.add(1.6)
                        listOf(1.0, 1.0) -> defenderNetEffectivenessList.add(1.0)
                        listOf(1.6, 0.625) -> defenderNetEffectivenessList.add(1.0)
                        listOf(0.625, 1.6) -> defenderNetEffectivenessList.add(1.0)
                        listOf(1.0, 0.625) -> defenderNetEffectivenessList.add(0.625)
                        listOf(0.625, 1.0) -> defenderNetEffectivenessList.add(0.625)
                        listOf(1.6, 0.390625) -> defenderNetEffectivenessList.add(0.625)
                        listOf(0.390625, 1.6) -> defenderNetEffectivenessList.add(0.625)
                        listOf(0.625, 0.625) -> defenderNetEffectivenessList.add(0.390625)
                        listOf(1.0, 0.390625) -> defenderNetEffectivenessList.add(0.390625)
                        listOf(0.390625, 1.0) -> defenderNetEffectivenessList.add(0.390625)
                        listOf(0.625, 0.390625) -> defenderNetEffectivenessList.add(0.244)
                        listOf(0.390625, 0.625) -> defenderNetEffectivenessList.add(0.244)
                    }
                }
                false -> {
                    when (types) {
                        listOf(1.6, 1.6) -> defenderNetEffectivenessList.add(2.56)
                        listOf(1.6, 1.0) -> defenderNetEffectivenessList.add(1.6)
                        listOf(1.0, 1.6) -> defenderNetEffectivenessList.add(1.6)
                        listOf(1.0, 1.0) -> defenderNetEffectivenessList.add(1.0)
                        listOf(1.6, 0.625) -> defenderNetEffectivenessList.add(1.0)
                        listOf(0.625, 1.6) -> defenderNetEffectivenessList.add(1.0)
                        listOf(1.0, 0.625) -> defenderNetEffectivenessList.add(0.625)
                        listOf(0.625, 1.0) -> defenderNetEffectivenessList.add(0.625)
                        listOf(0.625, 0.625) -> defenderNetEffectivenessList.add(0.25)
                        listOf(1.6, 0.390625) -> defenderNetEffectivenessList.add(0.0)
                        listOf(0.390625, 1.6) -> defenderNetEffectivenessList.add(0.0)
                        listOf(1.0, 0.390625) -> defenderNetEffectivenessList.add(0.0)
                        listOf(0.390625, 1.0) -> defenderNetEffectivenessList.add(0.0)
                        listOf(0.625, 0.390625) -> defenderNetEffectivenessList.add(0.0)
                        listOf(0.390625, 0.625) -> defenderNetEffectivenessList.add(0.0)
                    }
                }
            }
        }
        return (defenderNetEffectivenessList)
    }

}