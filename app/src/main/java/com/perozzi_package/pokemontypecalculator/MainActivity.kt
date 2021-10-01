package com.perozzi_package.pokemontypecalculator

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.databinding.ActivityMainBinding

@SuppressLint("UseSwitchCompatOrMaterialCode")
@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {
    private var mainActivityViewModel = MainActivityViewModel(resources)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide() // Hides top bar

        // Bindings
        val binding = ActivityMainBinding.inflate(layoutInflater); setContentView(binding.root)
        val gameSwitchTextBinding = binding.gameSwitchText
        val attackingTypeSpinner = binding.attackingTypeSpinner
        val defendingType1Spinner = binding.type1Spinner
        val defendingType2Spinner = binding.type2Spinner
        val doesNotExistDisclaimer = binding.doesNotExistDisclaimer
        val povSwitch = binding.povSwitch
        val gameSwitch = binding.gameSwitch
        val iceJiceSwitch = binding.iceJiceSwitch
        val infoButton = binding.infoButton

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

        mainActivityViewModel.fetchJson() //gets .json file
        // mainActivityViewModel.fetchAllPokemonNamesAndURLs()
        // mainActivityViewModel.fetchAllTypingPossibilities()

        // RecyclerView
        val listOfCellBackgroundColors: MutableList<Int> = mainActivityViewModel.onesInt()
        val listOfCellTextColors: MutableList<Int> = mainActivityViewModel.onesInt()
        var recyclerView: RecyclerView? = findViewById(R.id.typeTableRecyclerView)
        mainActivityViewModel.gridLayoutManager =
            GridLayoutManager(applicationContext, 3, LinearLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = mainActivityViewModel.gridLayoutManager
        recyclerView?.setHasFixedSize(true)
        mainActivityViewModel.arrayListForTypeGrid = ArrayList()
        mainActivityViewModel.arrayListForTypeGrid = mainActivityViewModel.setDataInTypeGridList(
            mainActivityViewModel.arrayOfTypeIcons,
            mainActivityViewModel.onesString(),
            listOfCellBackgroundColors,
            listOfCellTextColors
        )
        mainActivityViewModel.typeGridAdapter =
            TypeGridAdapter(mainActivityViewModel.arrayListForTypeGrid!!)
        recyclerView?.adapter = mainActivityViewModel.typeGridAdapter

        // POV SWITCH
        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.weAreDefending.value = onSwitch

            // QoL: transfers the value from the to-be-invisible spinner to the to-be-visible one
            if (mainActivityViewModel.weAreDefending.value!!) {
                mainActivityViewModel.defendType2.value = "[none]"
                defendingType2Spinner.setSelection(0)
                mainActivityViewModel.defendType1 = mainActivityViewModel.attackType
                defendingType1Spinner.setSelection(mainActivityViewModel.atkIndex.value!!)
            } else {
                mainActivityViewModel.attackType = mainActivityViewModel.defendType1
                attackingTypeSpinner.setSelection(mainActivityViewModel.def1Index.value!!)
                if ((mainActivityViewModel.defendType1.value!! == "(choose)" || mainActivityViewModel.defendType1.value!! == Types.NoType.type) &&
                    (mainActivityViewModel.defendType2.value!! != "[none]" && mainActivityViewModel.defendType2.value!! != Types.NoType.type)
                ) {
                    mainActivityViewModel.attackType = mainActivityViewModel.defendType2
                    attackingTypeSpinner.setSelection(mainActivityViewModel.def2Index.value!!)
                }
                doesNotExistDisclaimer.visibility = View.INVISIBLE
            }
            mainActivityViewModel.refreshTheData()
            recyclerView?.adapter = mainActivityViewModel.typeGridAdapter
        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                mainActivityViewModel.atkIndex.value = p2

                mainActivityViewModel.refreshTheData()
                recyclerView?.adapter = mainActivityViewModel.typeGridAdapter
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                mainActivityViewModel.def1Index.value = p2

                mainActivityViewModel.refreshTheData()
                recyclerView?.adapter = mainActivityViewModel.typeGridAdapter
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View, p2: Int, p3: Long) {
                mainActivityViewModel.def2Index.value = p2
                mainActivityViewModel.refreshTheData()
                recyclerView?.adapter = mainActivityViewModel.typeGridAdapter
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // This text is attached to gameSwitch; clicking on text will also toggle gameSwitch
        gameSwitchTextBinding.setOnClickListener { gameSwitch.toggle() }

        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.pogoTime.value = onSwitch

            mainActivityViewModel.refreshTheData()
            recyclerView?.adapter = mainActivityViewModel.typeGridAdapter
        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            mainActivityViewModel.jiceTime.value = onSwitch

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

    private fun setupSpinner(spinnerOptions: Array<String>, spinner: Spinner) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerOptions)
        spinner.setSelection(0, false)
    }

    /*private fun justChangeJiceInGridView() {
        arrayListForTypeGrid?.get(11)?.iconsInGridView = if (mainActivityViewModel.jiceTime) { R.drawable.jice_icon } else { R.drawable.ice_icon }
        arrayListForTypeGrid?.let { TypeGridAdapter(it).submitList(arrayListForTypeGrid) }
    }*/
}