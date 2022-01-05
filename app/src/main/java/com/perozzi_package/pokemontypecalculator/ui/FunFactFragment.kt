package com.perozzi_package.pokemontypecalculator.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.perozzi_package.pokemontypecalculator.databinding.FragmentFunFactBinding

class FunFactFragment : Fragment() {

    private lateinit var binding: FragmentFunFactBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFunFactBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ffViewModel = FunFactViewModel(resources, requireActivity().application)
        binding.funFactViewModel = ffViewModel
        binding.recyclerView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = ffViewModel.funFactAdapter

        binding.backButton.setOnClickListener {
            Navigation.findNavController(view).popBackStack()
        }
    }
}