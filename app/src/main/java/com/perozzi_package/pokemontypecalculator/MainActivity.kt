package com.perozzi_package.pokemontypecalculator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivityViewModel = MainActivityViewModel(resources,application)
        supportActionBar?.hide() // Hides top bar

        // Bindings
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.mainActivityViewModel = mainActivityViewModel

        setContentView(binding.root)

        val povSwitch = binding.povSwitch
        val gameSwitch = binding.gameSwitch
        val clickableGameSwitchText = binding.gameSwitchText
        val iceJiceSwitch = binding.iceJiceSwitch
        val infoButton = binding.infoButton

        val attackingTypeSpinner = binding.attackingTypeSpinner
        attackingTypeSpinner.setSelection(0)
        mainActivityViewModel.atkIndex.value = 0

        val defendingType1Spinner = binding.type1Spinner
        defendingType1Spinner.setSelection(0)
        mainActivityViewModel.def1Index.value = 0

        val defendingType2Spinner = binding.type2Spinner
        defendingType2Spinner.setSelection(0)
        mainActivityViewModel.def2Index.value = 0

        // Populates spinner options
        mainActivityViewModel.attackingSpinnerOptions.value?.let {
            setupSpinner(it, attackingTypeSpinner)
        }
        mainActivityViewModel.defendingSpinner1Options.value?.let {
            setupSpinner(it, defendingType1Spinner)
        }
        mainActivityViewModel.defendingSpinner2Options.value?.let {
            setupSpinner(it, defendingType2Spinner)
        }

        mainActivityViewModel.fetchJson()

        // Set initial spinner values
        mainActivityViewModel.attackType.value = "(choose)"
        mainActivityViewModel.defendType1.value = "(choose)"
        mainActivityViewModel.defendType2.value = "[none]"

        // RecyclerView
        val recyclerView: RecyclerView = binding.typeTableRecyclerView
        recyclerView.layoutManager = mainActivityViewModel.gridLayoutManager
        recyclerView.setHasFixedSize(true)

        val listAdapter = TypeGridAdapter()
        recyclerView.adapter = listAdapter

        mainActivityViewModel.arrayForTypeGrid.observe(this, { data ->
            listAdapter.submitList(data)
        })

        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.weAreDefending.value = onSwitch

            // Minor quality-of-life implementation: Transfers the value from the to-be-invisible
            // spinner to the to-be-visible one
            if (mainActivityViewModel.weAreDefending.value!!) {

                mainActivityViewModel.defendType2.value = "[none]"
                mainActivityViewModel.def2Index.value = 0
                defendingType2Spinner.setSelection(0)

                mainActivityViewModel.defendType1.value = mainActivityViewModel.attackType.value
                mainActivityViewModel.def1Index.value = mainActivityViewModel.atkIndex.value
                defendingType1Spinner.setSelection(mainActivityViewModel.atkIndex.value!!)

                mainActivityViewModel.attackType.value = "(choose)"
                mainActivityViewModel.atkIndex.value = 0
                attackingTypeSpinner.setSelection(0)

            } else {

                mainActivityViewModel.attackType.value = mainActivityViewModel.defendType1.value
                mainActivityViewModel.atkIndex.value = mainActivityViewModel.def1Index.value
                attackingTypeSpinner.setSelection(mainActivityViewModel.def1Index.value!!)

                if ((mainActivityViewModel.defendType1.value!! == "(choose)" || mainActivityViewModel.defendType1.value!! == Types.NoType.type) &&
                    (mainActivityViewModel.defendType2.value!! != "[none]" && mainActivityViewModel.defendType2.value!! != Types.NoType.type)
                ) {
                    mainActivityViewModel.attackType.value = mainActivityViewModel.defendType2.value
                    mainActivityViewModel.atkIndex.value = mainActivityViewModel.def2Index.value
                    attackingTypeSpinner.setSelection(mainActivityViewModel.def2Index.value!!)
                }

                mainActivityViewModel.defendType2.value = "[none]"
                mainActivityViewModel.def2Index.value = 0
                defendingType2Spinner.setSelection(0)

                mainActivityViewModel.defendType1.value = "[none]"
                mainActivityViewModel.def1Index.value = 0
                defendingType1Spinner.setSelection(0)
            }
        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mainActivityViewModel.atkIndex.value = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                mainActivityViewModel.def1Index.value = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                mainActivityViewModel.def2Index.value = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // This text is attached to gameSwitch; clicking on text will also toggle gameSwitch
        clickableGameSwitchText.setOnClickListener { gameSwitch.toggle() }
        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.pogoTime.value = onSwitch
        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.jiceTime.value = onSwitch
        }

        infoButton.setOnClickListener {
            val intent = Intent(this, TypeTriviaActivity::class.java)
            startActivity(intent)
        }

    } // End of onCreate

    private fun setupSpinner(spinnerOptions: Array<String>, spinner: Spinner) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        spinner.setSelection(0, false)
    }
}