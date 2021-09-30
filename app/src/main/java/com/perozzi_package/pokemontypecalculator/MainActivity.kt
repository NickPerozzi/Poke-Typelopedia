package com.perozzi_package.pokemontypecalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.databinding.ActivityMainBinding

@SuppressLint("UseSwitchCompatOrMaterialCode")
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {

    // Worked for me
    private var mainActivityViewModel = MainActivityViewModel(Resources.getSystem())

    var recyclerView: RecyclerView? = null

    // needed for makeDataVisibleIfATypeIsSelected()
    private lateinit var typeTableRecyclerView: RecyclerView
    private lateinit var gameSwitchText: TextView
    private lateinit var gameSwitch: Switch
    private lateinit var iceJiceSwitch: Switch

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
        mainActivityViewModel.gridLayoutManager = GridLayoutManager(applicationContext, 3, LinearLayoutManager.VERTICAL,false)
        recyclerView?.layoutManager = mainActivityViewModel.gridLayoutManager
        recyclerView?.setHasFixedSize(true)
        mainActivityViewModel.arrayListForTypeGrid = ArrayList()
        mainActivityViewModel.arrayListForTypeGrid = mainActivityViewModel.setDataInTypeGridList(mainActivityViewModel.arrayOfTypeIcons,mainActivityViewModel.onesString(),listOfCellBackgroundColors,listOfCellTextColors)
        mainActivityViewModel.typeGridAdapter = TypeGridAdapter(mainActivityViewModel.arrayListForTypeGrid!!)
        recyclerView?.adapter = mainActivityViewModel.typeGridAdapter

        // BL

        // Necessary to adjust spinner values when switching between Attacker and Defender
        var atkSpinnerIndex = 0
        var defSpinner1Index = 0
        var defSpinner2Index = 0

        // LiveData implemented for the tableHeader
        mainActivityViewModel.promptText.observe(this, { typeSelectionPrompt.text = it })

        // POV SWITCH
        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.weAreDefending.value = onSwitch
            // Swaps the respective spinners in and out
            attackingTypeSpinnerAndLabel.visibility = if (mainActivityViewModel.weAreDefending.value!!) { View.GONE } else { View.VISIBLE }
            defendingType1SpinnerAndLabel.visibility = if (mainActivityViewModel.weAreDefending.value!!) { View.VISIBLE } else { View.GONE }
            defendingType2SpinnerAndLabel.visibility = if (mainActivityViewModel.weAreDefending.value!!) { View.VISIBLE } else { View.GONE }

            povSwitch.text = if (mainActivityViewModel.weAreDefending.value!!) { getString(R.string.pov_switch_to_defending) } else { getString(R.string.pov_switch_to_attacking) }
            // old
            // typeSelectionPrompt.text = if (weAreDefending) {resources.getString(R.string.defending_prompt) } else { resources.getString(R.string.attacking_prompt) }
            // new
            mainActivityViewModel.promptText.value = if (mainActivityViewModel.weAreDefending.value!!) {resources.getString(R.string.defending_prompt) } else { resources.getString(R.string.attacking_prompt) }


            // QoL: transfers the value from the to-be-invisible spinner to the to-be-visible one
            if (mainActivityViewModel.weAreDefending.value!!) {
                mainActivityViewModel.defendingType2.value = "[none]"
                defendingType2Spinner.setSelection(0)
                mainActivityViewModel.defendingType1 = mainActivityViewModel.attackingType
                defendingType1Spinner.setSelection(atkSpinnerIndex)
            } else {
                mainActivityViewModel.attackingType = mainActivityViewModel.defendingType1
                attackingTypeSpinner.setSelection(defSpinner1Index)
                if ((mainActivityViewModel.defendingType1.value!! == "(choose)" || mainActivityViewModel.defendingType1.value!! == Types.NoType.type) &&
                    (mainActivityViewModel.defendingType2.value!! != "[none]" && mainActivityViewModel.defendingType2.value!! != Types.NoType.type)) {
                    mainActivityViewModel.attackingType = mainActivityViewModel.defendingType2
                    attackingTypeSpinner.setSelection(defSpinner2Index)
                }
                doesNotExistDisclaimer.visibility = View.INVISIBLE
            }
            mainActivityViewModel.refreshTheData()
            recyclerView?.adapter = mainActivityViewModel.typeGridAdapter

        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                atkSpinnerIndex = p2
                mainActivityViewModel.attackingType.value = Types.values()[atkSpinnerIndex].type
                makeGridAndSwitchesVisibleIfATypeIsSelected()
                mainActivityViewModel.refreshTheData()
                recyclerView?.adapter = mainActivityViewModel.typeGridAdapter

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                defSpinner1Index = p2
                mainActivityViewModel.defendingType1.value = Types.values()[defSpinner1Index].type
                makeGridAndSwitchesVisibleIfATypeIsSelected()

                // TODO(Work on doesNotExistDisclaimer rework after liveData is worked out)
                /*doesNotExistDisclaimer.visibility = if (mutableListOf(
                        defendingType1,
                        defendingType2
                    ) !in mainActivityViewModel.listOfPossibleTypes
                ) { View.VISIBLE } else { View.INVISIBLE }*/
                mainActivityViewModel.refreshTheData()
                recyclerView?.adapter = mainActivityViewModel.typeGridAdapter
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                defSpinner2Index = p2
                mainActivityViewModel.defendingType2.value = Types.values()[defSpinner2Index].type
                makeGridAndSwitchesVisibleIfATypeIsSelected()
                mainActivityViewModel.refreshTheData()
                recyclerView?.adapter = mainActivityViewModel.typeGridAdapter

            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        gameSwitchText.setOnClickListener { gameSwitch.toggle() } // toggles gameSwitch when text is clicked
        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.pogoTime.value = onSwitch
            gameSwitchText.text = if (mainActivityViewModel.pogoTime.value!!) {
                resources.getString(R.string.pogo) } else { resources.getString(R.string.mainGame)}
            mainActivityViewModel.refreshTheData()
            recyclerView?.adapter = mainActivityViewModel.typeGridAdapter

        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.jiceTime.value = onSwitch

            // Adjusts the text in the spinners
            attackingSpinnerTypeOptions[12] = if (onSwitch) { getString(R.string.jice ) } else { getString(R.string.ice) }
            defendingSpinnerType1Options[12] = if (onSwitch) { getString(R.string.jice ) } else { getString(R.string.ice) }
            defendingSpinnerType2Options[12] = if (onSwitch) { getString(R.string.jice ) } else { getString(R.string.ice) }

            mainActivityViewModel.refreshTheData()
            recyclerView?.adapter = mainActivityViewModel.typeGridAdapter
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
        val type1: MutableLiveData<String> = if (mainActivityViewModel.weAreDefending.value!!) { mainActivityViewModel.defendingType1 } else { mainActivityViewModel.attackingType }
        val type2: MutableLiveData<String> = if (mainActivityViewModel.weAreDefending.value!!) { mainActivityViewModel.defendingType2 } else { MutableLiveData("[none]") }
                if ((type1.value == "(choose)" || type1.value == Types.NoType.type) &&
                    (type2.value == "[none]" || type2.value == Types.NoType.type)) {
                    typeTableRecyclerView.visibility = View.INVISIBLE
                    gameSwitchText.visibility = View.INVISIBLE
                    gameSwitch.visibility = View.INVISIBLE
                    iceJiceSwitch.visibility = View.INVISIBLE
                } else {
                    typeTableRecyclerView.visibility = View.VISIBLE
                    gameSwitchText.visibility = View.VISIBLE
                    gameSwitch.visibility = View.VISIBLE
                    iceJiceSwitch.visibility = View.VISIBLE
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

    // BL, to be implemented later when LiveData is a thing
    /*private fun justChangeJiceInGridView() {
        arrayListForTypeGrid?.get(11)?.iconsInGridView = if (mainActivityViewModel.jiceTime) { R.drawable.jice_icon } else { R.drawable.ice_icon }
        arrayListForTypeGrid?.let { TypeGridAdapter(it).submitList(arrayListForTypeGrid) }
    }*/
}