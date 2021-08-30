package com.example.pokemontypecalculator

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
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

    private lateinit var povSpinnerOptions: Array<String>

    private var arrayWithCellID = listOf<TextView>()

    var spinnerTypeOptions1 = arrayOf<String>()

    // @@@ktg break this out into several smaller functions that get called from onCreate
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
        val type1Spinner = binding.type1Spinner
        val type2Spinner = binding.type2Spinner
        // Gathering strings (prompts, headers, labels) via bindings
        val initialPrompt = binding.initialPrompt
        val typeSelectionPrompt = binding.secondPrompt
        val tableHeader = binding.tableHeader
        val doesNotExistDisclaimer = binding.doesNotExistDisclaimer
        val type1Label = binding.type1Label
        // Gathering groups of objects (spinner and label as one) via bindings
        val type1SpinnerAndLabel = binding.type1SpinnerAndLabel
        val type2SpinnerAndLabel = binding.type2SpinnerAndLabel


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
        setupSpinner(spinnerTypeOptions1, type1Spinner)
        setupSpinner(spinnerTypeOptions2, type2Spinner)

        var povSpinnerSelectedValue = 0
        var type1SelectedValue = 0
        var type2SelectedValue = 0
        var listOfInteractions: MutableList<Double>

        // When the user selects an option in the povSpinner, onItemSelectedListener calls this object
        povSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                povSpinnerSelectedValue = p2
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
                    type1Spinner.setSelection(0)
                    type2Spinner.setSelection(0)
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

        // When the user selects an option in the first type spinner, onItemSelectedListener calls this object
        type1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //@@@ktg duplidate code = function-ize
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                type1SelectedValue = p2
                // Makes the table header invisible
                if (type1SelectedValue == 0 && type2SelectedValue == 0) {
                    tableHeader.visibility = View.INVISIBLE
                }
                // Makes the table header visible
                if (type1SelectedValue != 0 || type2SelectedValue != 0) {
                    tableHeader.visibility = View.VISIBLE
                }
                // If attacking
                if (povSpinnerSelectedValue == 1) {
                    // Changes the tableHeader text to the attacking prompt if povSpinner is set to "Attacking (1 type)"
                    (resources.getString(R.string.table_header_attacking_part_1) + spinnerTypeOptions1[p2] + resources.getString(
                        R.string.table_header_attacking_part_2
                    )).also { tableHeader.text = it }
                    // Gets the list of type interactions
                    listOfInteractions = attackingEffectivenessCalculator(p2)
                    // Places the list into the graph
                    for (i in 0 until 18) {
                        arrayWithCellID[i].text = listOfInteractions[i].toString()
                        when (listOfInteractions[i]) {
                            1.0 -> makeCellEffective(i)
                            1.6 -> makeCellSuperEffective(i)
                            0.625 -> makeCellNotVeryEffective(i)
                            0.390625 -> makeCellDoesNotEffect(i)
                        }
                    }
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
                        // Gets the coefficients for the matchup
                        listOfInteractions = defendingEffectivenessCalculator(p2)
                        // Places the coefficients in the table
                        for (i in 0 until 18) {
                            arrayWithCellID[i].text = listOfInteractions[i].toString()
                            when (listOfInteractions[i]) {
                                1.0 -> makeCellEffective(i)
                                1.6 -> makeCellSuperEffective(i)
                                0.625 -> makeCellNotVeryEffective(i)
                                0.390625 -> makeCellDoesNotEffect(i)
                            }
                        }
                        // Updates the tableHeader text to reflect a second type
                    }
                    if (type2SelectedValue != 0 && type1SelectedValue != type2SelectedValue) {
                            if (checkIfTypingExists(type1SelectedValue, type2SelectedValue)) {
                                doesNotExistDisclaimer.visibility = View.VISIBLE
                            }
                        // Adjusts table heading
                        (resources.getString(R.string.table_header_defending_part_1) +
                                spinnerTypeOptions1[type1SelectedValue] +
                                resources.getString(R.string.table_header_defending_part_2) +
                                spinnerTypeOptions2[type2SelectedValue] +
                                resources.getString(R.string.table_header_defending_part_3)).also {
                            tableHeader.text = it
                        }
                        // Gets the coefficients for the matchup
                        var listOfInteractionsType1 = defendingEffectivenessCalculator(type1SelectedValue)
                        var listOfInteractionsType2 = defendingEffectivenessCalculator(type2SelectedValue)

                        var listOfInteractions: MutableList<Double> = arrayListOf()
                        for (i in 0 until 18) {
                            // Determines the net coefficient from both types
                            when (listOfInteractionsType1[i]) {
                                1.0 -> listOfInteractions.add(listOfInteractionsType2[i])
                                1.6 -> when (listOfInteractionsType2[i]) {
                                    0.390625 -> listOfInteractions.add(0.625)
                                    0.625 -> listOfInteractions.add(1.0)
                                    1.0 -> listOfInteractions.add(1.6)
                                    1.6 -> listOfInteractions.add(2.56)
                                }
                                0.625 -> when (listOfInteractionsType2[i]) {
                                    0.390625 -> listOfInteractions.add(0.244140625)
                                    // The value below is changed to reflect the main game.
                                    // In PoGo, 2 types that are not very effective combined yield 1/4 damage, not zero damage.
                                    0.625 -> listOfInteractions.add(.40)
                                    1.0 -> listOfInteractions.add(0.625)
                                    1.6 -> listOfInteractions.add(1.0)
                                }
                                0.390625 -> when (listOfInteractionsType2[i]) {
                                    0.390625 -> listOfInteractions.add(.15258789062) // This permutation cannot exist
                                    0.625 -> listOfInteractions.add(.244140625)
                                    1.0 -> listOfInteractions.add(0.390625)
                                    1.6 -> listOfInteractions.add(0.625)
                                }
                            }
                            // Places the coefficients in the table
                            arrayWithCellID[i].text = listOfInteractions[i].toString()
                            when (listOfInteractions[i]) {
                                1.0 -> makeCellEffective(i)
                                1.6 -> makeCellSuperEffective(i)
                                2.56 -> makeCellUltraSuperEffective(i)
                                0.625 -> makeCellNotVeryEffective(i)
                                0.40 -> makeCellUltraNotVeryEffective(i)
                                0.390625 -> makeCellDoesNotEffect(i)
                                0.224140625 -> makeCellDoesNotEffect(i)
                                0.15258789062 -> makeCellDoesNotEffect(i) // This permutation cannot exist
                            }
                        }
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // When the user selects an option in the second type spinner, onItemSelectedListener calls this object
        type2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // When an option is selected
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                type2SelectedValue = p2
                // If the second type is [none] or if both types are the same
                if (type2SelectedValue != 0 && type1SelectedValue == 0) {
                    // Changes the tableHeader text to the attacking prompt if povSpinner is set to "Attacking (1 type)"
                    tableHeader.text = resources.getString(R.string.table_header_defending, "_____", spinnerTypeOptions2[type2SelectedValue])
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
                    listOfInteractions = defendingEffectivenessCalculator(p2)
                    // Places the coefficients in the table
                    for (i in 0 until 18) {
                        arrayWithCellID[i].text = listOfInteractions[i].toString()
                        when (listOfInteractions[i]) {
                            1.0 -> makeCellEffective(i)
                            1.6 -> makeCellSuperEffective(i)
                            0.625 -> makeCellNotVeryEffective(i)
                            0.390625 -> makeCellDoesNotEffect(i)
                        }
                    }
                }
                //attackEffectivenessCalculator(type1SelectedValue,type2SelectedValue)
                if (type2SelectedValue != 0 && type1SelectedValue != type2SelectedValue) {
                    // Updates the table header by adding a "/" to separate the dual types
                    (resources.getString(R.string.table_header_defending_part_1) +
                            spinnerTypeOptions1[type1SelectedValue] +
                            resources.getString(R.string.table_header_defending_part_2) +
                            spinnerTypeOptions2[type2SelectedValue] +
                            resources.getString(R.string.table_header_defending_part_3)).also {
                        tableHeader.text = it
                    }
                    var listOfInteractionsType1 = defendingEffectivenessCalculator(type1SelectedValue)
                    var listOfInteractionsType2 = defendingEffectivenessCalculator(type2SelectedValue)
                    var listOfInteractions: MutableList<Double> = arrayListOf()
                    // @@@ktg find a way to simplify this
                    // Just use PoGo numbers
                    for (i in 0 until 18) {
                        // Determines the net coefficient from both types
                        when (listOfInteractionsType1[i]) {
                            1.0 -> listOfInteractions.add(listOfInteractionsType2[i])
                            1.6 -> when (listOfInteractionsType2[i]) {
                                0.390625 -> listOfInteractions.add(0.625)
                                0.625 -> listOfInteractions.add(1.0)
                                1.0 -> listOfInteractions.add(1.6)
                                1.6 -> listOfInteractions.add(2.56)
                            }
                            0.625 -> when (listOfInteractionsType2[i]) {
                                0.390625 -> listOfInteractions.add(0.244140625)
                                // The value below is changed to reflect the main game.
                                // In PoGo, 2 types that are not very effective combined yield 1/4 damage, not zero damage.
                                0.625 -> listOfInteractions.add(.40)
                                1.0 -> listOfInteractions.add(0.625)
                                1.6 -> listOfInteractions.add(1.0)
                            }
                            0.390625 -> when (listOfInteractionsType2[i]) {
                                0.390625 -> listOfInteractions.add(.15258789062) // This permutation cannot exist
                                0.625 -> listOfInteractions.add(.244140625)
                                1.0 -> listOfInteractions.add(0.390625)
                                1.6 -> listOfInteractions.add(0.625)
                            }
                        }
                        // Places the coefficients in the table
                        arrayWithCellID[i].text = listOfInteractions[i].toString()
                        when (listOfInteractions[i]) {
                            1.0 -> makeCellEffective(i)
                            1.6 -> makeCellSuperEffective(i)
                            2.56 -> makeCellUltraSuperEffective(i)
                            0.625 -> makeCellNotVeryEffective(i)
                            0.40 -> makeCellUltraNotVeryEffective(i)
                            0.390625 -> makeCellDoesNotEffect(i)
                            0.244140625 -> makeCellDoesNotEffect(i)
                            0.15258789062 -> makeCellDoesNotEffect(i) // This permutation cannot exist
                        }
                    }
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }


        fetchJson()

        // End of onCreate
    }

    private fun setupSpinner(spinnerOptions: Array<String>, spinner: Spinner) {
        // Assigning the povSpinner options to an adapter value, which is then assigned to the povSpinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        spinner.adapter = adapter
    }

    fun fetchJson() {
        val url = "https://pogoapi.net/api/v1/type_effectiveness.json"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response?.body?.string()
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
    fun makeCellEffective(typeIndex: Int) {
        val textView = arrayWithCellID[typeIndex]
        textView.background = ContextCompat.getDrawable(this, R.color.x1color)
        textView.setTextColor(getColor(R.color.black))
        //textView.setTextColor(Color.parseColor("#000000")) //@@@ktg move colors to colors.xml
    }

    fun makeCellSuperEffective(typeIndex: Int) {
        val textView = arrayWithCellID[typeIndex]
        textView.text = "x 2" //@@@ktg move strings to strings.xml
        textView.background = ContextCompat.getDrawable(this, R.color.x2color)
        textView.setTextColor(Color.parseColor("#000000"))
    }

    fun makeCellUltraSuperEffective(typeIndex: Int) {
        val textView = arrayWithCellID[typeIndex]
        textView.text = "x 4"
        textView.background = ContextCompat.getDrawable(this, R.color.x4color)
        textView.setTextColor(Color.parseColor("#000000"))
    }

    fun makeCellNotVeryEffective(typeIndex: Int) {
        val textView = arrayWithCellID[typeIndex]
        textView.text = "x .5"
        textView.background = ContextCompat.getDrawable(this, R.color.x_5color)
        textView.setTextColor(Color.parseColor("#000000"))
    }

    fun makeCellUltraNotVeryEffective(typeIndex: Int) {
        val textView = arrayWithCellID[typeIndex]
        textView.text = "x .25"
        textView.background = ContextCompat.getDrawable(this, R.color.x_25color)
        textView.setTextColor(Color.parseColor("#000000"))
    }

    fun makeCellDoesNotEffect(typeIndex: Int) {
        val textView = arrayWithCellID[typeIndex]
        textView.text = "x 0"
        textView.background = ContextCompat.getDrawable(this, R.color.x0color)
        textView.setTextColor(Color.parseColor("#FFFFFF"))
    }

    // @@@ktg yah nah
    // I would shoot you if you submitted this to PR
    // instantly
    fun checkIfTypingExists (type1: Int, type2: Int): Boolean {
        return (   (type1 == 13 && type2 == 12)
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
    fun attackingEffectivenessCalculator(attacker: Int): MutableList<Double> {
        if (attacker == 0) {
            // @@@ktg there's an easier way to instantiate a list of the same value
                // hint: loops/in-line functions
            return mutableListOf(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0
            )
        }
        val attackerType: String = spinnerTypeOptions1[attacker]
        var dictOfSelectedTypes = typeMatchups.Bug
        // @@@ktg do some research into Enums - the whole project can be refactored a fair bit to use a Type enum
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
        val listOfAttackingMatchupCoefficients: MutableList<Double> =
            dictOfSelectedTypes.values.toMutableList()
        return listOfAttackingMatchupCoefficients
    }

    // Returns a mutable list for how one type defends against all other types
    fun defendingEffectivenessCalculator(defender: Int): MutableList<Double> {
        if (defender == 0) {
            return mutableListOf(
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0,
                1.0
            )
        }
        var listOfDefendingMatchupCoefficients: MutableList<Double> = arrayListOf()
        val defendingType: String = spinnerTypeOptions1[defender]

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
}

