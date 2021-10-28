package com.perozzi_package.pokemontypecalculator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.perozzi_package.pokemontypecalculator.databinding.FragmentFunFactBinding

class FunFactFragment : Fragment() {

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var funFactAdapter: RecyclerView.Adapter<FunFactAdapter.ItemHolder>? = null
    private var arrayListForFunFact:ArrayList<FunFact> ? = null
    private lateinit var typeCalculatorViewModel: TypeCalculatorViewModel
    private lateinit var navController: NavController


    // onCreate is for initial creation of fragment.
    // Do non graphical initializations here
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_type_trivia)

        val funFactTitles: Array<String> = resources.getStringArray(R.array.fun_fact_titles)
        val funFactContents: Array<String> = resources.getStringArray(R.array.fun_fact_contents)

        arrayListForFunFact = setDataInFunFactList(funFactTitles,funFactContents)
        funFactAdapter = FunFactAdapter(arrayListForFunFact!!)
    }

    // onCreateView is for inflating the layout; graphical initialization usually takes place here.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFunFactBinding.inflate(layoutInflater)
        val recyclerView = binding.recyclerView
        val backButton = binding.backButton
        backButton.setOnClickListener {
            navController.navigate(R.id.action_funFactFragment_to_typeCalculatorFragment)
        }

        typeCalculatorViewModel = TypeCalculatorViewModel(resources, requireActivity().application)
        binding.typeCalculatorViewModel = typeCalculatorViewModel
        layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = funFactAdapter

        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        /*val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.show()
        actionBar!!.title = resources.getString(R.string.trivia_action_bar_title)
        actionBar.setDisplayHomeAsUpEnabled(true)*/

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
    }

    private fun setDataInFunFactList(funFactTitles: Array<String>, funFactText: Array<String>):
            ArrayList<FunFact> {
        val items: ArrayList<FunFact> = ArrayList()
        for (i in funFactTitles.indices) {
            items.add(
                FunFact(
                    funFactTitles[i],
                    funFactText[i]
                )
            )
        }
        return items
    }

}