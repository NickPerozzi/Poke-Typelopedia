package com.perozzi_package.pokemontypecalculator

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.databinding.FragmentTypeCalculatorBinding
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation

class TypeCalculatorFragment : Fragment() , View.OnClickListener {

    private lateinit var navController: NavController
    private lateinit var binding: FragmentTypeCalculatorBinding
    private lateinit var typeCalculatorViewModel: TypeCalculatorViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTypeCalculatorBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        typeCalculatorViewModel = TypeCalculatorViewModel(resources, requireActivity().application)
        binding.typeCalculatorViewModel = typeCalculatorViewModel

        // Bindings
        val povSwitch = binding.povSwitch
        povSwitch.isChecked = false
        val gameSwitch = binding.gameSwitch
        val clickableGameSwitchText = binding.gameSwitchText
        val iceJiceSwitch = binding.iceJiceSwitch

        val attackingTypeSpinner = binding.attackingTypeSpinner
        attackingTypeSpinner.setSelection(0)
        typeCalculatorViewModel.atkIndex.value = 0

        val defendingType1Spinner = binding.type1Spinner
        defendingType1Spinner.setSelection(0)
        typeCalculatorViewModel.def1Index.value = 0

        val defendingType2Spinner = binding.type2Spinner
        defendingType2Spinner.setSelection(0)
        typeCalculatorViewModel.def2Index.value = 0

        // Populates spinner options
        typeCalculatorViewModel.attackingSpinnerOptions.value?.let {
            setupSpinner(it, attackingTypeSpinner)
        }
        typeCalculatorViewModel.defendingSpinner1Options.value?.let {
            setupSpinner(it, defendingType1Spinner)
        }
        typeCalculatorViewModel.defendingSpinner2Options().value?.let {
            setupSpinner(it, defendingType2Spinner)
        }

        typeCalculatorViewModel.fetchJson()

        // Set initial spinner values
        typeCalculatorViewModel.attackType.value = "(choose)"
        typeCalculatorViewModel.defendType1.value = "(choose)"
        typeCalculatorViewModel.defendType2.value = "[none]"

        // RecyclerView
        val recyclerView: RecyclerView = binding.typeTableRecyclerView
        recyclerView.layoutManager = typeCalculatorViewModel.gridLayoutManager
        recyclerView.setHasFixedSize(true)

        val listAdapter = TypeGridAdapter()
        recyclerView.adapter = listAdapter

        typeCalculatorViewModel.arrayForTypeGrid.observe(viewLifecycleOwner, { data ->
            listAdapter.submitList(data)
        })
        
        typeCalculatorViewModel.typeCombinationExists.observe(viewLifecycleOwner, {
            if (!it) {
                Toast.makeText(requireContext(), R.string.does_not_exist_disclaimer, Toast.LENGTH_LONG).show()}
        })

        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            typeCalculatorViewModel.weAreDefending.value = onSwitch

            // Minor quality-of-life implementation: Transfers the value from the to-be-invisible
            // spinner to the to-be-visible one
            if (typeCalculatorViewModel.weAreDefending.value!!) {

                typeCalculatorViewModel.defendType2.value = "[none]"
                typeCalculatorViewModel.def2Index.value = 0
                defendingType2Spinner.setSelection(0)

                typeCalculatorViewModel.defendType1.value = typeCalculatorViewModel.attackType.value
                typeCalculatorViewModel.def1Index.value = typeCalculatorViewModel.atkIndex.value
                defendingType1Spinner.setSelection(typeCalculatorViewModel.atkIndex.value!!)

                typeCalculatorViewModel.attackType.value = "(choose)"
                typeCalculatorViewModel.atkIndex.value = 0
                attackingTypeSpinner.setSelection(0)

            } else {

                typeCalculatorViewModel.attackType.value = typeCalculatorViewModel.defendType1.value
                typeCalculatorViewModel.atkIndex.value = typeCalculatorViewModel.def1Index.value
                attackingTypeSpinner.setSelection(typeCalculatorViewModel.def1Index.value!!)

                if ((typeCalculatorViewModel.defendType1.value!! == "(choose)" || typeCalculatorViewModel.defendType1.value!! == Types.NoType.type) &&
                    (typeCalculatorViewModel.defendType2.value!! != "[none]" && typeCalculatorViewModel.defendType2.value!! != Types.NoType.type)
                ) {
                    typeCalculatorViewModel.attackType.value = typeCalculatorViewModel.defendType2.value
                    typeCalculatorViewModel.atkIndex.value = typeCalculatorViewModel.def2Index.value
                    attackingTypeSpinner.setSelection(typeCalculatorViewModel.def2Index.value!!)
                }

                typeCalculatorViewModel.defendType2.value = "[none]"
                typeCalculatorViewModel.def2Index.value = 0
                defendingType2Spinner.setSelection(0)

                typeCalculatorViewModel.defendType1.value = "[none]"
                typeCalculatorViewModel.def1Index.value = 0
                defendingType1Spinner.setSelection(0)
            }
        }

        attackingTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                typeCalculatorViewModel.atkIndex.value = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType1Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
                typeCalculatorViewModel.def1Index.value = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        defendingType2Spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
                typeCalculatorViewModel.def2Index.value = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // This text is attached to gameSwitch; clicking on text will also toggle gameSwitch
        clickableGameSwitchText.setOnClickListener { gameSwitch.toggle() }
        gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            typeCalculatorViewModel.pogoTime.value = onSwitch
        }

        iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            typeCalculatorViewModel.jiceTime.value = onSwitch
        }

        binding.infoButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        // This when statement is redundant with two fragments, but will be helpful upon more
        // fragments being added
        when (v!!.id){
            R.id.infoButton -> navController.navigate(R.id.action_typeCalculatorFragment_to_funFactFragment)
        }
    }

    private fun setupSpinner(spinnerOptions: Array<String>, spinner: Spinner) {
        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerOptions)
        spinner.setSelection(0, false)
    }


}