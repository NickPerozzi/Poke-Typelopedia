package com.perozzi_package.pokemontypecalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type

//class MainActivity(val TypeMatchups: TypeMatchupsClass) : AppCompatActivity() {
@SuppressLint("UseSwitchCompatOrMaterialCode")
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null // needed for gridView functionality
    private var gridLayoutManager: GridLayoutManager? = null // needed for gridView functionality
    private var arrayListForTypeGrid:ArrayList<TypeGrid> ? = null // needed for gridView functionality
    private var typeGridAdapter:TypeGridAdapter ? = null // needed for gridView functionality

    private lateinit var typeMatchups: Map<PokemonType, Map<String, Double>> // used by attackingEffectivenessCalculator()
    private var defendingSpinnerType1Options = arrayOf<String>() // used by attackingEffectivenessCalculator()
    private var attackingSpinnerTypeOptions = arrayOf<String>() // used by attackingEffectivenessCalculator()

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var gameSwitch: Switch // used by defendingWithTwoTypes()

    private lateinit var iceJiceSwitch: Switch // used by setDataInList()
    private var jiceTime = false

    private lateinit var doesNotExistDisclaimer: TextView // used by makeVisibleIfTypeSelected()

    private var weAreDefending = false // used by adjustMoveSpinnerAndLabelVisibility()
    private lateinit var attackingTypeSpinnerAndLabel: LinearLayout
    private lateinit var defendingType1SpinnerAndLabel: LinearLayout
    private lateinit var defendingType2SpinnerAndLabel: LinearLayout

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Hides top bar

        val binding =
            ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root) // Sets up bindings

        // Night mode compatibility
        val mainLinearLayout = binding.mainLinearLayout
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mainLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector_night)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mainLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector)
            }
        }

        // Bindings
        val defendingType1Spinner = binding.type1Spinner
        val defendingType2Spinner = binding.type2Spinner
        val attackingTypeSpinner = binding.attackingTypeSpinner
        val typeSelectionPrompt = binding.secondPrompt
        val tableHeader = binding.tableHeader
        doesNotExistDisclaimer = binding.doesNotExistDisclaimer
        attackingTypeSpinnerAndLabel = binding.attackingTypeSpinnerAndLabel
        defendingType1SpinnerAndLabel = binding.defendingType1SpinnerAndLabel
        defendingType2SpinnerAndLabel = binding.defendingType2SpinnerAndLabel
        val povSwitch = binding.povSwitch
        gameSwitch = binding.gameSwitch
        iceJiceSwitch = binding.iceJiceSwitch
        val typeTableRecyclerView = binding.typeTableRecyclerView
        val infoButton = binding.infoButton

        // Populates spinner options
        attackingSpinnerTypeOptions = resources.getStringArray(R.array.spinner_type_options_1)
        setupSpinner(attackingSpinnerTypeOptions, attackingTypeSpinner)
        defendingSpinnerType1Options = resources.getStringArray(R.array.spinner_type_options_1)
        setupSpinner(defendingSpinnerType1Options, defendingType1Spinner)
        val defendingSpinnerType2Options = resources.getStringArray(R.array.spinner_type_options_2)
        setupSpinner(defendingSpinnerType2Options, defendingType2Spinner)

        fetchJson() //gets .json file

        // Initializes the gridView
        val listOfCellBackgroundColors: MutableList<Int> = onesInt()
        val listOfCellTextColors: MutableList<Int> = onesInt()
        recyclerView = findViewById(R.id.typeTableRecyclerView)
        gridLayoutManager = GridLayoutManager(applicationContext, 3, LinearLayoutManager.VERTICAL,false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(true)
        arrayListForTypeGrid = ArrayList()
        arrayListForTypeGrid = setDataInTypeGridList(arrayOfIcons,onesDouble(),listOfCellBackgroundColors,listOfCellTextColors)
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
        recyclerView?.adapter = typeGridAdapter


        weAreDefending = false
        var attackingType = 0
        var defendingType1 = 0
        var defendingType2 = 0
        var listOfInteractions: MutableList<Double> = onesDouble()
        var listOfInteractionsDualDefender: MutableList<String> = onesString()

        //var effectivenessList: MutableList<String> = interactionsToEffectiveness(onesDouble())

        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            weAreDefending = onSwitch
            adjustTypeSpinnersVisibility()

            when (weAreDefending) {
                false -> {
                    if (defendingType1 != 0) {
                        attackingType = defendingType1
                        attackingTypeSpinner.setSelection(defendingType1)
                    }
                    if (defendingType1 == 0 && defendingType2 != 0) {
                        attackingType = defendingType2
                        attackingTypeSpinner.setSelection(defendingType1)
                    }

                    povSwitch.text = getString(R.string.pov_switch_to_attacking)
                    typeSelectionPrompt.text = resources.getString(R.string.attacking_prompt)
                    adjustTableHeaderText(tableHeader,attackingType)
                    adjustVisibility(doesNotExistDisclaimer,1)
                }
                true -> {
                    defendingType2 = 0
                    defendingType2Spinner.setSelection(0)
                    defendingType1 = attackingType
                    defendingType1Spinner.setSelection(attackingType)
                    povSwitch.text = getString(R.string.pov_switch_to_defending)
                    typeSelectionPrompt.text = resources.getString(R.string.defending_prompt)
                    adjustTableHeaderText(tableHeader,defendingType1,defendingType2)
                }
            }
            if (!weAreDefending) {
                listOfInteractions = attackingEffectivenessCalculator(attackingType)
                interactionsToGridView(listOfInteractions)
            }
            if (weAreDefending) {
                if (defendingType1 != 0 && defendingType2 != 0 && defendingType1 != defendingType2) {
                    interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
                } else {
                    listOfInteractions = if (defendingType1 == 0) {
                        defendingEffectivenessCalculator(defendingType2)
                    } else {
                        defendingEffectivenessCalculator(defendingType1)
                    }
                    interactionsToGridView(listOfInteractions)
                }
            }
        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                attackingType = p2

                // Table header text adjustment
                adjustTableHeaderText(tableHeader,attackingType)

                // Adjusts visibility depending on whether user has selected a type yet
                makeVisibleIfTypeSelected(typeTableRecyclerView,attackingType)
                makeVisibleIfTypeSelected(gameSwitch,attackingType)
                makeVisibleIfTypeSelected(iceJiceSwitch,attackingType)
                makeVisibleIfTypeSelected(tableHeader,attackingType)

                // Gets the values
                listOfInteractions = attackingEffectivenessCalculator(attackingType)

                // Makes the values show in GridView (multiple nested functions)
                interactionsToGridView(listOfInteractions)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // TODO @@@ktg duplicate code = function-ize
            // @@@nap functionized much of it, some parts still duplicate but not sure if it's unavoidable/too pyrrhic to fix
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                defendingType1 = p2

                // Table header text adjustment
                adjustTableHeaderText(tableHeader,defendingType1,defendingType2)

                // Adjusts visibility depending on whether user has selected a type yet
                checkIfTypingExists(defendingType1,defendingType2)
                makeVisibleIfTypeSelected(tableHeader,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(typeTableRecyclerView,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitch,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(iceJiceSwitch,defendingType1,defendingType2)

                // Gets values and shows them in GridView if only one type is selected
                if (defendingType2 == 0 || defendingType1 == defendingType2) {
                    listOfInteractions = defendingEffectivenessCalculator(defendingType1)
                    interactionsToGridView(listOfInteractions)
                }

                // Gets values and shows them in GridView if more than one type is selected
                if (defendingType2 != 0 && defendingType1 != defendingType2) {
                    listOfInteractionsDualDefender = defendingWithTwoTypesCalculator(defendingType1,defendingType2)
                    interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Called when user selects an option in the second type spinner
        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                defendingType2 = p2

                // Table header text adjustment
                adjustTableHeaderText(tableHeader,defendingType1,defendingType2)

                // Adjusts visibility depending on whether user has selected a type yet
                checkIfTypingExists(defendingType1,defendingType2)
                makeVisibleIfTypeSelected(tableHeader,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(typeTableRecyclerView,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitch,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(iceJiceSwitch,defendingType1,defendingType2)

                if (defendingType2 == 0 || defendingType1 == defendingType2) {
                    listOfInteractions = defendingEffectivenessCalculator(defendingType1)
                    interactionsToGridView(listOfInteractions)

                }
                if (defendingType2 != 0 && defendingType1 != defendingType2) {
                    listOfInteractionsDualDefender = defendingWithTwoTypesCalculator(defendingType1,defendingType2)
                    interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            run {
                when (weAreDefending) {
                    false -> {
                        listOfInteractions =
                            attackingEffectivenessCalculator(attackingType)
                    }
                    true -> {
                        if (defendingType1 != 0 && defendingType2 != 0 && defendingType1 != defendingType2) {
                            listOfInteractionsDualDefender =
                                defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                            interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
                        }
                        listOfInteractions = if (defendingType1 != 0 && (defendingType1 == defendingType2 || defendingType2 == 0)) {
                            defendingEffectivenessCalculator(defendingType1)
                        } else {
                            defendingEffectivenessCalculator(defendingType2)
                        }
                        interactionsToGridView(listOfInteractions)
                    }
                }
            }

            // Changes the switch's text between "Pokémon GO and Main Game)
            if (onSwitch) {
                gameSwitch.text = resources.getString((R.string.pogo))
            } else {
                gameSwitch.text = resources.getString((R.string.mainGame))
            }

            // Sends information to gridView depending on whether dual type is selected or not
            if (weAreDefending && defendingType1 !=0 && defendingType2 != 0 && defendingType1 != defendingType2) {
                interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
            } else {
                interactionsToGridView(listOfInteractions)
            }
        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            jiceTime = onSwitch
            if (onSwitch) {
                iceJiceSwitch.text = getString(R.string.jice)

                // Switching Ice to Jice in the spinner values is actually a very involved task so
                /*attackingSpinnerTypeOptions[12] = getString(R.string.jice)
                defendingSpinnerType1Options[12] = getString(R.string.jice)
                val defendingSpinnerType2Options = resources.getStringArray(R.array.spinner_type_options_2)
                defendingSpinnerType2Options[12] = getString(R.string.jice)*/
            }
            else {
                iceJiceSwitch.text = getString(R.string.ice)
                /*attackingSpinnerTypeOptions[12] = getString(R.string.ice)
                defendingSpinnerType1Options[12] = getString(R.string.ice)
                val defendingSpinnerType2Options = resources.getStringArray(R.array.spinner_type_options_2)
                defendingSpinnerType2Options[12] = getString(R.string.ice)*/

            }
            /*setupSpinner(attackingSpinnerTypeOptions, attackingTypeSpinner)
            setupSpinner(defendingSpinnerType1Options, defendingType1Spinner)
            setupSpinner(defendingSpinnerType2Options, defendingType2Spinner)*/

            // Sends information to gridView depending on whether dual type is selected or not
            if (!weAreDefending) {
                interactionsToGridView(listOfInteractions)
                adjustTableHeaderText(tableHeader,attackingType)
            }
            if (weAreDefending) {
                if (defendingType1 != 0 && defendingType2 != 0 && defendingType1 != defendingType2) {
                    interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
                } else {
                    listOfInteractions = if (defendingType1 == 0) {
                        defendingEffectivenessCalculator(defendingType2)
                    } else {
                        defendingEffectivenessCalculator(defendingType1)
                    }
                    interactionsToGridView(listOfInteractions)
                }
                adjustTableHeaderText(tableHeader,defendingType1,defendingType2)
            }
        }

        infoButton.setOnClickListener {
            val intent = Intent(this, TypeTriviaActivity::class.java)
            startActivity(intent)
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////     End of onCreate     ///////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////



    private fun interactionsToGridView(interactionsList: MutableList<Double>) {
        val effectivenessList = interactionsToEffectiveness(interactionsList)
        val displayedListOfInteractions = effectivenessToDisplayedCellValues(effectivenessList)
        val listOfCellTextColors = effectivenessToCellTextColors(effectivenessList)
        val listOfCellBackgroundColors = effectivenessToCellBackgroundColors(effectivenessList)
        arrayListForTypeGrid = setDataInTypeGridList(arrayOfIcons,displayedListOfInteractions,listOfCellBackgroundColors,listOfCellTextColors)
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
        recyclerView?.adapter = typeGridAdapter
    }

    private fun interactionsToEffectiveness(mutableList: MutableList<Double>): MutableList<String> {
        val stringList: MutableList<String> = mutableListOf()
        for (i in 0 until 18) {
            when (mutableList[i]) {
                1.6 -> stringList.add(Effectiveness.SUPER_EFFECTIVE.impact)
                1.0 -> stringList.add(Effectiveness.EFFECTIVE.impact)
                0.625 -> stringList.add(Effectiveness.NOT_VERY_EFFECTIVE.impact)
            }
            if (gameSwitch.isChecked) {
                when (mutableList[i]) {
                    0.390625 -> stringList.add(Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact)
                }
            } else {
                when (mutableList[i]) {
                    0.390625 -> stringList.add(Effectiveness.DOES_NOT_EFFECT.impact)
                }
            }
        }
        return stringList
    }

    private fun adjustTypeSpinnersVisibility() {
        when (weAreDefending) {
            false -> {
                adjustVisibility(attackingTypeSpinnerAndLabel, 0)
                adjustVisibility(defendingType1SpinnerAndLabel, 2)
                adjustVisibility(defendingType2SpinnerAndLabel, 2)
            }
            true -> {
                adjustVisibility(attackingTypeSpinnerAndLabel, 2)
                adjustVisibility(defendingType1SpinnerAndLabel, 0)
                adjustVisibility(defendingType2SpinnerAndLabel, 0)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun effectivenessToDisplayedCellValues(listOfEffectivenesses: MutableList<String>): MutableList<Double> {
        val mutableListOfEffectivenessDoubles: MutableList<Double> = mutableListOf()
        for (i in 0 until 18) {
            if (!gameSwitch.isChecked) {
                when (listOfEffectivenesses[i]) {
                    Effectiveness.EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(1.0)
                    Effectiveness.SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(2.0)
                    Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(4.0)
                    Effectiveness.NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(0.5)
                    Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(0.25)
                    Effectiveness.DOES_NOT_EFFECT.impact -> mutableListOfEffectivenessDoubles.add(0.0)
                }
            } else {
                when (listOfEffectivenesses[i]) {
                    Effectiveness.EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(1.0)
                    Effectiveness.SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(1.6)
                    Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(2.56)
                    Effectiveness.NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(0.625)
                    Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add(0.39)
                    Effectiveness.ULTRA_DOES_NOT_EFFECT.impact -> mutableListOfEffectivenessDoubles.add(0.244)
                }
            }
        }
        return mutableListOfEffectivenessDoubles
    }

    private fun effectivenessToCellTextColors(mutableList: MutableList<String>): MutableList<Int> {
        val listOfCellTextColors: MutableList<Int> = mutableListOf()
        for (i in 0 until 18) {
            if ((mutableList[i] == Effectiveness.DOES_NOT_EFFECT.impact) || (mutableList[i] == Effectiveness.ULTRA_DOES_NOT_EFFECT.impact)) {
                listOfCellTextColors.add(getColor(R.color.white))
            } else {
                listOfCellTextColors.add(getColor(R.color.black))
            }
        }
        return listOfCellTextColors
    }

    private fun effectivenessToCellBackgroundColors(mutableList: MutableList<String>): MutableList<Int> {
        val listOfCellBackgroundColors: MutableList<Int> = mutableListOf()
        for (i in 0 until 18) {
            when (mutableList[i]) {
                Effectiveness.EFFECTIVE.impact -> listOfCellBackgroundColors.add(getColor(R.color.x1color))
                Effectiveness.SUPER_EFFECTIVE.impact -> listOfCellBackgroundColors.add(getColor(R.color.x2color))
                Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> listOfCellBackgroundColors.add(getColor(R.color.x4color))
                Effectiveness.NOT_VERY_EFFECTIVE.impact -> listOfCellBackgroundColors.add(getColor(R.color.x_5color))
                Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> listOfCellBackgroundColors.add(getColor(R.color.x_25color))
                Effectiveness.DOES_NOT_EFFECT.impact -> listOfCellBackgroundColors.add(getColor(R.color.x0color))
                Effectiveness.ULTRA_DOES_NOT_EFFECT.impact -> listOfCellBackgroundColors.add(getColor(R.color.UDNEcolor))
            }
        }
        return listOfCellBackgroundColors
    }

    private fun setDataInTypeGridList(iconMutableList: MutableList<Int>, effectivenessMutableList:MutableList<Double>,
                                      backgroundColorList: MutableList<Int>, textColorList: MutableList<Int>):
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
        items[11] = if (jiceTime) {
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

    private fun interactionsToGridViewDualDefender(interactionsList: MutableList<String>) {
        val displayedListOfInteractions = effectivenessToDisplayedCellValues(interactionsList)
        val listOfCellTextColors = effectivenessToCellTextColors(interactionsList)
        val listOfCellBackgroundColors = effectivenessToCellBackgroundColors(interactionsList)
        arrayListForTypeGrid = setDataInTypeGridList(arrayOfIcons,displayedListOfInteractions,listOfCellBackgroundColors,listOfCellTextColors)
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
        recyclerView?.adapter = typeGridAdapter
    }

    private fun adjustVisibility(selectedTextView: View, visibleInvisibleGone: Int) {
        when (visibleInvisibleGone) {
            0 -> selectedTextView.visibility = View.VISIBLE
            1 -> selectedTextView.visibility = View.INVISIBLE
            2 -> selectedTextView.visibility = View.GONE
        }
    }

    private fun defendingWithTwoTypesCalculator(type1: Int, type2: Int): MutableList<String> {
        val defenderType1List = defendingEffectivenessCalculator(type1)
        val defenderType2List = defendingEffectivenessCalculator(type2)
        val defenderNetListOfStrings: MutableList<String> = arrayListOf()
        // @@@ktg find a way to simplify this
        // Just use PoGo numbers
        for (i in 0 until 18) {

            // Ultra super effective
            // (4x or 2.56x)
            // (1 possible permutation)
            if ((defenderType1List[i] == 1.6) && (defenderType2List[i] == 1.6)) {
                defenderNetListOfStrings.add(Effectiveness.ULTRA_SUPER_EFFECTIVE.impact)
            }

            // Super effective
            // (2x or 1.6x)
            // (2 possible permutations)
            if ((defenderType1List[i] == 1.6) && (defenderType2List[i] == 1.0)
                || (defenderType1List[i] == 1.0) && (defenderType2List[i] == 1.6)
            ) {
                defenderNetListOfStrings.add(Effectiveness.SUPER_EFFECTIVE.impact)
            }

            // Effective
            // (1x or 1x)
            // (3 possible permutations)
            if ((defenderType1List[i] == 1.6) && (defenderType2List[i] == 0.625)
                || (defenderType1List[i] == 1.0) && (defenderType2List[i] == 1.0)
                || (defenderType1List[i] == 0.625) && (defenderType2List[i] == 1.6)
            ) {
                defenderNetListOfStrings.add(Effectiveness.EFFECTIVE.impact)
            }

            // Not very effective
            // (.5x or .625x)
            // (2 permutations in main game, 4 in PoGo)
            if (((defenderType1List[i] == 1.6) && (defenderType2List[i] == 0.390625) && (gameSwitch.isChecked))
                || ((defenderType1List[i] == 1.0) && (defenderType2List[i] == 0.625))
                || ((defenderType1List[i] == 0.625) && (defenderType2List[i] == 1.0))
                || ((defenderType1List[i] == 0.390625) && (defenderType2List[i] == 1.6) && (gameSwitch.isChecked))
            ) {
                defenderNetListOfStrings.add(Effectiveness.NOT_VERY_EFFECTIVE.impact)
            }

            // Type interactions lower than .5x are different in Pokemon Go than the main game

            // Ultra not very effective
            // (.25x or .390625x)
            // (1 permutation in main game, 2 in PoGo)
            if (((defenderType1List[i] == 0.625) && (defenderType2List[i] == 0.625))
                || ((defenderType1List[i] == 1.0) && (defenderType2List[i] == 0.390625) && (gameSwitch.isChecked))
                || ((defenderType1List[i] == 0.390625) && (defenderType2List[i] == 1.0) && (gameSwitch.isChecked))
            ) {
                defenderNetListOfStrings.add(Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact)
            }

            // Does not effect
            // (0x in main game, not possible in PoGo)
            // (7 permutations for main game, 0 in PoGo)
            if (((defenderType1List[i] == 0.390625) && (!gameSwitch.isChecked))
                || ((defenderType2List[i] == 0.390625) && (!gameSwitch.isChecked))
            ) {
                defenderNetListOfStrings.add(Effectiveness.DOES_NOT_EFFECT.impact)
            }

            // Ultra does not effect
            // (Not possible in main game, .244x in PoGo)
            // (0 permutations in main game, 2 in PoGo)
            if (((defenderType1List[i] == 0.625) && (defenderType2List[i] == 0.390625) && (gameSwitch.isChecked))
                || ((defenderType1List[i] == 0.390625) && (defenderType2List[i] == 0.625) && (gameSwitch.isChecked))
            ) {
                defenderNetListOfStrings.add(Effectiveness.ULTRA_DOES_NOT_EFFECT.impact)
            }
        }
        return (defenderNetListOfStrings)
    }

    private fun onesString(): MutableList<String> {
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

    private fun onesInt(): MutableList<Int> {
        val table = mutableListOf<Int>()
        for (i in 0 until 18) {
            table.add(1)
        }
        return table
    }

    private fun setupSpinner(spinnerOptions: Array<String>, spinner: Spinner) {
        // Assigning the povSpinner options to an adapter value, which is then assigned to the povSpinner
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        spinner.adapter = spinnerAdapter
    }

    private fun fetchJson() {
        val url = "https://pogoapi.net/api/v1/type_effectiveness.json"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val gson = GsonBuilder().create()
                val typeToken: Type =
                    object : TypeToken<Map<PokemonType, Map<String, Double>>>() {}.type
                typeMatchups = gson.fromJson(body, typeToken)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to call request")
            }
        })
    }

    private fun adjustTableHeaderText(tableHeader: TextView, type1: Int, type2: Int = 0) {
        if (type1 == 0 && type2 == 0) {
            adjustVisibility(tableHeader, 2)
        }
        val arrayOfTypesJiceOrNoJice: Array<String> = resources.getStringArray(R.array.spinner_type_options_1)
        if (jiceTime) {
            arrayOfTypesJiceOrNoJice[12] = getString(R.string.jice)
        }
        when (weAreDefending) {
            true -> {
                if (type1 != 0 && type2 == 0) {
                    adjustVisibility(tableHeader, 0)
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        arrayOfTypesJiceOrNoJice[type1]
                    )
                }
                if (type1 == 0 && type2 != 0) {
                    adjustVisibility(tableHeader, 0)
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        arrayOfTypesJiceOrNoJice[type2]
                    )
                }
                if (type1 != 0 && type2 != 0 && type1 != type2) {
                    adjustVisibility(tableHeader, 0)
                    tableHeader.text = resources.getString(
                        R.string.table_header_two_types,
                        "_____",
                        arrayOfTypesJiceOrNoJice[type1],
                        arrayOfTypesJiceOrNoJice[type2]
                    )
                }
                if (type1 != 0 && type1 == type2) {
                    adjustVisibility(tableHeader, 0)
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        arrayOfTypesJiceOrNoJice[type1]
                    )
                }
            }
            false -> {
                adjustVisibility(tableHeader, 0)
                tableHeader.text = resources.getString(
                    R.string.table_header_one_type,
                    arrayOfTypesJiceOrNoJice[type1], "_____"
                )
            }
        }
    }

    private fun checkIfTypingExists(type1: Int, type2: Int) {
        val currentTypingPair: List<Int> = listOf(type1, type2)
        if (currentTypingPair in listOfNonexistentTypes) {
            adjustVisibility(doesNotExistDisclaimer, 0)
        } else {
            adjustVisibility(doesNotExistDisclaimer, 1)
        }
    }

    private fun makeVisibleIfTypeSelected(givenView: View, type1: Int, type2: Int = 0) {
        if (type1 != 0 || type2 != 0) {
            adjustVisibility(givenView, 0)
        } else {
            adjustVisibility(givenView,1)
        }
    }

    // @@@ktg there's an easier way to instantiate a list of the same value
    // hint: loops/in-line functions

    private fun attackingEffectivenessCalculator(attacker: Int): MutableList<Double> {
        if (attacker == 0) { return onesDouble() }

        var dictOfSelectedTypes: Map<String, Double> = emptyMap()
        val attackerType: String = attackingSpinnerTypeOptions[attacker]

        for (moveType in PokemonType.values()) {
            if (attackerType == moveType.type) {
                dictOfSelectedTypes = typeMatchups.getValue(moveType)
            }
        }
        return dictOfSelectedTypes.values.toMutableList()
    }

    // Returns a mutable list for how one type defends against all other types
    private fun defendingEffectivenessCalculator(defender: Int): MutableList<Double> {
        if (defender == 0) {
            return onesDouble()
        }
        var dictOfSelectedTypes: Map<String, Double>
        val listOfDefendingMatchupCoefficients: MutableList<Double> = arrayListOf()
        val defendingType: String = defendingSpinnerType1Options[defender]
        for (moveType in PokemonType.values()) {
            dictOfSelectedTypes = typeMatchups.getValue(moveType)
            dictOfSelectedTypes[defendingType]?.let { listOfDefendingMatchupCoefficients.add(it) }
        }
        return listOfDefendingMatchupCoefficients
    }

    private var arrayOfIcons: MutableList<Int> = mutableListOf(
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

    private val listOfNonexistentTypes: List<List<Int>> = listOf(
        listOf(13,12),
        listOf(13,14),
        listOf(13,1),
        listOf(13,16),
        listOf(13,9),
        listOf(13,17),
        listOf(7,5),
        listOf(7,8),
        listOf(4,6),
        listOf(12,14),
        listOf(6,11),
        listOf(6,5),
        listOf(14,17),
        listOf(11,8),
        listOf(1,3),
        listOf(1,2),
        listOf(16,9),
        listOf(12,13),
        listOf(14,13),
        listOf(1,13),
        listOf(16,13),
        listOf(9,13),
        listOf(17,13),
        listOf(5,7),
        listOf(8,7),
        listOf(6,4),
        listOf(14,12),
        listOf(11,6),
        listOf(5,6),
        listOf(17,14),
        listOf(8,11),
        listOf(3,1),
        listOf(2,1),
        listOf(9,16))
}