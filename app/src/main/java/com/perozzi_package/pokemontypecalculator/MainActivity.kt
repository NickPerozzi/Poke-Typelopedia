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

    private var mainActivityViewModel = MainActivityViewModel()

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
        mainActivityViewModel.fetchJson() //gets .json file

        // BL
        // Initializes the gridView
        val listOfCellBackgroundColors: MutableList<Int> = mainActivityViewModel.onesInt()
        val listOfCellTextColors: MutableList<Int> = mainActivityViewModel.onesInt()

        // Link between UI and BL
        recyclerView = findViewById(R.id.typeTableRecyclerView)
        gridLayoutManager = GridLayoutManager(applicationContext, 3, LinearLayoutManager.VERTICAL,false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(true)
        arrayListForTypeGrid = ArrayList()
        arrayListForTypeGrid = mainActivityViewModel.setDataInTypeGridList(mainActivityViewModel.arrayOfTypeIcons,mainActivityViewModel.onesString(),listOfCellBackgroundColors,listOfCellTextColors)
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
        var listOfInteractions: MutableList<Double> = mainActivityViewModel.onesDouble()

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

                    listOfInteractions = mainActivityViewModel.attackingEffectivenessCalculator(attackingType)
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
                        mainActivityViewModel.defendingEffectivenessCalculator(defendingType1)
                    } else if (defendingType1 == "(choose)") {
                        mainActivityViewModel.defendingEffectivenessCalculator(defendingType2)
                    } else {
                        mainActivityViewModel.defendingWithTwoTypesCalculator(defendingType1, defendingType2)
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
                listOfInteractions = mainActivityViewModel.attackingEffectivenessCalculator(attackingType)

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
                doesNotExistDisclaimer.visibility = if (mainActivityViewModel.doesThisTypingExist(defendingType1,defendingType2)) {View.VISIBLE} else {View.INVISIBLE}
                makeVisibleIfTypeSelected(tableHeader,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(typeTableRecyclerView,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitchText,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitch,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(iceJiceSwitch,defendingType1,defendingType2)

                // Gets values and shows them in GridView if only one type is selected
                listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType2)
                } else {
                    mainActivityViewModel.defendingWithTwoTypesCalculator(defendingType1, defendingType2)
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
                doesNotExistDisclaimer.visibility = if (mainActivityViewModel.doesThisTypingExist(defendingType1,defendingType2)) {View.VISIBLE} else {View.INVISIBLE}
                makeVisibleIfTypeSelected(tableHeader,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(typeTableRecyclerView,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitchText,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(gameSwitch,defendingType1,defendingType2)
                makeVisibleIfTypeSelected(iceJiceSwitch,defendingType1,defendingType2)

                listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType2)
                } else {
                    mainActivityViewModel.defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                }
                interactionsToGridView(listOfInteractions)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // UI
        // TODO(change the switch when the game switch text is selected in addition to the switch itself)
        // gameSwitchText.setOnClickListener
        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.pogoTime = onSwitch
            run {
                when (weAreDefending) {
                    false -> {
                        listOfInteractions =
                            mainActivityViewModel.attackingEffectivenessCalculator(attackingType)
                    }
                    true -> {
                        listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                            mainActivityViewModel.defendingEffectivenessCalculator(defendingType1)
                        } else if (defendingType1 == "(choose)") {
                            mainActivityViewModel.defendingEffectivenessCalculator(defendingType2)
                        } else {
                            mainActivityViewModel.defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                        }
                        interactionsToGridView(listOfInteractions)
                    }
                }
            }

            // Changes the switch's text between Pogo and Main Game
            if (mainActivityViewModel.pogoTime) {
                gameSwitchText.text = resources.getString((R.string.pogo))
            } else {
                gameSwitchText.text = resources.getString((R.string.mainGame))
            }

            // Sends information to gridView depending on whether dual type is selected or not
            if (weAreDefending) {
                listOfInteractions = if (defendingType2 == "[none]" || defendingType1 == defendingType2) {
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType2)
                } else {
                    mainActivityViewModel.defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                }
            }
            interactionsToGridView(listOfInteractions)
        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.jiceTime = onSwitch

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
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType1)
                } else if (defendingType1 == "(choose)") {
                    mainActivityViewModel.defendingEffectivenessCalculator(defendingType2)
                } else {
                    mainActivityViewModel.defendingWithTwoTypesCalculator(defendingType1, defendingType2)
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
        var type1Displayed = type1
        var type2Displayed = type2
        if ((type1Displayed == "Ice" || type1Displayed == "Jice")) {
            type1Displayed = if (mainActivityViewModel.jiceTime) { "Jice" } else { "Ice" }
        }
        if ((type2Displayed == "Ice" || type2Displayed == "Jice")) {
            type2Displayed = if (mainActivityViewModel.jiceTime) { "Jice" } else { "Ice" }
        }

        when (weAreDefending) {
            true -> {
                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && (type2Displayed == "[none]" || type2Displayed == Types.NoType.type)) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        type1Displayed
                    )
                }
                if ((type1Displayed == "(choose)" || type1Displayed == Types.NoType.type) && (type2Displayed != "[none]" && type2Displayed != Types.NoType.type)) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        type2Displayed
                    )
                }
                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && (type2Displayed != "[none]" && type2Displayed != Types.NoType.type) && type1Displayed != type2Displayed) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_two_types,
                        "_____",
                        type1Displayed,
                        type2Displayed
                    )
                }
                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && type1Displayed == type2Displayed) {
                    tableHeader.text = resources.getString(
                        R.string.table_header_one_type,
                        "_____",
                        type1Displayed
                    )
                }
            }
            false -> {
                tableHeader.text = resources.getString(
                    R.string.table_header_one_type,
                    type1Displayed, "_____"
                )
            }
        }
    }

    // BL to UI
    private fun interactionsToGridView(interactionsList: MutableList<Double>) {
        val effectivenessList = mainActivityViewModel.interactionsToEffectiveness(interactionsList)
        val displayedListOfInteractions = mainActivityViewModel.effectivenessToDisplayedCellValues(effectivenessList)
        val listOfCellTextColors = effectivenessToCellTextColors(effectivenessList)
        val listOfCellBackgroundColors = effectivenessToCellBackgroundColors(effectivenessList)
        arrayListForTypeGrid = mainActivityViewModel.setDataInTypeGridList(mainActivityViewModel.arrayOfTypeIcons,displayedListOfInteractions,listOfCellBackgroundColors,listOfCellTextColors)
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
        recyclerView?.adapter = typeGridAdapter
    }

    // BL
    // NEEDS COLORS
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
    // NEEDS COLORS
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

    // @@@ktg there's an easier way to instantiate a list of the same value
    // hint: loops/in-line functions
    // @@@nap see onesString(), onesDouble(), and onesInt()

    // ONLY update the jice elements
    // write a function that replaces setDataInTypeGridList

    // BL
    private fun justChangeJiceInGridView() {
        arrayListForTypeGrid?.get(11)?.iconsInGridView = if (mainActivityViewModel.jiceTime) { R.drawable.jice_icon } else { R.drawable.ice_icon }
        arrayListForTypeGrid?.let { TypeGridAdapter(it).submitList(arrayListForTypeGrid) }
    }
}