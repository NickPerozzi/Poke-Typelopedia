package com.example.pokemontypecalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
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
import com.example.pokemontypecalculator.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException
import java.lang.reflect.Type

//class MainActivity(val TypeMatchups: TypeMatchupsClass) : AppCompatActivity() {
@SuppressLint("UseSwitchCompatOrMaterialCode")
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var gridLayoutManager: GridLayoutManager? = null
    private var arrayList:ArrayList<TypeGridView> ? = null
    private var typeGridViewAdapter:TypeGridViewAdapter ? = null

    // @@@ktg class-level variables are necessary sometimes, but not preferred - in almost all cases in CS, we want
    // to give things as limited of scope as possible (ideally, variables are scope to a function, for instance).
    // Some of these class-level (aka "member variables") are probably necessary. But I know at least one isn't.
    // After you do other refactoring (Enums, especially), come back and look at these. Can you make one or more of them
    // more tightly scoped (i.e. scoped to something less than the class, like a function)?
    // And also see if you have lateinit vars after that refactor, see if you can get rid of the lateinit property
    // of any remaining class-level variables

    // attackingEffectivenessCalculator() function uses typeMatchups, which is out of onCreate    // Functions outside onCreate use this line
    private lateinit var typeMatchups: Map<PokemonType, Map<String, Double>>

    // defendingWithTwoTypes() function uses gameSwitch, which is out of onCreate
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var gameSwitch: Switch

    private lateinit var iceJiceSwitch: Switch

    // povSpinnerOptions was placed into onCreate


    // attackingEffectivenessCalculator() function uses spinnerTypeOptions1, which is out of onCreate
    var defendingSpinnerType1Options = arrayOf<String>()
    private var attackingSpinnerTypeOptions = arrayOf<String>()

    // adjustTableHeading() uses tableHeader, which is in onCreate

    // checkIfExists uses
    private lateinit var doesNotExistDisclaimer: TextView

    // TODO @@@ktg break this out into several smaller functions that get called from onCreate
    //
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Initialize bindings
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Spinner bindings
        val povSpinner = binding.povSpinner
        val defendingType1Spinner = binding.type1Spinner
        val defendingType2Spinner = binding.type2Spinner
        val attackingTypeSpinner = binding.attackingTypeSpinner
        // TextView bindings
        val initialPrompt = binding.initialPrompt
        val typeSelectionPrompt = binding.secondPrompt
        val tableHeader = binding.tableHeader
        doesNotExistDisclaimer = binding.doesNotExistDisclaimer
        // Spinner + TextView bindings
        val attackingTypeSpinnerAndLabel = binding.attackingTypeSpinnerAndLabel
        val defendingType1SpinnerAndLabel = binding.defendingType1SpinnerAndLabel
        val defendingType2SpinnerAndLabel = binding.defendingType2SpinnerAndLabel
        // Switch binding
        gameSwitch = binding.gameSwitch
        iceJiceSwitch = binding.iceJiceSwitch
        // LinearLayout binding
        val mainLinearLayout = binding.mainLinearLayout
        // Table binding
        val typeTableRecyclerView = binding.typeTableRecyclerView
        // Info button binding
        val infoButton = binding.infoButton
        // GridView binding
        //val typeGridView = binding.typeGridView

        // Hides top bar
        supportActionBar?.hide()

        // Populates spinner options
        val povSpinnerOptions: Array<String> = resources.getStringArray(R.array.pov_spinner_options)
        attackingSpinnerTypeOptions = resources.getStringArray(R.array.spinner_type_options_1)
        defendingSpinnerType1Options = resources.getStringArray(R.array.spinner_type_options_1)
        val defendingSpinnerType2Options = resources.getStringArray(R.array.spinner_type_options_2)

        // Retrieve .json files
        fetchJson()

        // Night mode compatibility
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                println("night mode is on")
                mainLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector_night)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                println("night mode is not on")
                mainLinearLayout.background =
                    ContextCompat.getDrawable(this, R.drawable.main_header_selector)
            }
        }

        // TODO @@@ktg convert your TableView to a GridLayout (wait until a commit)
        //setContentView(R.layout.activity_main)
        val listOfCellBackgroundColors: MutableList<Int> = onesInt()
        val listOfCellTextColors: MutableList<Int> = onesInt()
        recyclerView = findViewById(R.id.typeTableRecyclerView)
        gridLayoutManager = GridLayoutManager(applicationContext, 3, LinearLayoutManager.VERTICAL,false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(true)
        arrayList = ArrayList()
        arrayList = setDataInList(arrayOfIcons,onesDouble(),listOfCellBackgroundColors,listOfCellTextColors)
        typeGridViewAdapter = TypeGridViewAdapter(applicationContext, arrayList!!)
        recyclerView?.adapter = typeGridViewAdapter

        // You'll have to do the enum conversion first

        // Gives the spinners their options
        setupSpinner(povSpinnerOptions, povSpinner)
        setupSpinner(attackingSpinnerTypeOptions, attackingTypeSpinner)
        setupSpinner(defendingSpinnerType1Options, defendingType1Spinner)
        setupSpinner(defendingSpinnerType2Options, defendingType2Spinner)

        var povSpinnerSelectedValue = 0
        var attackingType = 0
        var defendingType1 = 0
        var defendingType2 = 0
        var listOfInteractions: MutableList<Double> = onesDouble()
        var listOfInteractionsDualDefender: MutableList<String> = onesString()
        //var effectivenessList: MutableList<String> = interactionsToEffectiveness(onesDouble())

        // When the user selects an option in the povSpinner, onItemSelectedListener calls this object
        povSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {

                povSpinnerSelectedValue = p2

                // Resets spinner values
                attackingTypeSpinner.setSelection(0)
                defendingType1Spinner.setSelection(0)
                defendingType2Spinner.setSelection(0)
                gameSwitch.

                // Adjusts visibility of various objects
                makeVisibleIfTypeSelected(typeTableRecyclerView,0)
                makeVisibleIfTypeSelected(gameSwitch,0)
                makeVisibleIfTypeSelected(iceJiceSwitch,0)
                makeVisibleIfTypeSelected(tableHeader,0)
                makeVisibleIfTypeSelected(doesNotExistDisclaimer,0)
                when (povSpinnerSelectedValue) {
                    0 -> {
                        adjustVisibility(initialPrompt, 0)
                        adjustVisibility(typeSelectionPrompt, 1)
                        adjustVisibility(attackingTypeSpinnerAndLabel, 1)
                        adjustVisibility(defendingType1SpinnerAndLabel, 1)
                        adjustVisibility(defendingType2SpinnerAndLabel, 1)
                    }
                    1 -> {
                        adjustVisibility(initialPrompt, 2)
                        adjustVisibility(typeSelectionPrompt, 0)
                        adjustVisibility(attackingTypeSpinnerAndLabel, 0)
                        adjustVisibility(defendingType1SpinnerAndLabel, 2)
                        adjustVisibility(defendingType2SpinnerAndLabel, 2)
                        typeSelectionPrompt.text = resources.getString(R.string.attacking_prompt)
                    }
                    2 -> {
                        adjustVisibility(initialPrompt, 2)
                        adjustVisibility(typeSelectionPrompt, 0)
                        adjustVisibility(attackingTypeSpinnerAndLabel, 2)
                        adjustVisibility(defendingType1SpinnerAndLabel, 0)
                        adjustVisibility(defendingType2SpinnerAndLabel, 0)
                        typeSelectionPrompt.text = resources.getString(R.string.defending_prompt)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                attackingType = p2

                // Table header text adjustment
                tableHeader.text = resources.getString(
                    R.string.table_header_one_type,
                    defendingSpinnerType1Options[attackingType],
                    "_____"
                )

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
            // @@@nap function-ized much of it, some parts still duplicate but not sure if it's unavoidable/too pyrrhic to fix
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
                when (povSpinnerSelectedValue) {
                    1 -> {
                        listOfInteractions =
                            attackingEffectivenessCalculator(attackingType)
                    }
                    2 -> {
                        if (defendingType2 != 0 && defendingType1 != defendingType2) {
                            listOfInteractionsDualDefender =
                                defendingWithTwoTypesCalculator(defendingType1, defendingType2)
                            interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
                        } else {
                            if (defendingType1 == 0) {
                                listOfInteractions =
                                    defendingEffectivenessCalculator(defendingType1)
                            } else {
                                defendingEffectivenessCalculator(defendingType2)
                            }
                            interactionsToGridView(listOfInteractions)
                        }
                    }
                    else -> {}
                }
            }

            // Changes the switch's text between "Pokémon GO and Main Game)
            if (onSwitch) {
                gameSwitch.text = resources.getString((R.string.pogo))
            } else {
                gameSwitch.text = resources.getString((R.string.mainGame))
            }

            // Sends information to gridView depending on whether dual type is selected or not
            if (povSpinnerSelectedValue == 2 && defendingType1 !=0 && defendingType2 != 0 && defendingType1 != defendingType2) {
                interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
            } else {
                interactionsToGridView(listOfInteractions)
            }
        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            if (onSwitch) {iceJiceSwitch.text = getString(R.string.jice)}
            else {iceJiceSwitch.text = getString(R.string.ice)}

            // Sends information to gridView depending on whether dual type is selected or not
            if (povSpinnerSelectedValue == 2 && defendingType1 !=0 && defendingType2 != 0 && defendingType1 != defendingType2) {
                interactionsToGridViewDualDefender(listOfInteractionsDualDefender)
            } else {
                interactionsToGridView(listOfInteractions)
            }

        }

        infoButton.setOnClickListener {
            val intent = Intent(this, TypeTriviaActivity::class.java)
            startActivity(intent)
        }

    } // End of onCreate

    private fun setDataInList(iconMutableList: MutableList<Int>, effectivenessMutableList:MutableList<Double>,
                              backgroundColorList: MutableList<Int>, textColorList: MutableList<Int>):
            ArrayList<TypeGridView> {

        var items: ArrayList<TypeGridView> = ArrayList()
        for (i in 0 until 18) {
            items.add(
                TypeGridView(
                    iconMutableList[i],
                    effectivenessMutableList[i],
                    backgroundColorList[i],
                    textColorList[i]
                )
            )
        }

        if (iceJiceSwitch.isChecked()) {
            items[11] = TypeGridView(
                R.drawable.jice_icon,
                effectivenessMutableList[11],
                backgroundColorList[11],
                textColorList[11]
            )
        } else {
            items[11] = TypeGridView(
                R.drawable.ice_icon,
                effectivenessMutableList[11],
                backgroundColorList[11],
                textColorList[11]
            )
        }
        return items
    }

    fun interactionsToGridView(interactionsList: MutableList<Double>) {
        val effectivenessList = interactionsToEffectiveness(interactionsList)
        val displayedListOfInteractions = effectivenessToDisplayedCellValues(effectivenessList)
        val listOfCellTextColors = effectivenessToCellTextColors(effectivenessList)
        val listOfCellBackgroundColors = effectivenessToCellBackgroundColors(effectivenessList)
        arrayList = setDataInList(arrayOfIcons,displayedListOfInteractions,listOfCellBackgroundColors,listOfCellTextColors)
        typeGridViewAdapter = TypeGridViewAdapter(applicationContext, arrayList!!)
        recyclerView?.adapter = typeGridViewAdapter
    }

    fun interactionsToGridViewDualDefender(interactionsList: MutableList<String>) {
        val displayedListOfInteractions = effectivenessToDisplayedCellValues(interactionsList)
        val listOfCellTextColors = effectivenessToCellTextColors(interactionsList)
        val listOfCellBackgroundColors = effectivenessToCellBackgroundColors(interactionsList)
        arrayList = setDataInList(arrayOfIcons,displayedListOfInteractions,listOfCellBackgroundColors,listOfCellTextColors)
        typeGridViewAdapter = TypeGridViewAdapter(applicationContext, arrayList!!)
        recyclerView?.adapter = typeGridViewAdapter
    }

    fun adjustVisibility(selectedTextView: View, visibleInvisibleGone: Int) {
        when (visibleInvisibleGone) {
            0 -> selectedTextView.visibility = View.VISIBLE
            1 -> selectedTextView.visibility = View.INVISIBLE
            2 -> selectedTextView.visibility = View.GONE
        }
    }

    fun defendingWithTwoTypesCalculator(type1: Int, type2: Int): MutableList<String> {
        val defenderType1List = defendingEffectivenessCalculator(type1)
        val defenderType2List = defendingEffectivenessCalculator(type2)
        val defenderNetListOfStrings: MutableList<String> = arrayListOf()
        var defenderNetListOfDoubles: MutableList<Double> = arrayListOf()
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

    fun interactionsToEffectiveness(mutableList: MutableList<Double>): MutableList<String> {
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

    fun effectivenessToCellBackgroundColors(mutableList: MutableList<String>): MutableList<Int> {
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

    fun effectivenessToCellTextColors(mutableList: MutableList<String>): MutableList<Int> {
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

    fun adjustTableHeaderText(tableHeader: TextView, type1: Int, type2: Int) {
        if (type1 == 0 && type2 == 0) {
            adjustVisibility(tableHeader, 2)
        }
        if (type1 != 0 && type2 == 0) {
            adjustVisibility(tableHeader, 0)
            tableHeader.text = resources.getString(
                R.string.table_header_one_type,
                "_____",
                defendingSpinnerType1Options[type1]
            )
        }
        if (type1 == 0 && type2 != 0) {
            adjustVisibility(tableHeader, 0)
            tableHeader.text = resources.getString(
                R.string.table_header_one_type,
                "_____",
                defendingSpinnerType1Options[type2]
            )
        }
        if (type1 != 0 && type2 != 0 && type1 != type2) {
            adjustVisibility(tableHeader, 0)
            tableHeader.text = resources.getString(
                R.string.table_header_two_types,
                "_____",
                defendingSpinnerType1Options[type1],
                defendingSpinnerType1Options[type2]
            )
        }
        if (type1 != 0 && type1 == type2) {
            adjustVisibility(tableHeader, 0)
            tableHeader.text = resources.getString(
                R.string.table_header_one_type,
                "_____",
                defendingSpinnerType1Options[type1]
            )
        }
    }

    // @@@ktg yah nah
    // I would shoot you if you submitted this to PR
    // instantly

    // @@@nap bet
    fun checkIfTypingExists(type1: Int, type2: Int) {
        if (type1 != 0 && type2 != 0 && type1 != type2 &&
            ((type1 == 13 && type2 == 12)
            || (type1 == 13 && type2 == 14)
            || (type1 == 13 && type2 == 1)
            || (type1 == 13 && type2 == 16)
            || (type1 == 13 && type2 == 9)
            || (type1 == 13 && type2 == 17)
            || (type1 == 7 && type2 == 5)
            || (type1 == 7 && type2 == 8)
            || (type1 == 4 && type2 == 6)
            || (type1 == 12 && type2 == 14)
            || (type1 == 6 && type2 == 11)
            || (type1 == 6 && type2 == 5)
            || (type1 == 14 && type2 == 17)
            || (type1 == 11 && type2 == 8)
            || (type1 == 1 && type2 == 3)
            || (type1 == 1 && type2 == 2)
            || (type1 == 16 && type2 == 9)
            || (type1 == 12 && type2 == 13)
            || (type1 == 14 && type2 == 13)
            || (type1 == 1 && type2 == 13)
            || (type1 == 16 && type2 == 13)
            || (type1 == 9 && type2 == 13)
            || (type1 == 17 && type2 == 13)
            || (type1 == 5 && type2 == 7)
            || (type1 == 8 && type2 == 7)
            || (type1 == 6 && type2 == 4)
            || (type1 == 14 && type2 == 12)
            || (type1 == 11 && type2 == 6)
            || (type1 == 5 && type2 == 6)
            || (type1 == 17 && type2 == 14)
            || (type1 == 8 && type2 == 11)
            || (type1 == 3 && type2 == 1)
            || (type1 == 2 && type2 == 1)
            || (type1 == 9 && type2 == 16)
        )) {
            adjustVisibility(doesNotExistDisclaimer, 0)
        } else {
            adjustVisibility(doesNotExistDisclaimer, 1)
        }
    }

    fun makeVisibleIfTypeSelected(tableHeader: View, type1: Int, type2: Int = 0) {
        if (type1 != 0 || type2 != 0) {
            adjustVisibility(tableHeader, 0)
        } else {
            adjustVisibility(tableHeader,1)
        }
    }

    // @@@ktg there's an easier way to instantiate a list of the same value
    // hint: loops/in-line functions

    fun attackingEffectivenessCalculator(attacker: Int): MutableList<Double> {
        if (attacker == 0) {
            return onesDouble()
        }
        var dictOfSelectedTypes: Map<String, Double> = emptyMap()

        // @@@nap LFG

        val attackerType: String = defendingSpinnerType1Options[attacker]

        for (moveType in PokemonType.values()) {
            if (attackerType == moveType.type) {
                dictOfSelectedTypes = typeMatchups.getValue(moveType)
            }
        }
        return dictOfSelectedTypes.values.toMutableList()
    }

    // Returns a mutable list for how one type defends against all other types
    fun defendingEffectivenessCalculator(defender: Int): MutableList<Double> {
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

    var arrayOfIcons: MutableList<Int> = mutableListOf(
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
}