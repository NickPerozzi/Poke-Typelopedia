package com.perozzi_package.pokemontypecalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
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

@SuppressLint("UseSwitchCompatOrMaterialCode")
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    // Link between UI and BL
    // needed for gridView functionality
    private var recyclerView: RecyclerView? = null
    private var gridLayoutManager: GridLayoutManager? = null
    private var arrayListForTypeGrid:ArrayList<TypeGrid> ? = null
    private var typeGridAdapter:TypeGridAdapter ? = null

    // BL
    // needed for 3 functions
    private lateinit var typeMatchups: Map<Types, Map<String, Double>>

    // BL
    // needed for 3 functions
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private var jiceTime = false

    // BL
    // needed for 3 functions
    private var pogoTime = false

    // BL
    // needed for checkIfTypingExists()
    private lateinit var doesNotExistDisclaimer: TextView // used by makeVisibleIfTypeSelected()

    // BL
    // needed for 2 functions
    private var weAreDefending = false

    // UI
    // needed for adjustTypeSpinnerVisibility()
    private lateinit var attackingTypeSpinnerAndLabel: LinearLayout
    private lateinit var defendingType1SpinnerAndLabel: LinearLayout
    private lateinit var defendingType2SpinnerAndLabel: LinearLayout

    // BL
    private var defendingType1: String = "(choose)"
    private var defendingType2: String = "(choose)"
    private var attackingType: String = "[none]"

    @SuppressLint("UseSwitchCompatOrMaterialCode", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Hides top bar

        // UI
        val binding =
            ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root) // Sets up bindings for activity_main

        // UI
        // Night mode compatibility
        val mainLinearLayout = binding.mainLinearLayout
        val gameSwitchText = binding.gameSwitchText
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mainLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector_night)
                gameSwitchText.setTextColor(Color.WHITE)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mainLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector)
                gameSwitchText.setTextColor(Color.BLACK)
            }
        }

        // UI
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
        val gameSwitch = binding.gameSwitch
        val iceJiceSwitch = binding.iceJiceSwitch
        val typeTableRecyclerView = binding.typeTableRecyclerView
        val infoButton = binding.infoButton

        // UI
        // Populates spinner options
        val attackingSpinnerTypeOptions = resources.getStringArray(R.array.spinner_type_options_1)
        setupSpinner(attackingSpinnerTypeOptions, attackingTypeSpinner)
        val defendingSpinnerType1Options = resources.getStringArray(R.array.spinner_type_options_1)
        setupSpinner(defendingSpinnerType1Options, defendingType1Spinner)
        val defendingSpinnerType2Options = resources.getStringArray(R.array.spinner_type_options_2)
        setupSpinner(defendingSpinnerType2Options, defendingType2Spinner)

        // BL
        fetchJson() //gets .json file

        // BL
        // Initializes the gridView
        val listOfCellBackgroundColors: MutableList<Int> = onesInt()
        val listOfCellTextColors: MutableList<Int> = onesInt()

        // Link between UI and BL
        recyclerView = findViewById(R.id.typeTableRecyclerView)
        gridLayoutManager = GridLayoutManager(applicationContext, 3, LinearLayoutManager.VERTICAL,false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(true)
        arrayListForTypeGrid = ArrayList()
        arrayListForTypeGrid = setDataInTypeGridList(arrayOfIcons,onesString(),listOfCellBackgroundColors,listOfCellTextColors)
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
        recyclerView?.adapter = typeGridAdapter

        // BL
        weAreDefending = false
        attackingType = "(choose)"
        defendingType1 = "(choose)"
        defendingType2 = "[none]"

        // Necessary to adjust spinner values when switching between Attacker and Defender
        var atkSpinnerIndex = 0
        var defSpinner1Index = 0
        var defSpinner2Index = 0

        // BL
        var listOfInteractions: MutableList<Double> = onesDouble()

        // UI
        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            weAreDefending = onSwitch
            adjustTypeSpinnersVisibility()

            when (weAreDefending) {
                false -> {
                    if (defendingType1 != "(choose)") {
                        attackingType = defendingType1
                        // This line is the reason defSpinner1Index exists
                        attackingTypeSpinner.setSelection(defSpinner1Index)
                    }
                    if (defendingType1 == "(choose)" && defendingType2 != "[none]") {
                        attackingType = defendingType2
                        attackingTypeSpinner.setSelection(defSpinner2Index)
                    }

                    povSwitch.text = getString(R.string.pov_switch_to_attacking)
                    typeSelectionPrompt.text = resources.getString(R.string.attacking_prompt)
                    adjustTableHeaderText(tableHeader,attackingType)
                    adjustVisibility(doesNotExistDisclaimer,1)

                    listOfInteractions = attackingEffectivenessCalculator(attackingType)
                }
                true -> {
                    defendingType2 = "[none]"
                    defendingType2Spinner.setSelection(0)
                    defendingType1 = attackingType
                    defendingType1Spinner.setSelection(atkSpinnerIndex)
                    povSwitch.text = getString(R.string.pov_switch_to_defending)
                    typeSelectionPrompt.text = resources.getString(R.string.defending_prompt)
                    adjustTableHeaderText(tableHeader,defendingType1,defendingType2)

                    listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                        defendingEffectivenessCalculator(defendingType1)
                    } else if (defendingType1 == "(choose)") {
                        defendingEffectivenessCalculator(defendingType2)
                    } else {
                        defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                    }
                }
            }
            interactionsToGridView(listOfInteractions)
        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                atkSpinnerIndex = p2
                attackingType = Types.values()[p2].type
                // Saving for later in case I need it
                // attackingType = attackingTypeSpinner.selectedItem.toString()

                // Table header text adjustment
                adjustTableHeaderText(tableHeader,attackingType)

                // Adjusts visibility depending on whether user has selected a type yet
                makeVisibleIfTypeSelected(typeTableRecyclerView,attackingType)
                makeVisibleIfTypeSelected(gameSwitchText,attackingType)
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
                defSpinner1Index = p2
                defendingType1 = Types.values()[p2].type

                // Table header text adjustment
                adjustTableHeaderText(tableHeader,defendingType1,defendingType2)

                // Adjusts visibility depending on whether user has selected a type yet
                checkIfTypingExists(defendingType1,defendingType2)
                makeVisibleIfTypeSelected(tableHeader,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(typeTableRecyclerView,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitchText,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitch,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(iceJiceSwitch,defendingType1,defendingType2)

                // Gets values and shows them in GridView if only one type is selected
                listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                    defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    defendingEffectivenessCalculator(defendingType2)
                } else {
                    defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                }
                interactionsToGridView(listOfInteractions)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Called when user selects an option in the second type spinner
        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                defSpinner2Index = p2
                defendingType2 = Types.values()[p2].type

                // Table header text adjustment
                adjustTableHeaderText(tableHeader,defendingType1,defendingType2)

                // Adjusts visibility depending on whether user has selected a type yet
                checkIfTypingExists(defendingType1,defendingType2)
                makeVisibleIfTypeSelected(tableHeader,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(typeTableRecyclerView,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitchText,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitch,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(iceJiceSwitch,defendingType1,defendingType2)

                listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                    defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    defendingEffectivenessCalculator(defendingType2)
                } else {
                    defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                }
                interactionsToGridView(listOfInteractions)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // UI
        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            pogoTime = onSwitch
            run {
                when (weAreDefending) {
                    false -> {
                        listOfInteractions =
                            attackingEffectivenessCalculator(attackingType)
                    }
                    true -> {
                        listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                            defendingEffectivenessCalculator(defendingType1)
                        } else if (defendingType1 == "(choose)") {
                            defendingEffectivenessCalculator(defendingType2)
                        } else {
                            defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                        }
                        interactionsToGridView(listOfInteractions)
                    }
                }
            }

            // Changes the switch's text between Pogo and Main Game
            if (pogoTime) {
                gameSwitchText.text = resources.getString((R.string.pogo))
            } else {
                gameSwitchText.text = resources.getString((R.string.mainGame))
            }

            // Sends information to gridView depending on whether dual type is selected or not
            if (weAreDefending) {
                listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                    defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    defendingEffectivenessCalculator(defendingType2)
                } else {
                    defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                }
            }
            interactionsToGridView(listOfInteractions)
        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            jiceTime = onSwitch

            // Adjusts the text on the switch itself
            iceJiceSwitch.text = if (onSwitch) { getString(R.string.jice) } else { getString(R.string.ice) }

            // Adjusts the text in the spinners
            attackingSpinnerTypeOptions[12] = if (onSwitch) { getString(R.string.jice ) } else { getString(R.string.ice) }
            defendingSpinnerType1Options[12] = if (onSwitch) { getString(R.string.jice ) } else { getString(R.string.ice) }
            defendingSpinnerType2Options[12] = if (onSwitch) { getString(R.string.jice ) } else { getString(R.string.ice) }

            // Adjusts the text in the table header (only if Ice/Jice is currently selected)
            if ((attackingType == "Ice" || defendingType1 == "Ice" || defendingType2 == "Ice")
                || (attackingType == "Jice" || defendingType1 == "Jice" || defendingType2 == "Jice")) {
                when (weAreDefending) {
                    false -> adjustTableHeaderText(tableHeader, attackingType)
                    true -> adjustTableHeaderText(tableHeader, defendingType1, defendingType2)
                }
            }
            // Adjusts the icon in the gridView
            justChangeJiceInGridView()


            // Sends information to gridView depending on whether dual type is selected or not

            // TODO(Eventually not need these lines of code)
            if (!weAreDefending) {
                interactionsToGridView(listOfInteractions)
            }
            if (weAreDefending) {
                listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                    defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    defendingEffectivenessCalculator(defendingType2)
                } else {
                    defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                }
                interactionsToGridView(listOfInteractions)
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

    // UI
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

    // UI
    private fun makeVisibleIfTypeSelected(givenView: View, type1: String, type2: String = "[none]") {
        if ((type1 == "(choose)" || type1 == Types.NoType.type) && (type2 == "[none]" || type2 == Types.NoType.type)) {
            adjustVisibility(givenView, 1)
        } else {
            adjustVisibility(givenView,0)
        }
    }

    // UI
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

    // UI
    private fun adjustVisibility(selectedTextView: View, visibleInvisibleGone: Int) {
        when (visibleInvisibleGone) {
            0 -> selectedTextView.visibility = View.VISIBLE
            1 -> selectedTextView.visibility = View.INVISIBLE
            2 -> selectedTextView.visibility = View.GONE
        }
    }

    // UI
    private fun setupSpinner(spinnerOptions: Array<String>, spinner: Spinner) {
        // Assigning the povSpinner options to an adapter value, which is then assigned to the povSpinner
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        spinner.adapter = spinnerAdapter
        spinner.setSelection(0,false)
    }

    // UI
    private fun adjustTableHeaderText(tableHeader: TextView, type1: String, type2: String = "[none]") {
        if ((type1 == "(choose)" || type1 == Types.NoType.type) && (type2 == "[none]" || type2 == Types.NoType.type)){
            adjustVisibility(tableHeader, 2)
        } else {adjustVisibility(tableHeader, 0)}

        when (weAreDefending) {
            true -> {
                if ((type1 != "(choose)" && type1 != Types.NoType.type) && (type2 == "[none]" || type2 == Types.NoType.type)) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        type1
                    )
                }
                if ((type1 == "(choose)" || type1 == Types.NoType.type) && (type2 != "[none]" && type2 != Types.NoType.type)) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        type2
                    )
                }
                if ((type1 != "(choose)" && type1 != Types.NoType.type) && (type2 != "[none]" && type2 != Types.NoType.type) && type1 != type2) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_two_types,
                        "_____",
                        type1,
                        type2
                    )
                }
                if ((type1 != "(choose)" && type1 != Types.NoType.type) && type1 == type2) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        type1
                    )
                }
            }
            false -> {
                tableHeader.text = resources.getString(
                    R.string.table_header_one_type,
                    type1, "_____"
                )
            }
        }
    }

    // BL to UI
    private fun interactionsToGridView(interactionsList: MutableList<Double>) {
        val effectivenessList = interactionsToEffectiveness(interactionsList)
        val displayedListOfInteractions = effectivenessToDisplayedCellValues(effectivenessList)
        val listOfCellTextColors = effectivenessToCellTextColors(effectivenessList)
        val listOfCellBackgroundColors = effectivenessToCellBackgroundColors(effectivenessList)
        arrayListForTypeGrid = setDataInTypeGridList(arrayOfIcons,displayedListOfInteractions,listOfCellBackgroundColors,listOfCellTextColors)
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
        recyclerView?.adapter = typeGridAdapter
    }

    // BL
    private fun interactionsToEffectiveness(mutableList: MutableList<Double>): MutableList<String> {
        val stringList: MutableList<String> = mutableListOf()
        for (i in 0 until 18) {
            if (pogoTime) {
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

    //BL
    @SuppressLint("SetTextI18n")
    private fun effectivenessToDisplayedCellValues(listOfEffectivenesses: MutableList<String>): MutableList<String> {
        val mutableListOfEffectivenessDoubles: MutableList<String> = mutableListOf()
        for (i in 0 until 18) {
            if (!pogoTime) {
                when (listOfEffectivenesses[i]) {
                    Effectiveness.EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x1")
                    Effectiveness.SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x2")
                    Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x4")
                    Effectiveness.NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x0.5")
                    Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x0.25")
                    Effectiveness.DOES_NOT_EFFECT.impact -> mutableListOfEffectivenessDoubles.add("x0")
                }
            } else {
                when (listOfEffectivenesses[i]) {
                    Effectiveness.EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x1")
                    Effectiveness.SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x1.6")
                    Effectiveness.ULTRA_SUPER_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x2.56")
                    Effectiveness.NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x0.625")
                    Effectiveness.ULTRA_NOT_VERY_EFFECTIVE.impact -> mutableListOfEffectivenessDoubles.add("x0.391")
                    Effectiveness.ULTRA_DOES_NOT_EFFECT.impact -> mutableListOfEffectivenessDoubles.add("x0.244")
                }
            }
        }
        return mutableListOfEffectivenessDoubles
    }

    // BL
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

    // BL
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

    // BL
    private fun setDataInTypeGridList(iconMutableList: MutableList<Int>, effectivenessMutableList:MutableList<String>,
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

        // TODO(eventually not need these lines of code)
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

    // BL
    private fun defendingWithTwoTypesCalculator(type1: String, type2: String): MutableList<Double> {
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

    // BL
    private fun onesString(): MutableList<String> {
        val table = mutableListOf<String>()
        for (i in 0 until 18) {
            table.add("1.0")
        }
        return table
    }

    // BL
    private fun onesDouble(): MutableList<Double> {
        val table = mutableListOf<Double>()
        for (i in 0 until 18) {
            table.add(1.0)
        }
        return table
    }

    // BL
    private fun onesInt(): MutableList<Int> {
        val table = mutableListOf<Int>()
        for (i in 0 until 18) {
            table.add(1)
        }
        return table
    }

    // BL
    private fun fetchJson() {
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

    // BL
    private fun checkIfTypingExists(type1: String, type2: String) {
        val currentTypingPair: List<String> = listOf(type1, type2)
        if (currentTypingPair in listOfNonexistentTypes) {
            adjustVisibility(doesNotExistDisclaimer, 0)
        } else {
            adjustVisibility(doesNotExistDisclaimer, 1)
        }
    }

    // @@@ktg there's an easier way to instantiate a list of the same value
    // hint: loops/in-line functions
    // @@@nap see onesString(), onesDouble(), and onesInt()

    // BL
    private fun attackingEffectivenessCalculator(attacker: String): MutableList<Double> {
        if (attacker == "(choose)" || attacker == Types.NoType.type) { return onesDouble() }

        var dictOfSelectedTypes: Map<String, Double> = emptyMap()

        for (moveType in Types.values()) {
            if (attacker == moveType.type && attacker != Types.NoType.type) {
                dictOfSelectedTypes = typeMatchups.getValue(moveType)
            }
        }
        return dictOfSelectedTypes.values.toMutableList()
    }

    // BL
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

    // ONLY update the jice elements
    // write a function that replaces setDataInTypeGridList

    // BL
    private fun justChangeJiceInGridView() {
        arrayListForTypeGrid?.get(11)?.iconsInGridView = if (jiceTime) { R.drawable.jice_icon } else { R.drawable.ice_icon }
        arrayListForTypeGrid?.let { TypeGridAdapter(it).submitList(arrayListForTypeGrid) }
    }

    // BL
    private val listOfNonexistentTypes: List<List<String>> = listOf(
        listOf("Normal","Ice"), // Normal ice
        listOf("Ice","Normal"),
        listOf("Normal","Poison"), // Normal poison
        listOf("Poison","Normal"),
        listOf("Normal","Bug"), // Normal bug
        listOf("Bug","Normal"),
        listOf("Normal","Rock"), // Normal rock
        listOf("Rock","Normal"),
        listOf("Normal","Ghost"), // Normal ghost
        listOf("Ghost","Normal"),
        listOf("Normal","Steel"), // Normal steel
        listOf("Steel","Normal"),
        listOf("Fire","Fairy"), // Fire fairy
        listOf("Fairy","Fire"),
        listOf("Fire","Grass"), // Fire grass
        listOf("Grass","Fire"),
        listOf("Electric","Fighting"), // Fighting electric
        listOf("Fighting","Electric"),
        listOf("Ice","Poison"), // Ice poison
        listOf("Poison","Ice"),
        listOf("Fighting","Ground"), // Fighting ground
        listOf("Ground","Fighting"),
        listOf("Fighting","Fairy"), // Fighting fairy
        listOf("Fairy","Fighting"),
        listOf("Poison","Steel"), // Steel poison
        listOf("Steel","Poison"),
        listOf("Ground","Fairy"), // Fairy ground
        listOf("Fairy","Ground"),
        listOf("Bug","Dragon"), // Bug dragon
        listOf("Dragon","Bug"),
        listOf("Bug","Dark"), // Bug dark
        listOf("Dark","Bug"),
        listOf("Rock","Ghost"), // Rock ghost
        listOf("Ghost","Rock")
    )
}