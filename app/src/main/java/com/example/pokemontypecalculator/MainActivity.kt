package com.example.pokemontypecalculator

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pokemontypecalculator.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException

//class MainActivity(val TypeMatchups: TypeMatchupsClass) : AppCompatActivity() {
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    // @@@ktg class-level variables are necessary sometimes, but not preferred - in almost all cases in CS, we want
    // to give things as limited of scope as possible (ideally, variables are scope to a function, for instance).
    // Some of these class-level (aka "member variables") are probably necessary. But I know at least one isn't.
    // After you do other refactoring (Enums, especially), come back and look at these. Can you make one or more of them
    // more tightly scoped (i.e. scoped to something less than the class, like a function)?

    // And also see if you have lateinit vars after that refactor, see if you can get rid of the lateinit property
    // of any remaining class-level variables
    private lateinit var typeMatchups: TypeMatchups
    // private lateinit var pokemonType: PokemonType

    @SuppressLint("UseSwitchCompatOrMaterialCode") //fvghcdfgh
    private lateinit var gameSwitch: Switch

    private lateinit var povSpinnerOptions: Array<String>

    private var arrayWithCellID = listOf<TextView>()

    var spinnerTypeOptions1 = arrayOf<String>()

    // @@@ktg break this out into several smaller functions that get called from onCreate
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        spinnerTypeOptions1 = resources.getStringArray(R.array.spinner_type_options_1)
        // Gathering arrays for the spinners (is this initializing or calling?)
        povSpinnerOptions = resources.getStringArray(R.array.pov_spinner_options)
        val spinnerTypeOptions2 = resources.getStringArray(R.array.spinner_type_options_2)
        // Gathering spinners via bindings
        val povSpinner = binding.povSpinner
        val defendingType1Spinner = binding.type1Spinner
        val defendingType2Spinner = binding.type2Spinner
        // Gathering strings (prompts, headers, labels) via bindings
        val initialPrompt = binding.initialPrompt
        val typeSelectionPrompt = binding.secondPrompt
        val tableHeader = binding.tableHeader
        val doesNotExistDisclaimer = binding.doesNotExistDisclaimer
        val type1Label = binding.type1Label
        // Gathering groups of objects (spinner and label as one) via bindings
        val type1SpinnerAndLabel = binding.type1SpinnerAndLabel
        val type2SpinnerAndLabel = binding.type2SpinnerAndLabel

        // Retrieve .json files
        fetchJson()


        // Gathering switch via bindings
        gameSwitch = binding.gameSwitch

        //@@@ktg convert your TableView to a GridLayout
        // You'll have to do the enum conversion first
        arrayWithCellID = listOf(
            binding.r1column1b,
            binding.r2column1b,
            binding.r3column1b,
            binding.r4column1b,
            binding.r5column1b,
            binding.r6column1b,
            binding.r1column2b,
            binding.r2column2b,
            binding.r3column2b,
            binding.r4column2b,
            binding.r5column2b,
            binding.r6column2b,
            binding.r1column3b,
            binding.r2column3b,
            binding.r3column3b,
            binding.r4column3b,
            binding.r5column3b,
            binding.r6column3b,
        )

        // Gives the spinners their options
        setupSpinner(povSpinnerOptions, povSpinner)
        setupSpinner(spinnerTypeOptions1, defendingType1Spinner)
        setupSpinner(spinnerTypeOptions2, defendingType2Spinner)

        var povSpinnerSelectedValue = 0
        var type1SelectedValue = 0
        var type2SelectedValue = 0
        var listOfInteractions: MutableList<Double>
        var stringListOfInteractions: MutableList<String> = doubleListToStringList(ones())

        // When the user selects an option in the povSpinner, onItemSelectedListener calls this object
        povSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                povSpinnerSelectedValue = p2
                tableHeader.visibility = View.INVISIBLE
                doesNotExistDisclaimer.visibility = View.INVISIBLE
                // When the app starts, povSpinner is at p2 == 0 by default
                if (povSpinnerSelectedValue == 0) {
                    // Makes initialPrompt visible
                    initialPrompt.visibility = View.VISIBLE
                    // secondPrompt, both the type spinners, and their respective labels are all invisible here so that the user knows to only address initialPrompt
                    typeSelectionPrompt.visibility = View.INVISIBLE
                    type1SpinnerAndLabel.visibility = View.INVISIBLE
                    type2SpinnerAndLabel.visibility = View.INVISIBLE
                    tableHeader.visibility = View.INVISIBLE
                }
                // If 1 of the 2 options have been selected
                if (povSpinnerSelectedValue != 0) {
                    // initialPrompt goes away and secondPrompt becomes visible
                    initialPrompt.visibility = View.INVISIBLE
                    typeSelectionPrompt.visibility = View.VISIBLE
                    // The spinner and label for the first type become visible
                    type1SpinnerAndLabel.visibility = View.VISIBLE
                    // Resets the values for the type spinners
                    defendingType1Spinner.setSelection(0)
                    defendingType2Spinner.setSelection(0)
                    // Hides the heading for the table
                    tableHeader.visibility = View.INVISIBLE
                }
                // If "Attacking (1 type)" is selected
                if (povSpinnerSelectedValue == 1) {
                    // Text for secondPrompt switches to the attacking prompt
                    typeSelectionPrompt.text = resources.getString(R.string.attacking_prompt)
                    // Label reads "Move type" for attack move, as opposed to "First type" used when defending
                    type1Label.text = resources.getString(R.string.move_type)
                    // The spinner and label for the second type disappears, since you cannot attack with more than one type at a time. "GONE" is used rather than "INVISIBLE" to center the sole move type spinner
                    type2SpinnerAndLabel.visibility = View.GONE
                    // Hides the heading for the table
                    tableHeader.visibility = View.INVISIBLE


                }
                // If "Defending (1 or 2 types)" is selected
                if (povSpinnerSelectedValue == 2) {
                    // Text for secondPrompt switches to the Attacking prompt
                    typeSelectionPrompt.text = resources.getString(R.string.defending_prompt)
                    // Label reads "First type" for defending Pokemon, as opposed to "Move type" used when attacking
                    type1Label.text = resources.getString(R.string.first_type)
                    type2SpinnerAndLabel.visibility = View.VISIBLE
                    // Hides the heading for the table
                    tableHeader.visibility = View.INVISIBLE

                }
            }

            //onNothingSelected will never be called, but onItemSelectedListener will call an error if it does not exist
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        
        gameSwitch.setOnCheckedChangeListener { compoundButton, onSwitch ->
            if (povSpinnerSelectedValue == 1) {
                listOfInteractions = attackingEffectivenessCalculator(type1SelectedValue)
                stringListOfInteractions = doubleListToStringList(listOfInteractions)
            }
            if (povSpinnerSelectedValue == 2) {
                if (type2SelectedValue == 0 || type1SelectedValue == type2SelectedValue) {
                    listOfInteractions = defendingEffectivenessCalculator(type1SelectedValue)
                    stringListOfInteractions = doubleListToStringList(listOfInteractions)
                }
                if (type2SelectedValue != 0 && type1SelectedValue != type2SelectedValue) {
                    stringListOfInteractions =
                        defendingWithTwoTypesCalculator(type1SelectedValue, type2SelectedValue)
                }
            }
            changeCellColors(stringListOfInteractions)
            changeCellValues(stringListOfInteractions)
            if (onSwitch) {
                gameSwitch.text = resources.getString((R.string.pogo))
            } else {
                gameSwitch.text = resources.getString((R.string.mainGame))
            }
        }

        // When the user selects an option in the first type spinner, onItemSelectedListener calls this object
        defendingType1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //@@@ktg duplidate code = function-ize
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                type1SelectedValue = p2

                // Table header visibility
                if (type1SelectedValue == 0 && type2SelectedValue == 0) {
                    tableHeader.visibility = View.INVISIBLE
                }
                if (type1SelectedValue != 0 || type2SelectedValue != 0) {
                    tableHeader.visibility = View.VISIBLE
                }

                // If attacking
                if (povSpinnerSelectedValue == 1) {
                    // Changes the tableHeader text to the attacking prompt if povSpinner is set to "Attacking (1 type)"
                    (resources.getString(R.string.table_header_attacking_part_1) + spinnerTypeOptions1[p2] + resources.getString(
                        R.string.table_header_attacking_part_2
                    )).also { tableHeader.text = it }

                    listOfInteractions = attackingEffectivenessCalculator(type1SelectedValue)
                    stringListOfInteractions = doubleListToStringList(listOfInteractions)
                    changeCellValues(stringListOfInteractions)
                    changeCellColors(stringListOfInteractions)
                }

                // If defending
                if (povSpinnerSelectedValue == 2) {
                    if (type2SelectedValue == 0 || type1SelectedValue == type2SelectedValue) {
                        doesNotExistDisclaimer.visibility = View.INVISIBLE
                        // Adjusts table heading
                        (resources.getString(R.string.table_header_defending_part_1) +
                                spinnerTypeOptions1[type1SelectedValue] +
                                resources.getString(R.string.table_header_defending_part_3)).also {
                            tableHeader.text = it
                        }
                        listOfInteractions = defendingEffectivenessCalculator(type1SelectedValue)
                        stringListOfInteractions = doubleListToStringList(listOfInteractions)
                        changeCellValues(stringListOfInteractions)
                        changeCellColors(stringListOfInteractions)
                    }

                    if (type2SelectedValue != 0 && type1SelectedValue != type2SelectedValue) {
                        if (checkIfTypingExists(type1SelectedValue, type2SelectedValue)) {
                            doesNotExistDisclaimer.visibility = View.VISIBLE
                        } else {doesNotExistDisclaimer.visibility = View.INVISIBLE}
                        // Adjusts table heading
                        (resources.getString(R.string.table_header_defending_part_1) +
                                spinnerTypeOptions1[type1SelectedValue] +
                                resources.getString(R.string.table_header_defending_part_2) +
                                spinnerTypeOptions2[type2SelectedValue] +
                                resources.getString(R.string.table_header_defending_part_3)).also {
                            tableHeader.text = it
                        }
                        stringListOfInteractions =
                            defendingWithTwoTypesCalculator(type1SelectedValue, type2SelectedValue)
                        changeCellValues(stringListOfInteractions)
                        changeCellColors(stringListOfInteractions)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Called when user selects an option in the second type spinner
        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // When an option is selected
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                type2SelectedValue = p2
                if (checkIfTypingExists(type1SelectedValue, type2SelectedValue)) {
                    doesNotExistDisclaimer.visibility = View.VISIBLE
                } else {doesNotExistDisclaimer.visibility = View.INVISIBLE}
                // Table header formatting
                if (type2SelectedValue != 0 && type1SelectedValue == 0) {
                    // Changes the tableHeader text to the attacking prompt if povSpinner is set to "Attacking (1 type)"
                    tableHeader.text = resources.getString(
                        R.string.table_header_defending,
                        "_____",
                        spinnerTypeOptions2[type2SelectedValue]
                    )
                }
                if (type2SelectedValue == 0 || type1SelectedValue == type2SelectedValue) {
                    // Updates the table header so that there is no "/" to separate the dual typing, since there is only the first type
                    tableHeader.text =
                        resources.getString(
                            R.string.table_header_defending,
                            "_____",
                            spinnerTypeOptions1[type1SelectedValue]
                        )
                    // Gets the coefficients for the matchup
                    listOfInteractions = defendingEffectivenessCalculator(type2SelectedValue)
                    stringListOfInteractions = doubleListToStringList(listOfInteractions)
                    changeCellColors(stringListOfInteractions)
                    changeCellValues(stringListOfInteractions)
                }
                if (type2SelectedValue != 0 && type1SelectedValue != type2SelectedValue) {
                    // Updates the table header by adding a "/" to separate the dual types
                    (resources.getString(R.string.table_header_defending_part_1) +
                            spinnerTypeOptions1[type1SelectedValue] +
                            resources.getString(R.string.table_header_defending_part_2) +
                            spinnerTypeOptions2[type2SelectedValue] +
                            resources.getString(R.string.table_header_defending_part_3)).also {
                        tableHeader.text = it
                    }
                    stringListOfInteractions =
                        defendingWithTwoTypesCalculator(type1SelectedValue, type2SelectedValue)
                    changeCellColors(stringListOfInteractions)
                    changeCellValues(stringListOfInteractions)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

    } // End of onCreate

    //This function is in onCreate because it uses gameSwitch, which is bound (via .binding) in onCreate
    fun defendingWithTwoTypesCalculator(type1: Int, type2: Int): MutableList<String> {
        val defenderType1List = defendingEffectivenessCalculator(type1)
        val defenderType2List = defendingEffectivenessCalculator(type2)
        val defenderNetList: MutableList<String> = arrayListOf()
        // @@@ktg find a way to simplify this
        // Just use PoGo numbers
        for (i in 0 until 18) {

            // Ultra super effective
            // (4x or 2.56x)
            // (1 possible permutation)
            if ((defenderType1List[i] == 1.6) && (defenderType2List[i] == 1.6)) {
                defenderNetList.add(Effectiveness.USE.impact)
            }

            // Super effective
            // (2x or 1.6x)
            // (2 possible permutations)
            if ((defenderType1List[i] == 1.6) && (defenderType2List[i] == 1.0)
                || (defenderType1List[i] == 1.0) && (defenderType2List[i] == 1.6)
            ) {
                defenderNetList.add(Effectiveness.SE.impact)
            }

            // Effective
            // (1x or 1x)
            // (3 possible permutations)
            if ((defenderType1List[i] == 1.6) && (defenderType2List[i] == 0.625)
                || (defenderType1List[i] == 1.0) && (defenderType2List[i] == 1.0)
                || (defenderType1List[i] == 0.625) && (defenderType2List[i] == 1.6)
            ) {
                defenderNetList.add(Effectiveness.E.impact)
            }

            // Not very effective
            // (.5x or .625x)
            // (2 possible permutations)
            if (((defenderType1List[i] == 1.6) && (defenderType2List[i] == 0.390625) && (gameSwitch.isChecked))
                || ((defenderType1List[i] == 1.0) && (defenderType2List[i] == 0.625))
                || ((defenderType1List[i] == 0.625) && (defenderType2List[i] == 1.0))
                || ((defenderType1List[i] == 0.390625) && (defenderType2List[i] == 1.6) && (!gameSwitch.isChecked))
            ) {
                defenderNetList.add(Effectiveness.NVE.impact)
            }

            // Type interactions lower than .5x are different in Pokemon Go than the main game

            // Ultra not very effective
            // (.25x or .390625x)
            // (1 permutation in main game, 2 in PoGo)
            if (((defenderType1List[i] == 0.625) && (defenderType2List[i] == 0.625))
                || ((defenderType1List[i] == 1.0) && (defenderType2List[i] == 0.390625) && (gameSwitch.isChecked))
                || ((defenderType1List[i] == .390625) && (defenderType2List[i] == 1.0) && (gameSwitch.isChecked))
            ) {
                defenderNetList.add(Effectiveness.UNVE.impact)
            }

            // Does not effect
            // (0x in main game, not possible in PoGo)
            // (7 permutations for main game, 0 in PoGo)
            if (((defenderType1List[i] == 0.390625) && (!gameSwitch.isChecked))
                || ((defenderType2List[i] == 0.390625) && (!gameSwitch.isChecked))
            ) {
                defenderNetList.add(Effectiveness.DNE.impact)
            }

            // Ultra does not effect
            // (Not possible in main game, .244x in PoGo)
            // (0 permutations in main game, 2 in PoGo)
            if (((defenderType1List[i] == 0.625) && (defenderType2List[i] == 0.390625) && (gameSwitch.isChecked))
                || ((defenderType1List[i] == 0.390625) && (defenderType2List[i] == 0.625) && (gameSwitch.isChecked))
            ) {
                defenderNetList.add(Effectiveness.UDNE.impact)
            }
        }
        return (defenderNetList)
    }

    //This function is in onCreate because it uses gameSwitch, which is bound (via .binding) in onCreate
    fun doubleListToStringList(mutableList: MutableList<Double>): MutableList<String> {
        val stringList: MutableList<String> = mutableListOf()
        for (i in 0 until 18) {
            when (mutableList[i]) {
                1.6 -> stringList.add(Effectiveness.SE.impact)
                1.0 -> stringList.add(Effectiveness.E.impact)
                0.625 -> stringList.add(Effectiveness.NVE.impact)
            }
            if (gameSwitch.isChecked) {
                when (mutableList[i]) {
                    0.390625 -> stringList.add(Effectiveness.UNVE.impact)
                }
            } else {
                when (mutableList[i]) {
                    0.390625 -> stringList.add(Effectiveness.DNE.impact)
                }
            }
        }
        return stringList
    }

    @SuppressLint("SetTextI18n")
    private fun changeCellValues(mutableList: MutableList<String>) {
        for (i in 0 until 18) {
            val textView = arrayWithCellID[i]
            if (gameSwitch.isChecked == false) {
                when (mutableList[i]) {
                    "E" -> textView.text = "1.0"
                    "SE" -> textView.text = "2.0"
                    "USE" -> textView.text = "4.0"
                    "NVE" -> textView.text = "0.5"
                    "UNVE" -> textView.text = "0.25"
                    "DNE" -> textView.text = "0"
                }
            } else {
                when (mutableList[i]) {
                    "E" -> textView.text = "1.0"
                    "SE" -> textView.text = "1.6"
                    "USE" -> textView.text = "2.56"
                    "NVE" -> textView.text = "0.625"
                    "UNVE" -> textView.text = "0.39"
                    "UDNE" -> textView.text = "0.244"
                }
            }
        }


        // End of onCreate
    }

    private fun ones(): MutableList<Double> {
        val table = mutableListOf<Double>()
        for (i in 0 until 18) {
            table.add(1.0)
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
                typeMatchups = gson.fromJson(body, TypeMatchups::class.java)
            }

            override fun onFailure(call: Call, e: IOException) {
                println("Failed to call request")
            }
        })
    }

    // Functions for changing the text in the table cells
    // @@@ktg all these functions look ~eerily~ similar - can we make one function that does all these things?
    // Probably need to add a couple parameters to do so

    // Turned six functions into two
    // Added the mutable list as a parameter
    fun changeCellColors(mutableList: MutableList<String>) {
        for (i in 0 until 18) {
            val textView = arrayWithCellID[i]
            when (mutableList[i]) {
                "E" -> textView.background = ContextCompat.getDrawable(this, R.color.x1color)
                "SE" -> textView.background = ContextCompat.getDrawable(this, R.color.x2color)
                "USE" -> textView.background = ContextCompat.getDrawable(this, R.color.x4color)
                "NVE" -> textView.background = ContextCompat.getDrawable(this, R.color.x_5color)
                "UNVE" -> textView.background = ContextCompat.getDrawable(this, R.color.x_25color)
                "DNE" -> textView.background = ContextCompat.getDrawable(this, R.color.x0color)
                "UDNE" -> textView.background = ContextCompat.getDrawable(this, R.color.UDNEColor)
            }
            if ((mutableList[i] == "DNE") || (mutableList[i] == "UDNE")) {
                textView.setTextColor(getColor(R.color.white))
            } else {
                textView.setTextColor(getColor(R.color.black))
            }
        }
    }

    // @@@ktg yah nah
    // I would shoot you if you submitted this to PR
    // instantly

    // @@@nap I will address this later; not a priority issue
    fun checkIfTypingExists(type1: Int, type2: Int): Boolean {
        return ((type1 == 13 && type2 == 12)
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
                || (type1 == 6 && type2 == 8)
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
                || (type1 == 8 && type2 == 6)
                || (type1 == 17 && type2 == 14)
                || (type1 == 8 && type2 == 11)
                || (type1 == 3 && type2 == 1)
                || (type1 == 2 && type2 == 1)
                || (type1 == 9 && type2 == 16))
    }

    // Functions for retrieving the matchup interactions
    // Returns a mutable list for how one type attacks all other types
    // @@@ktg there's an easier way to instantiate a list of the same value
    // hint: loops/in-line functions
    fun attackingEffectivenessCalculator(attacker: Int): MutableList<Double> {
        if (attacker == 0) {
            return ones()
        }
        var dictOfSelectedTypes: Map<String, Double> = typeMatchups.Bug
        // @@@ktg do some research into Enums - the whole project can be refactored (change how this works in code) a fair bit to use a Type enum
        //use enum values as keys, so that when you loop through them
        val attackerType: String = spinnerTypeOptions1[attacker]
        /*for (type in pokemonType.type) {
            if (attackerType == pokemonType.type) {
                dictOfSelectedTypes = typeMatchups.TODO(Fix this)//
            }
        }*/
        if (attackerType == "Bug") {
            dictOfSelectedTypes = typeMatchups.Bug
        }
        if (attackerType == "Dark") {
            dictOfSelectedTypes = typeMatchups.Dark
        }
        if (attackerType == "Dragon") {
            dictOfSelectedTypes = typeMatchups.Dragon
        }
        if (attackerType == "Electric") {
            dictOfSelectedTypes = typeMatchups.Electric
        }
        if (attackerType == "Fairy") {
            dictOfSelectedTypes = typeMatchups.Fairy
        }
        if (attackerType == "Fighting") {
            dictOfSelectedTypes = typeMatchups.Fighting
        }
        if (attackerType == "Fire") {
            dictOfSelectedTypes = typeMatchups.Fire
        }
        if (attackerType == "Flying") {
            dictOfSelectedTypes = typeMatchups.Flying
        }
        if (attackerType == "Ghost") {
            dictOfSelectedTypes = typeMatchups.Ghost
        }
        if (attackerType == "Grass") {
            dictOfSelectedTypes = typeMatchups.Grass
        }
        if (attackerType == "Ground") {
            dictOfSelectedTypes = typeMatchups.Ground
        }
        if (attackerType == "Ice") {
            dictOfSelectedTypes = typeMatchups.Ice
        }
        if (attackerType == "Normal") {
            dictOfSelectedTypes = typeMatchups.Normal
        }
        if (attackerType == "Poison") {
            dictOfSelectedTypes = typeMatchups.Poison
        }
        if (attackerType == "Psychic") {
            dictOfSelectedTypes = typeMatchups.Psychic
        }
        if (attackerType == "Rock") {
            dictOfSelectedTypes = typeMatchups.Rock
        }
        if (attackerType == "Steel") {
            dictOfSelectedTypes = typeMatchups.Steel
        }
        if (attackerType == "Water") {
            dictOfSelectedTypes = typeMatchups.Water
        }
        return dictOfSelectedTypes.values.toMutableList()
    }

    // Returns a mutable list for how one type defends against all other types
    fun defendingEffectivenessCalculator(defender: Int): MutableList<Double> {
        if (defender == 0) {
            return ones()
        }
        val listOfDefendingMatchupCoefficients: MutableList<Double> = arrayListOf()
        val defendingType: String = spinnerTypeOptions1[defender]

        /*
        // These lines of code simplify the ones following it, but I don't know how to
        // restructure my data so that I can pull from it more effectively

        for (type in pokemonType.type) {
            listOfDefendingMatchupCoefficients.add(typeMatchups.type.getValue(defendingType))
        }
        TODO(Think on how to structure data to appropriately cut code down)

        */

        // @@@ktg :thinking-face: enum eyah

        listOfDefendingMatchupCoefficients.add(typeMatchups.Bug.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Dark.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Dragon.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Electric.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Fairy.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Fighting.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Fire.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Flying.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Ghost.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Grass.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Ground.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Ice.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Normal.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Poison.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Psychic.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Rock.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Steel.getValue(defendingType))
        listOfDefendingMatchupCoefficients.add(typeMatchups.Water.getValue(defendingType))
        return listOfDefendingMatchupCoefficients
    }
}
//kevin's scratch
/*
    enum class Types(name: String) {
        NORMAL("normal"),
        ROCK("rock"),
        //etc
    }

data class DictEffectiveness(
        val typeDict: Map<String, Float>
    )

    //dictionary approach
    fun fillMyChartThing() {
        val typeEffectivenessDict = mapOf(Pair("normal", 1.0), Pair("poison", 1.0)) // typeEffectivenessDict = { "normal" : 1.0 }
        for (type in typeEffectivenessDict) {
            val chartId = resources.getIdentifier(type.key, "id", packageName)
            val chartCell = findViewById<>(chartId)
            chartCell.value = type.value //1.0
        }
    }
*/
/*









    //discrete properties approach
    data class EffectivenessMatrix(
        val normal: Float,
        val poison: Float,
        //...etc
    )

    fun fillMyChart() {
        val typeEffectiveness = getEffectivenessMatrix() // EffectivenessMatrix(normal: 1.0, poison 1.0)

        val normalView = findViewById<>(R.id.normalThing)
        val poisonView = findViewById<>(R.id.poisonThing)

        normalView.value = typeEffectiveness.normal
        poisonView.value = typeEffectiveness.poison
    }*/