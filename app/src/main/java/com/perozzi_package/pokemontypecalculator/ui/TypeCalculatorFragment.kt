package com.perozzi_package.pokemontypecalculator.ui

import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.R
import com.perozzi_package.pokemontypecalculator.adapters.TypeGridAdapter
import com.perozzi_package.pokemontypecalculator.Types
import com.perozzi_package.pokemontypecalculator.databinding.FragmentTypeCalculatorBinding

class TypeCalculatorFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentTypeCalculatorBinding
    private lateinit var tcViewModel: TypeCalculatorViewModel

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
        tcViewModel = TypeCalculatorViewModel(resources, requireActivity().application)
        binding.typeCalculatorViewModel = tcViewModel
        val povSwitch = binding.povSwitch

        tcViewModel.fetchJson()

        val recyclerView: RecyclerView = binding.typeTableRecyclerView
        recyclerView.layoutManager = tcViewModel.gridLayoutManager
        recyclerView.setHasFixedSize(true)
        val listAdapter = TypeGridAdapter()
        recyclerView.adapter = listAdapter
        tcViewModel.arrayForTypeGrid.observe(viewLifecycleOwner, { data ->
            listAdapter.submitList(data)
        })

        tcViewModel.typeCombinationExists.observe(viewLifecycleOwner, {
            if (!it) {
                Toast.makeText(
                    requireContext(),
                    R.string.does_not_exist_disclaimer,
                    Toast.LENGTH_LONG
                ).show()
            }
        })

        val attackingTypeSpinner = binding.attackingTypeSpinner
        setupSpinner(tcViewModel.attackingSpinnerOptions,attackingTypeSpinner)
        changeLiveDataOnSpinnerSelectionChange(attackingTypeSpinner,tcViewModel.atkIndex)
        val defendingType1Spinner = binding.type1Spinner
        setupSpinner(tcViewModel.defendingSpinner1Options,defendingType1Spinner)
        changeLiveDataOnSpinnerSelectionChange(defendingType1Spinner,tcViewModel.def1Index)
        val defendingType2Spinner = binding.type2Spinner
        setupSpinner(tcViewModel.defendingSpinner2Options,defendingType2Spinner)
        changeLiveDataOnSpinnerSelectionChange(defendingType2Spinner,tcViewModel.def2Index)

        povSwitch.setOnCheckedChangeListener { _, onSwitch ->
            tcViewModel.weAreDefending.value = onSwitch
            calibrateSpinnerValuesAfterPOVSwitch(
                attackingTypeSpinner,
                defendingType1Spinner,
                defendingType2Spinner
            )
        }

        binding.gameSwitch.setOnCheckedChangeListener { _, onSwitch ->
            tcViewModel.pogoTime.value = onSwitch
        }
        // This textView is adjacent to gameSwitch; clicking on text will also toggle gameSwitch
        binding.gameSwitchText.setOnClickListener { binding.gameSwitch.toggle() }

        binding.iceJiceSwitch.setOnCheckedChangeListener { _, onSwitch ->
            tcViewModel.jiceTime.value = onSwitch
        }

        binding.infoButton.setOnClickListener(this)
    }

    private fun calibrateSpinnerValuesAfterPOVSwitch(
        attackingTypeSpinner: Spinner,
        defendingType1Spinner: Spinner, defendingType2Spinner: Spinner
    ) {
        if (tcViewModel.weAreDefending.value!!) {

            tcViewModel.defendType2.value = "[none]"
            tcViewModel.def2Index.value = 0
            defendingType2Spinner.setSelection(0)

            tcViewModel.defendType1.value = tcViewModel.attackType.value
            tcViewModel.def1Index.value = tcViewModel.atkIndex.value
            defendingType1Spinner.setSelection(tcViewModel.atkIndex.value!!)

            tcViewModel.attackType.value = "(choose)"
            tcViewModel.atkIndex.value = 0
            attackingTypeSpinner.setSelection(0)

        } else {

            tcViewModel.attackType.value = tcViewModel.defendType1.value
            tcViewModel.atkIndex.value = tcViewModel.def1Index.value
            attackingTypeSpinner.setSelection(tcViewModel.def1Index.value!!)

            if ((tcViewModel.defendType1.value!! == "(choose)" || tcViewModel.defendType1.value!! == Types.NoType.type) &&
                (tcViewModel.defendType2.value!! != "[none]" && tcViewModel.defendType2.value!! != Types.NoType.type)
            ) {
                tcViewModel.attackType.value = tcViewModel.defendType2.value
                tcViewModel.atkIndex.value = tcViewModel.def2Index.value
                attackingTypeSpinner.setSelection(tcViewModel.def2Index.value!!)
            }

            tcViewModel.defendType2.value = "[none]"
            tcViewModel.def2Index.value = 0
            defendingType2Spinner.setSelection(0)

            tcViewModel.defendType1.value = "[none]"
            tcViewModel.def1Index.value = 0
            defendingType1Spinner.setSelection(0)
        }
    }

    override fun onClick(v: View?) {
        // This when statement is redundant with two fragments, but will be helpful if more
        // fragments being added
        when (v?.id) {
            R.id.infoButton -> Navigation.findNavController(v)
                .navigate(R.id.action_typeCalculatorFragment_to_funFactFragment)
        }
    }

    private fun setupSpinner(spinnerOptions: LiveData<Array<String>>, spinner: Spinner) {
        spinnerOptions.value?.let {
            spinner.adapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
        }
    }

    private fun changeLiveDataOnSpinnerSelectionChange(spinner: Spinner, liveData: MutableLiveData<Int>) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                liveData.value = p2
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
}