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

@SuppressLint("UseSwitchCompatOrMaterialCode")
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    // Worked for me
    private var mainActivityViewModel = MainActivityViewModel()

    // Link between UI and BL
    // needed for gridView functionality
    private var recyclerView: RecyclerView? = null
    private var gridLayoutManager: GridLayoutManager? = null
    private var arrayListForTypeGrid: ArrayList<TypeGrid> ? = null
    private var typeGridAdapter: TypeGridAdapter ? = null

    var attackingType = "(choose)"
    var defendingType1 = "(choose)"
    var defendingType2 = "[none]"

    // needed for makeDataVisibleIfATypeIsSelected()
    private lateinit var typeTableRecyclerView: RecyclerView
    private lateinit var gameSwitchText: TextView
    private lateinit var gameSwitch: Switch
    private lateinit var iceJiceSwitch: Switch
    private lateinit var tableHeader: TextView

    @SuppressLint("UseSwitchCompatOrMaterialCode", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide() // Hides top bar

        // Binding set-up
        val binding =
            ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root) // Sets up bindings for activity_main
        // Night mode compatibility
        val mainLinearLayout = binding.mainLinearLayout
        gameSwitchText = binding.gameSwitchText
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
        // Bindings
        val defendingType1Spinner = binding.type1Spinner
        val defendingType2Spinner = binding.type2Spinner
        val attackingTypeSpinner = binding.attackingTypeSpinner
        val typeSelectionPrompt = binding.secondPrompt
        tableHeader = binding.tableHeader
        val doesNotExistDisclaimer = binding.doesNotExistDisclaimer
        val attackingTypeSpinnerAndLabel = binding.attackingTypeSpinnerAndLabel
        val defendingType1SpinnerAndLabel = binding.defendingType1SpinnerAndLabel
        val defendingType2SpinnerAndLabel = binding.defendingType2SpinnerAndLabel
        val povSwitch = binding.povSwitch
        gameSwitch = binding.gameSwitch
        iceJiceSwitch = binding.iceJiceSwitch
        typeTableRecyclerView = binding.typeTableRecyclerView
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
        // mainActivityViewModel.fetchAllPokemonNamesAndURLs()
        // mainActivityViewModel.fetchAllTypingPossibilities()

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

        // Necessary to adjust spinner values when switching between Attacker and Defender
        var atkSpinnerIndex = 0
        var defSpinner1Index = 0
        var defSpinner2Index = 0

        // LiveData implemented for the tableHeader
        // but adjustTableHeaderText() still under each onSelectedListener
        mainActivityViewModel.tableHeaderText.observe(
            this,{tableHeader.text = it } )
        mainActivityViewModel.promptText.observe(this, { typeSelectionPrompt.text = it })

        // POV SWITCH
        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.weAreDefending = onSwitch
            // Swaps the respective spinners in and out
            attackingTypeSpinnerAndLabel.visibility = if (mainActivityViewModel.weAreDefending) { View.GONE } else { View.VISIBLE }
            defendingType1SpinnerAndLabel.visibility = if (mainActivityViewModel.weAreDefending) { View.VISIBLE } else { View.GONE }
            defendingType2SpinnerAndLabel.visibility = if (mainActivityViewModel.weAreDefending) { View.VISIBLE } else { View.GONE }

            povSwitch.text = if (mainActivityViewModel.weAreDefending) { getString(R.string.pov_switch_to_defending) } else { getString(R.string.pov_switch_to_attacking) }
            // old
            // typeSelectionPrompt.text = if (weAreDefending) {resources.getString(R.string.defending_prompt) } else { resources.getString(R.string.attacking_prompt) }
            // new
            mainActivityViewModel.promptText.value = if (mainActivityViewModel.weAreDefending) {resources.getString(R.string.defending_prompt) } else { resources.getString(R.string.attacking_prompt) }


            // QoL: transfers the value from the to-be-invisible spinner to the to-be-visible one
            if (mainActivityViewModel.weAreDefending) {
                defendingType2 = "[none]"
                defendingType2Spinner.setSelection(0)
                defendingType1 = attackingType
                defendingType1Spinner.setSelection(atkSpinnerIndex)
            } else {
                attackingType = defendingType1
                attackingTypeSpinner.setSelection(defSpinner1Index)
                if ((defendingType1 == "(choose)" || defendingType1 == Types.NoType.type) &&
                    (defendingType2 != "[none]" && defendingType2 != Types.NoType.type)) {
                    attackingType = defendingType2
                    attackingTypeSpinner.setSelection(defSpinner2Index)
                }
                doesNotExistDisclaimer.visibility = View.INVISIBLE
            }
            refreshTheData()
            adjustTableHeaderText()
        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                atkSpinnerIndex = p2
                attackingType = Types.values()[atkSpinnerIndex].type
                adjustTableHeaderText()
                makeGridAndSwitchesVisibleIfATypeIsSelected()
                refreshTheData()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                defSpinner1Index = p2
                defendingType1 = Types.values()[defSpinner1Index].type
                adjustTableHeaderText()
                makeGridAndSwitchesVisibleIfATypeIsSelected()

                // TODO(Work on doesNotExistDisclaimer rework after liveData is worked out)
                /*doesNotExistDisclaimer.visibility = if (mutableListOf(
                        defendingType1,
                        defendingType2
                    ) !in mainActivityViewModel.listOfPossibleTypes
                ) { View.VISIBLE } else { View.INVISIBLE }*/

                doesNotExistDisclaimer.visibility = if (mainActivityViewModel.doesThisTypingExist(defendingType1,defendingType2)) {View.VISIBLE} else {View.INVISIBLE}
                refreshTheData()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                defSpinner2Index = p2
                defendingType2 = Types.values()[defSpinner2Index].type
                adjustTableHeaderText()
                makeGridAndSwitchesVisibleIfATypeIsSelected()
                doesNotExistDisclaimer.visibility = if (mainActivityViewModel.doesThisTypingExist(defendingType1,defendingType2)) {View.VISIBLE} else {View.INVISIBLE}
                refreshTheData()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        gameSwitchText.setOnClickListener { gameSwitch.toggle() } // toggles gameSwitch when text is clicked
        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.pogoTime = onSwitch
            gameSwitchText.text = if (mainActivityViewModel.pogoTime) {
                resources.getString(R.string.pogo) } else { resources.getString(R.string.mainGame)}
            refreshTheData()
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
                adjustTableHeaderText()
            }
            refreshTheData()
        }
        infoButton.setOnClickListener {
            val intent = Intent(this, TypeTriviaActivity::class.java)
            startActivity(intent)
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////     End of onCreate     ///////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////

    private fun makeGridAndSwitchesVisibleIfATypeIsSelected() {
        val type1 = if (mainActivityViewModel.weAreDefending) { defendingType1 } else { attackingType }
        val type2 = if (mainActivityViewModel.weAreDefending) { defendingType2 } else { "[none]" }
                if ((type1 == "(choose)" || type1 == Types.NoType.type) &&
                    (type2 == "[none]" || type2 == Types.NoType.type)) {
                    typeTableRecyclerView.visibility = View.INVISIBLE
                    gameSwitchText.visibility = View.INVISIBLE
                    gameSwitch.visibility = View.INVISIBLE
                    iceJiceSwitch.visibility = View.INVISIBLE
                    tableHeader.visibility = View.INVISIBLE
                } else {
                    typeTableRecyclerView.visibility = View.VISIBLE
                    gameSwitchText.visibility = View.VISIBLE
                    gameSwitch.visibility = View.VISIBLE
                    iceJiceSwitch.visibility = View.VISIBLE
                    tableHeader.visibility = View.VISIBLE
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
    private fun adjustTableHeaderText() {
        val type1 = if (mainActivityViewModel.weAreDefending) { defendingType1 } else { attackingType }
        val type2 = if (mainActivityViewModel.weAreDefending) { defendingType2 } else { "[none]" }
        var type1Displayed = type1
        var type2Displayed = type2
        if ((type1Displayed == "Ice" || type1Displayed == "Jice")) {
            type1Displayed = if (mainActivityViewModel.jiceTime) { "Jice" } else { "Ice" }
        }
        if ((type2Displayed == "Ice" || type2Displayed == "Jice")) {
            type2Displayed = if (mainActivityViewModel.jiceTime) { "Jice" } else { "Ice" }
        }

        when (mainActivityViewModel.weAreDefending) {
            true -> {
                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && (type2Displayed == "[none]" || type2Displayed == Types.NoType.type)) {
                    mainActivityViewModel.tableHeaderText.value = resources.getString(R.string.table_header_one_type, "_____", type1Displayed)
                }
                if ((type1Displayed == "(choose)" || type1Displayed == Types.NoType.type) && (type2Displayed != "[none]" && type2Displayed != Types.NoType.type)) {
                    mainActivityViewModel.tableHeaderText.value = resources.getString(R.string.table_header_one_type, "_____", type2Displayed)
                }
                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && (type2Displayed != "[none]" && type2Displayed != Types.NoType.type) && type1Displayed != type2Displayed) {
                    mainActivityViewModel.tableHeaderText.value = resources.getString(R.string.table_header_two_types, "_____", type1Displayed, type2Displayed)
                }
                if ((type1Displayed != "(choose)" && type1Displayed != Types.NoType.type) && type1Displayed == type2Displayed) {
                    mainActivityViewModel.tableHeaderText.value = resources.getString(R.string.table_header_one_type, "_____", type1Displayed)
                }
            }
            false -> {
                mainActivityViewModel.tableHeaderText.value = resources.getString(R.string.table_header_one_type, type1Displayed, "_____")
            }
        }
    }

    // BL BUT IT USES THE TWO FUNCTIONS THAT NEED COLORS
    private fun interactionsToGridView(interactionsList: MutableList<Double>) {
        val effectivenessList = mainActivityViewModel.interactionsToEffectiveness(interactionsList)
        val displayedListOfInteractions = mainActivityViewModel.effectivenessToDisplayedCellValues(effectivenessList)
        val listOfCellTextColors = effectivenessToCellTextColors(effectivenessList)
        val listOfCellBackgroundColors = effectivenessToCellBackgroundColors(effectivenessList)
        arrayListForTypeGrid = mainActivityViewModel.setDataInTypeGridList(mainActivityViewModel.arrayOfTypeIcons,displayedListOfInteractions,listOfCellBackgroundColors,listOfCellTextColors)
        typeGridAdapter = TypeGridAdapter(arrayListForTypeGrid!!)
        recyclerView?.adapter = typeGridAdapter
    }

    // BL BUT NEEDS COLORS, which I can't get in MainActivityViewModel yet
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

    // BL BUT NEEDS COLORS, which I can't get in MainActivityViewModel yet
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

    // Strikes me as BL, but I don't want to call in attackingType, defendingType1, defendingType2 from MainActivity
    private fun refreshTheData() {
        when (mainActivityViewModel.weAreDefending) {
            false -> {
                // if attacking
                mainActivityViewModel.listOfInteractions =
                    mainActivityViewModel.attackingEffectivenessCalculator(attackingType)
            }
            true -> {
                mainActivityViewModel.listOfInteractions =
                    // if defending with type 1 only
                    if (defendingType2 == "[none]" || defendingType2 == Types.NoType.type || defendingType1 == defendingType2) {
                        mainActivityViewModel.defendingEffectivenessCalculator(defendingType1)
                        // if defending with type 2 only
                    } else if (defendingType1 == "(choose)" || defendingType1 == Types.NoType.type) {
                        mainActivityViewModel.defendingEffectivenessCalculator(defendingType2)
                        // if defending with both type1 and type 2
                    } else {
                        mainActivityViewModel.defendingWithTwoTypesCalculator(
                            defendingType1,
                            defendingType2
                        )
                    }
            }
        }
        interactionsToGridView(mainActivityViewModel.listOfInteractions)
    }

    // BL, to be implemented later when LiveData is a thing
    /*private fun justChangeJiceInGridView() {
        arrayListForTypeGrid?.get(11)?.iconsInGridView = if (mainActivityViewModel.jiceTime) { R.drawable.jice_icon } else { R.drawable.ice_icon }
        arrayListForTypeGrid?.let { TypeGridAdapter(it).submitList(arrayListForTypeGrid) }
    }*/
}