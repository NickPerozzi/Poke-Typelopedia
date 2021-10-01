package com.perozzi_package.pokemontypecalculator

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivityViewModel(private val resources: Resources) : ViewModel() {

    // For the grid

    var gridLayoutManager: GridLayoutManager? = null
    var arrayListForTypeGrid: ArrayList<TypeGrid> ? = null
    var typeGridAdapter: TypeGridAdapter ? = null

    // Switch booleans
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    var jiceTime: MutableLiveData<Boolean> = MutableLiveData(false)
    var pogoTime: MutableLiveData<Boolean> = MutableLiveData(false)
    var weAreDefending: MutableLiveData<Boolean> = MutableLiveData(false)

    var attackingType: MutableLiveData<String> = MutableLiveData("(choose)")
    var defendingType1: MutableLiveData<String> = MutableLiveData("(choose)")
    var defendingType2: MutableLiveData<String> = MutableLiveData("[none]")

    var attackingSpinnerOptions = jiceTime.map { jiceTime ->
        val returnedArray: Array<String>
        if (!jiceTime) {
            returnedArray = resources.getStringArray(R.array.spinner_type_options_1)
        } else {
            returnedArray = resources.getStringArray(R.array.spinner_type_options_1)
            returnedArray[12] = resources.getString(R.string.jice)
        }
        returnedArray
    }
    // Identical to attackingSpinnerOptions. Could delete
    var defendingSpinner1Options = jiceTime.map { jiceTime ->
        val returnedArray: Array<String>
        if (!jiceTime) {
            returnedArray = resources.getStringArray(R.array.spinner_type_options_1)
        } else {
            returnedArray = resources.getStringArray(R.array.spinner_type_options_1)
            returnedArray[12] = resources.getString(R.string.jice)
        }
        returnedArray
    }
    var defendingSpinner2Options = jiceTime.map { jiceTime ->
        val returnedArray: Array<String>
        if (!jiceTime) {
            returnedArray = resources.getStringArray(R.array.spinner_type_options_2)
        } else {
            returnedArray = resources.getStringArray(R.array.spinner_type_options_2)
            returnedArray[12] = resources.getString(R.string.jice)
        }
        returnedArray
    }

    var povSwitchText = weAreDefending.map { weAreDefending ->
        if (weAreDefending) {
            resources.getString(R.string.pov_switch_to_defending)
        } else {
            resources.getString(R.string.pov_switch_to_attacking)
        }
    }

    var promptText = weAreDefending.map { weAreDefending ->
        if (weAreDefending) {
            resources.getString(R.string.defending_prompt)
        } else {
            resources.getString(R.string.attacking_prompt)
        }
    }

    var attackingSpinnerVisibility = weAreDefending.map { weAreDefending ->
        if (weAreDefending) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    var defendingSpinnersVisibility = weAreDefending.map { weAreDefending ->
        if (weAreDefending) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    var tableHeaderVisibility = attackingType.switchMap { atk ->
        defendingType1.switchMap { def1 ->
            defendingType2.switchMap { def2 ->
                weAreDefending.map { weAreDefending ->
                    when (weAreDefending) {
                        true -> {
                            if ((def1 == "(choose)" || def1 == Types.NoType.type) &&
                                (def2 == "[none]" || def2 == Types.NoType.type)
                            ) {
                                View.INVISIBLE
                            } else {
                                View.VISIBLE
                            }
                        }
                        false -> {
                            if (atk == "(choose)" || atk == Types.NoType.type) {
                                View.INVISIBLE
                            } else {
                                View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    fun tableHeaderText() = attackingType.switchMap { atk ->
        defendingType1.switchMap { def1 ->
            defendingType2.switchMap { def2 ->
                jiceTime.switchMap { jiceTime ->
                    weAreDefending.map { weAreDefending ->
                        val type1 = if (weAreDefending) {
                            def1
                        } else {
                            atk
                        }
                        val type2 = if (weAreDefending) {
                            def2
                        } else {
                            "[none]"
                        }
                        var type1Displayed = type1.toString()
                        var type2Displayed = type2.toString()
                        if ((type1Displayed == "Ice" || type1Displayed == "Jice")) {
                            type1Displayed = if (jiceTime) {
                                "Jice"
                            } else {
                                "Ice"
                            }
                        }
                        if ((type2Displayed == "Ice" || type2Displayed == "Jice")) {
                            type2Displayed = if (jiceTime) {
                                "Jice"
                            } else {
                                "Ice"
                            }
                        }
                        when (weAreDefending) {
                            true -> {
                                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && (type2Displayed == "[none]" || type2Displayed == Types.NoType.type)) {
                                    resources.getString(
                                        R.string.table_header_one_type,
                                        "_____",
                                        type1Displayed
                                    )
                                }
                                if ((type1Displayed == "(choose)" || type1Displayed == Types.NoType.type) && (type2Displayed != "[none]" && type2Displayed != Types.NoType.type)) {
                                    resources.getString(
                                        R.string.table_header_one_type,
                                        "_____",
                                        type2Displayed
                                    )
                                }
                                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && (type2Displayed != "[none]" && type2Displayed != Types.NoType.type) && type1Displayed != type2Displayed) {
                                    resources.getString(
                                        R.string.table_header_two_types,
                                        "_____",
                                        type1Displayed,
                                        type2Displayed
                                    )
                                }
                                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && type1Displayed == type2Displayed) {
                                    resources.getString(
                                        R.string.table_header_one_type,
                                        "_____",
                                        type1Displayed
                                    )
                                } else {
                                    "Initializer. If you see this then you have encountered a bug. Neat!"
                                }
                            }
                            false -> {
                                resources.getString(
                                    R.string.table_header_one_type,
                                    type1Displayed,
                                    "_____"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // identical to tableHeaderVisibility. These do not intend to change. Can I delete?
    var gameSwitchVisibility = attackingType.switchMap { atk ->
        defendingType1.switchMap { def1 ->
            defendingType2.switchMap { def2 ->
                weAreDefending.map { weAreDefending ->
                    when (weAreDefending) {
                        true -> {
                            if ((def1 == "(choose)" || def1 == Types.NoType.type) &&
                                (def2 == "[none]" || def2 == Types.NoType.type)
                            ) { View.INVISIBLE } else { View.VISIBLE }
                        }
                        false -> {
                            if (atk == "(choose)" || atk == Types.NoType.type) {
                                View.INVISIBLE } else { View.VISIBLE }
                        }
                    }
                }
            }
        }
    }

    var gameSwitchText = pogoTime.map { pogoTime ->
        if (pogoTime) {
            resources.getString(R.string.pogo)
        } else {
            resources.getString(R.string.mainGame)
        }
    }

    // identical to tableHeaderVisibility. These do not intend to change. Can I delete?
    var jiceSwitchVisibility = attackingType.switchMap { atk ->
        defendingType1.switchMap { def1 ->
            defendingType2.switchMap { def2 ->
                weAreDefending.map { weAreDefending ->
                    when (weAreDefending) {
                        true -> {
                            if ((def1 == "(choose)" || def1 == Types.NoType.type) &&
                                (def2 == "[none]" || def2 == Types.NoType.type)
                            ) { View.INVISIBLE } else { View.VISIBLE } }
                        false -> {
                            if (atk == "(choose)" || atk == Types.NoType.type) {
                                View.INVISIBLE } else { View.VISIBLE } }
                    }
                }
            }
        }
    }

    // identical to tableHeaderVisibility. These do not intend to change. Can I delete?
    var recyclerViewVisibility = attackingType.switchMap { atk ->
        defendingType1.switchMap { def1 ->
            defendingType2.switchMap { def2 ->
                weAreDefending.map { weAreDefending ->
                    when (weAreDefending) {
                        true -> {
                            if ((def1 == "(choose)" || def1 == Types.NoType.type) &&
                                (def2 == "[none]" || def2 == Types.NoType.type)
                            ) { View.INVISIBLE } else { View.VISIBLE } }
                        false -> {
                            if (atk == "(choose)" || atk == Types.NoType.type) {
                                View.INVISIBLE } else { View.VISIBLE } }
                    }
                }
            }
        }
    }

    var jiceSwitchText = jiceTime.map { jiceTime ->
        if (jiceTime) {
            resources.getString(R.string.jice)
        } else {
            resources.getString(R.string.ice)
        }
    }

    var doesNotExistVisibility = defendingType1.switchMap { def1 ->
        defendingType2.map { def2 ->
            val currentTypingPair: List<String> = listOf(def1, def2)
            if (currentTypingPair in listOfNonexistentTypeCombinations) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        }
    }

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

    var listOfInteractions: MutableList<Double> = onesDouble()

    //data
    private val listOfNonexistentTypeCombinations: List<List<String>> = listOf(
        listOf(Types.Normal.type, Types.Ice.type), // Normal ice
        listOf(Types.Ice.type, Types.Normal.type),
        listOf(Types.Normal.type, Types.Poison.type), // Normal poison
        listOf(Types.Poison.type, Types.Normal.type),
        listOf(Types.Normal.type, Types.Bug.type), // Normal bug
        listOf(Types.Bug.type, Types.Normal.type),
        listOf(Types.Normal.type, Types.Rock.type), // Normal rock
        listOf(Types.Rock.type, Types.Normal.type),
        listOf(Types.Normal.type, Types.Ghost.type), // Normal ghost
        listOf(Types.Ghost.type, Types.Normal.type),
        listOf(Types.Normal.type, Types.Steel.type), // Normal steel
        listOf(Types.Steel.type, Types.Normal.type),
        listOf(Types.Fire.type, Types.Fairy.type), // Fire fairy
        listOf(Types.Fairy.type, Types.Fire.type),
        listOf(Types.Fire.type, Types.Grass.type), // Fire grass
        listOf(Types.Grass.type, Types.Fire.type),
        listOf(Types.Electric.type, Types.Fighting.type), // Fighting electric
        listOf(Types.Fighting.type, Types.Electric.type),
        listOf(Types.Ice.type, Types.Poison.type), // Ice poison
        listOf(Types.Poison.type, Types.Ice.type),
        listOf(Types.Fighting.type, Types.Ground.type), // Fighting ground
        listOf(Types.Ground.type, Types.Fighting.type),
        listOf(Types.Fighting.type, Types.Fairy.type), // Fighting fairy
        listOf(Types.Fairy.type, Types.Fighting.type),
        listOf(Types.Poison.type, Types.Steel.type), // Steel poison
        listOf(Types.Steel.type, Types.Poison.type),
        listOf(Types.Ground.type, Types.Fairy.type), // Fairy ground
        listOf(Types.Fairy.type, Types.Ground.type),
        listOf(Types.Bug.type, Types.Dragon.type), // Bug dragon
        listOf(Types.Dragon.type, Types.Bug.type),
        listOf(Types.Bug.type, Types.Dark.type), // Bug dark
        listOf(Types.Dark.type, Types.Bug.type),
        listOf(Types.Rock.type, Types.Ghost.type), // Rock ghost
        listOf(Types.Ghost.type, Types.Rock.type)
    )

    private lateinit var typeMatchups: Map<Types, Map<String, Double>>

    fun onesString(): MutableList<String> {
        val table = mutableListOf<String>()
        for (i in 0 until 18) {
            table.add("1.0")
        }
        return table
    }

    private fun onesDouble(): MutableList<Double> {
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

    private fun attackingEffectivenessCalculator(attacker: String): MutableList<Double> {
        if (attacker == "(choose)" || attacker == Types.NoType.type) {
            return onesDouble()
        }

        var dictOfSelectedTypes: Map<String, Double> = emptyMap()

        for (moveType in Types.values()) {
            if (attacker == moveType.type && attacker != Types.NoType.type) {
                dictOfSelectedTypes = typeMatchups.getValue(moveType)
            }
        }
        return dictOfSelectedTypes.values.toMutableList()
    }

    private fun defendingEffectivenessCalculator(defender: String): MutableList<Double> {
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

    private fun defendingWithTwoTypesCalculator(type1: String, type2: String): MutableList<Double> {
        val defenderType1List = defendingEffectivenessCalculator(type1)
        val defenderType2List = defendingEffectivenessCalculator(type2)
        val defenderNetEffectivenessList: MutableList<Double> = mutableListOf()
        // @@@ktg find a way to simplify this
        // Just use PoGo numbers
        // @@@nap believe this comment is dated but there is still room to improve efficiency
        for (i in 0 until 18) {
            val types: List<Double> = listOf(defenderType1List[i], defenderType2List[i])
            when (pogoTime.value) {
                true -> {
                    when (types) {
                        listOf(1.6, 1.6) -> defenderNetEffectivenessList.add(2.56) // same for both
                        listOf(1.6, 1.0) -> defenderNetEffectivenessList.add(1.6) // same for both
                        listOf(1.0, 1.6) -> defenderNetEffectivenessList.add(1.6) // same for both
                        listOf(1.0, 1.0) -> defenderNetEffectivenessList.add(1.0) // same for both
                        listOf(1.6, 0.625) -> defenderNetEffectivenessList.add(1.0) // same for both
                        listOf(0.625, 1.6) -> defenderNetEffectivenessList.add(1.0) // same for both
                        listOf(
                            1.0,
                            0.625
                        ) -> defenderNetEffectivenessList.add(0.625) // same for both
                        listOf(
                            0.625,
                            1.0
                        ) -> defenderNetEffectivenessList.add(0.625) // same for both
                        listOf(
                            1.6,
                            0.390625
                        ) -> defenderNetEffectivenessList.add(0.625) // Pogo-exclusive
                        listOf(
                            0.390625,
                            1.6
                        ) -> defenderNetEffectivenessList.add(0.625) // Pogo-exclusive
                        listOf(
                            0.625,
                            0.625
                        ) -> defenderNetEffectivenessList.add(0.390625) // Pogo-exclusive
                        listOf(
                            1.0,
                            0.390625
                        ) -> defenderNetEffectivenessList.add(0.390625) // Pogo-exclusive
                        listOf(
                            0.390625,
                            1.0
                        ) -> defenderNetEffectivenessList.add(0.390625) // Pogo-exclusive
                        listOf(
                            0.625,
                            0.390625
                        ) -> defenderNetEffectivenessList.add(0.244) // Pogo-exclusive
                        listOf(
                            0.390625,
                            0.625
                        ) -> defenderNetEffectivenessList.add(0.244) // Pogo-exclusive
                    }
                }
                false -> {
                    when (types) {
                        listOf(1.6, 1.6) -> defenderNetEffectivenessList.add(2.56) // same for both
                        listOf(1.6, 1.0) -> defenderNetEffectivenessList.add(1.6) // same for both
                        listOf(1.0, 1.6) -> defenderNetEffectivenessList.add(1.6) // same for both
                        listOf(1.0, 1.0) -> defenderNetEffectivenessList.add(1.0) // same for both
                        listOf(1.6, 0.625) -> defenderNetEffectivenessList.add(1.0) // same for both
                        listOf(0.625, 1.6) -> defenderNetEffectivenessList.add(1.0) // same for both
                        listOf(
                            1.0,
                            0.625
                        ) -> defenderNetEffectivenessList.add(0.625) // same for both
                        listOf(
                            0.625,
                            1.0
                        ) -> defenderNetEffectivenessList.add(0.625) // same for both
                        listOf(
                            0.625,
                            0.625
                        ) -> defenderNetEffectivenessList.add(0.25) // main game-exclusive
                        listOf(
                            1.6,
                            0.390625
                        ) -> defenderNetEffectivenessList.add(0.0) // main game-exclusive
                        listOf(
                            0.390625,
                            1.6
                        ) -> defenderNetEffectivenessList.add(0.0) // main game-exclusive
                        listOf(
                            1.0,
                            0.390625
                        ) -> defenderNetEffectivenessList.add(0.0) // main game-exclusive
                        listOf(
                            0.390625,
                            1.0
                        ) -> defenderNetEffectivenessList.add(0.0) // main game-exclusive
                        listOf(
                            0.625,
                            0.390625
                        ) -> defenderNetEffectivenessList.add(0.0) // main game-exclusive
                        listOf(
                            0.390625,
                            0.625
                        ) -> defenderNetEffectivenessList.add(0.0) // main game-exclusive
                    }
                }
            }
        }
        return (defenderNetEffectivenessList)
    }

    private fun interactionsToEffectiveness(mutableList: MutableList<Double>): MutableList<String> {
        val stringList: MutableList<String> = mutableListOf()
        for (i in 0 until 18) {
            if (pogoTime.value == true) {
                when (mutableList[i]) {
                    2.56 -> stringList.add(Effectiveness.ULTRA_SUPER_EFFECTIVE.impact)
                    1.6 -> stringList.add(Effectiveness.SUPER_EFFECTIVE.impact)
                    1.0 -> stringList.add(Effectiveness.EFFECTIVE.impact)
                    0.625 -> stringList.add(Effectiveness.NOT_VERY_EFFECTIVE.impact)
                    0.390625 -> stringList.add(Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact)
                    0.244 -> stringList.add(Effectiveness.ULTRA_DOES_NOT_EFFECT.impact)
                }
            } else {
                when (mutableList[i]) {
                    2.56 -> stringList.add(Effectiveness.ULTRA_SUPER_EFFECTIVE.impact)
                    1.6 -> stringList.add(Effectiveness.SUPER_EFFECTIVE.impact)
                    1.0 -> stringList.add(Effectiveness.EFFECTIVE.impact)
                    0.625 -> stringList.add(Effectiveness.NOT_VERY_EFFECTIVE.impact)
                    0.25 -> stringList.add(Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact)
                    0.390625 -> stringList.add(Effectiveness.DOES_NOT_EFFECT.impact)
                    0.0 -> stringList.add(Effectiveness.DOES_NOT_EFFECT.impact)
                }
            }
        }
        return stringList
    }

    @SuppressLint("SetTextI18n")
    fun effectivenessToDisplayedCellValues(listOfEffectivenesses: MutableList<String>): MutableList<String> {
        val mutableListOfEffectivenessDoubles: MutableList<String> = mutableListOf()
        for (i in 0 until 18) {
            if (pogoTime.value == false) {
                when (listOfEffectivenesses[i]) {
                    Effectiveness.EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x1")
                    Effectiveness.SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x2")
                    Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(
                        "x4"
                    )
                    Effectiveness.NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(
                        "x0.5"
                    )
                    Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(
                        "x0.25"
                    )
                    Effectiveness.DOES_NOT_EFFECT.impact -> mutableListOfEffectivenessDoubles.add("x0")
                }
            } else {
                when (listOfEffectivenesses[i]) {
                    Effectiveness.EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x1")
                    Effectiveness.SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x1.6")
                    Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(
                        "x2.56"
                    )
                    Effectiveness.NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(
                        "x0.625"
                    )
                    Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(
                        "x0.391"
                    )
                    Effectiveness.ULTRA_DOES_NOT_EFFECT.impact -> mutableListOfEffectivenessDoubles.add(
                        "x0.244"
                    )
                }
            }
        }
        return mutableListOfEffectivenessDoubles
    }

    fun setDataInTypeGridList(
        iconMutableList: MutableList<Int>, effectivenessMutableList: MutableList<String>,
        backgroundColorList: MutableList<Int>, textColorList: MutableList<Int>
    ):
            ArrayList<TypeGrid> {

        val items: ArrayList<TypeGrid> = ArrayList()
        for (i in 0 until 18) {
            items.add(
                TypeGrid(
                    iconMutableList[i],
                    effectivenessMutableList[i],
                    backgroundColorList[i],
                    textColorList[i]
                )
            )
        }

        // Determines whether to add "Ice" or "Jice" icon

        // TODO(eventually not need these lines of code)
        items[11] = if (jiceTime.value!!) {
            TypeGrid(
                R.drawable.jice_icon,
                effectivenessMutableList[11],
                backgroundColorList[11],
                textColorList[11]
            )
        } else {
            TypeGrid(
                R.drawable.ice_icon,
                effectivenessMutableList[11],
                backgroundColorList[11],
                textColorList[11]
            )
        }
        return items
    }

    private fun interactionsToGridView(interactionsList: MutableList<Double>) {
        val effectivenessList = interactionsToEffectiveness(interactionsList)
        val displayedListOfInteractions =
            effectivenessToDisplayedCellValues(effectivenessList)
        val listOfCellTextColors =
            effectivenessToCellTextColors(effectivenessList)
        val listOfCellBackgroundColors =
            effectivenessToCellBackgroundColors(effectivenessList)
        arrayListForTypeGrid = setDataInTypeGridList(
            arrayOfTypeIcons,
            displayedListOfInteractions,
            listOfCellBackgroundColors,
            listOfCellTextColors
        )
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
    }

    fun refreshTheData() {
        when (weAreDefending.value) {
            false -> {
                // if attacking
                listOfInteractions =
                    attackingEffectivenessCalculator(attackingType.value!!)
            }
            true -> {
                listOfInteractions =
                        // if defending with type 1 only
                    if (defendingType2.value == "[none]" || defendingType2.value == Types.NoType.type || defendingType1 == defendingType2) {
                        defendingEffectivenessCalculator(defendingType1.value!!)
                        // if defending with type 2 only
                    } else if (defendingType1.value == "(choose)" || defendingType1.value == Types.NoType.type) {
                        defendingEffectivenessCalculator(defendingType2.value!!)
                        // if defending with both type1 and type 2
                    } else {
                        defendingWithTwoTypesCalculator(
                            defendingType1.value!!,
                            defendingType2.value!!
                        )
                    }
            }
        }
        interactionsToGridView(listOfInteractions)
    }

    private fun effectivenessToCellTextColors(mutableList: MutableList<String>): MutableList<Int> {
        val listOfCellTextColors: MutableList<Int> = mutableListOf()
        for (i in 0 until 18) {
            if ((mutableList[i] == Effectiveness.DOES_NOT_EFFECT.impact) || (mutableList[i] == Effectiveness.ULTRA_DOES_NOT_EFFECT.impact)) {
                listOfCellTextColors.add(R.color.white)
                // listOfCellTextColors.add(resources.getColor(R.color.white))
            } else {
                listOfCellTextColors.add(R.color.black)
                // listOfCellTextColors.add(resources.getColor(R.color.black))
            }
        }
        return listOfCellTextColors
    }

    private fun effectivenessToCellBackgroundColors(mutableList: MutableList<String>): MutableList<Int> {
        val listOfCellBackgroundColors: MutableList<Int> = mutableListOf()
        for (i in 0 until 18) {
            when (mutableList[i]) {
                Effectiveness.EFFECTIVE.impact -> listOfCellBackgroundColors.add(R.color.x1color)
                Effectiveness.SUPER_EFFECTIVE.impact -> listOfCellBackgroundColors.add(R.color.x2color)
                Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> listOfCellBackgroundColors.add(R.color.x4color)
                Effectiveness.NOT_VERY_EFFECTIVE.impact -> listOfCellBackgroundColors.add(R.color.x_5color)
                Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> listOfCellBackgroundColors.add(R.color.x_25color)
                Effectiveness.DOES_NOT_EFFECT.impact -> listOfCellBackgroundColors.add(R.color.x0color)
                Effectiveness.ULTRA_DOES_NOT_EFFECT.impact -> listOfCellBackgroundColors.add(R.color.UDNEcolor)
            }
        }
        return listOfCellBackgroundColors
    }

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

            override fun onFailure(call: Call, e: IOException) {}
        })
    }

// TODO(work on doesNotExistDisclaimer stuff after liveData)
/*
    private lateinit var masterURLFetch: Map<String,List<Map<String,String>>>
    private lateinit var pokemonURLFetch: Map<String,Any>
    private lateinit var pokemonNamesAndURLs: List<Map<String,String>>
    lateinit var listOfPossibleTypes: MutableList<MutableList<String>>
    private lateinit var pokemonNames: MutableList<String>
    private lateinit var pokemonURLs: MutableList<String>

    fun fetchAllPokemonNamesAndURLs() {
        val masterURL = "https://pokeapi.co/api/v2/pokemon?limit=898"
        val request = Request.Builder().url(masterURL).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                val typeToken: Type =
                    object : TypeToken<Map<String,List<Map<String,String>>>>() {}.type
                masterURLFetch = gson.fromJson(body, typeToken)
                pokemonNamesAndURLs = masterURLFetch.getValue("results")
                for (i in pokemonNamesAndURLs) {
                    pokemonNames.add(i.getValue("name"))
                    pokemonURLs.add(i.getValue("url"))
                }
            }
            override fun onFailure(call: Call, e: IOException) {}
        })
    }
    fun fetchAllTypingPossibilities() {
        for (url in pokemonURLs) {
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()
            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val gson = GsonBuilder().create()
                    val typeToken: Type =
                        object : TypeToken<Map<String,Any>>() {}.type
                    pokemonURLFetch = gson.fromJson(body, typeToken)
                    val pokemonTypesList: List<Map<String, Any>> =
                        pokemonURLFetch.getValue("types") as List<Map<String, Any>>
                    val typeCombo: MutableList<String> = mutableListOf()
                    val typeComboFlipped: MutableList<String> = mutableListOf()
                    val type1 = pokemonTypesList[0].getValue("type") as String
                    val type2 = if (pokemonTypesList.size == 2) {
                        pokemonTypesList[1].getValue("type") as String
                    } else { "(none)" }
                    typeCombo.add(type1)
                    typeCombo.add(type2)
                    typeComboFlipped.add(type2)
                    typeComboFlipped.add(type1)
                    if (typeCombo !in listOfPossibleTypes && typeComboFlipped !in listOfPossibleTypes) {
                        listOfPossibleTypes.add(typeCombo)
                        listOfPossibleTypes.add(typeComboFlipped)
                    }
                }
                override fun onFailure(call: Call, e: IOException) {}
            })
        }
    }*/
}
