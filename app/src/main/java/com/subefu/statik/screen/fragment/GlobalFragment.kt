package com.subefu.statik.screen.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.subefu.statik.databinding.FragmentGlobalBinding
import com.subefu.statik.model.Habit
import com.subefu.statik.adapter.HabitAdapter
import java.text.SimpleDateFormat
import java.util.Calendar

class GlobalFragment : Fragment() {

    lateinit var binding: FragmentGlobalBinding
    val listHabitCard = ArrayList<Habit>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGlobalBinding.inflate(inflater)

        init()


        return binding.root
    }

    fun init(){
        setCurrentDate()
        loadCard()
    }

    fun loadCard(){

        listHabitCard.add(Habit("water", 10))
        listHabitCard.add(Habit("steps", 10000))
        listHabitCard.add(Habit("mood", 8))
        listHabitCard.add(Habit("words", 15))
        listHabitCard.add(Habit("weather", 10))

        val rvHabitCard = binding.rvHabitCard
        rvHabitCard.adapter = HabitAdapter(requireContext(), listHabitCard)
        rvHabitCard.layoutManager = GridLayoutManager(requireContext(),3, RecyclerView.VERTICAL,false)
    }

    @SuppressLint("SimpleDateFormat")
    fun setCurrentDate(){
        val formater = SimpleDateFormat("dd.MM.yy")
        binding.currentDate.text = formater.format(Calendar.getInstance().time)
    }
}