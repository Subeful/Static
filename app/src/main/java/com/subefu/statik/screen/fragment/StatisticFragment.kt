package com.subefu.statik.screen.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.subefu.statik.R
import com.subefu.statik.databinding.FragmentStatisticBinding


class StatisticFragment : Fragment() {

    lateinit var binding: FragmentStatisticBinding

    val animateDuration = 1500L

    val mainLintSet = ArrayList<Pair<String, Float>>()
    val mainActiveSet = ArrayList<Float>()

    val selectedOverTargetSet = ArrayList<Float>()
    val selectedStatisticSet = ArrayList<Pair<String, Float>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatisticBinding.inflate(inflater)

        init()

        binding.statFilter.setOnClickListener {
            it.visibility = View.GONE
            toggleChildLayout(binding.chooseHabit)
        }

        binding.chipGroupHabit.setOnCheckedChangeListener() { group, checkedId ->
            if (checkedId == View.NO_ID){
                setModeGeneral()
                return@setOnCheckedChangeListener
            }
            toggleChildLayout(binding.choosePeriod)
            setModeSelection()

            when(checkedId){
                R.id.chipHabit_water -> selectingHabit("water")
                R.id.chipHabit_steps -> selectingHabit("steps")
                R.id.chipHabit_words -> selectingHabit("words")
                R.id.chipHabit_weather -> selectingHabit("weather")
                R.id.chipHabit_mood -> selectingHabit("mood")
                R.id.chipHabit_productive -> selectingHabit("productive")
                R.id.chipHabit_sport -> selectingHabit("sport")
                R.id.chipHabit_sleep -> selectingHabit("sleep")
                R.id.chipHabit_screenTime -> selectingHabit("screenTime")
                R.id.chipHabit_cost -> selectingHabit("cost")
                R.id.chipHabit_comment -> selectingHabit("comment")

                else -> selectingHabit("error chip")
            }
        }

        binding.chipGroupTime.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == View.NO_ID)
                return@setOnCheckedChangeListener

            when(checkedId){
                R.id.chipTime_today -> selectingHabit("today")
                R.id.chipTime_yesterday -> selectingHabit("yesterday")
                R.id.chipTime_week -> selectingHabit("week")
                R.id.chipTime_month -> selectingHabit("month")
                R.id.chipTime_year -> selectingHabit("year")

                else -> selectingHabit("error time")
            }
        }

        binding.statisticIconPeriud.setOnClickListener{
            binding.chipGroupTime.clearCheck()
        }
        binding.statisticIconCategory.setOnClickListener{
            binding.chipGroupHabit.clearCheck()
        }

        return binding.root
    }
    fun toggleChildLayout(childLayout: LinearLayout) {
        if (childLayout.visibility == View.GONE) {
            childLayout.visibility = View.VISIBLE
            animateHeightChange(binding.statFilterPanel, childLayout.measuredHeight)
        }
        else {
            childLayout.visibility = View.GONE
            animateHeightChange(binding.statFilterPanel, -childLayout.measuredHeight)
        }
    }
    fun animateHeightChange(view: View, change: Int) {
        var change = if( change != 0) 0 else 0
        val params = view.layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        view.layoutParams = params
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.show_filter_choose)
        animation.duration = 300
        view.startAnimation(animation)

        params.height += change
        view.layoutParams = params
        view.requestLayout()
    }


    fun init(){
        setMainStatistic()

        setMainInfo()
        setMainChartActive(mainLintSet)
        setMainChartDayInApp(mainActiveSet)
    }

    fun setMainStatistic(){
        //TODO this get data of main statistic from database and load it in list

        binding.mainStatDayInApp.setText("")
        binding.mainStatCurrentSeries.setText("")
        binding.mainStatRecordSeries.setText("")
    }
    fun setMainInfo(){
        //TODO this get data of main chart from database and load it in list

        mainLintSet.clear()
        mainActiveSet.clear()

        //mainLintSet.addAll()
        //mainActiveSet.addAll()
    }

    fun setSelectedInfo(habit: String){
        //TODO this get data of selection chart from database and load it in list

        selectedStatisticSet.clear()
        selectedOverTargetSet.clear()

        //selectedStatisticSet.addAll()
        //selectedOverTargetSet.addAll()
    }


    fun selectingHabit(habit: String){
         setSelectedInfo(habit)

        setSelectedChartedOverTarget(selectedOverTargetSet)
        setSelectedChartStatistic(selectedStatisticSet)
    }

    fun setMainChartActive(list: List<Pair<String, Float>>) {
        val lineChart = binding.mainStorageFrequency
        lineChart.apply {
            lineChart.tooltip.onCreateTooltip(lineChart)
            lineChart.animation.duration = animateDuration
            lineChart.animate(list)
            lineChart.gradientFillColors =
                intArrayOf(Color.parseColor("#F2C788"), Color.TRANSPARENT)

            lineChart.onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    list.toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun setMainChartDayInApp(list: List<Float>) {
        val donatChart = binding.mainDayOnApp
        donatChart.apply {
            donatChart.animation.duration = animateDuration
            donatChart.donutTotal = decideTotalForChart(list)
            donatChart.animate(list)
            donatChart.donutColors =
                intArrayOf(resources.getColor(R.color.green), resources.getColor(R.color.pink))
        }
    }

    fun setSelectedChartedOverTarget(list: List<Float>) {
        val selectedDonatChart = binding.selectedDayOffTarget
        selectedDonatChart.apply {
            selectedDonatChart.animation.duration = animateDuration
            selectedDonatChart.donutTotal = decideTotalForChart(list)
            selectedDonatChart.animate(list)
            selectedDonatChart.donutColors = intArrayOf(resources.getColor(R.color.green), resources.getColor(R.color.pink))
        }
    }
    fun setSelectedChartStatistic(list: List<Pair<String, Float>>) {
        val selectedStatistic = binding.selectedStatistic
        selectedStatistic.apply {
            selectedStatistic.animation.duration = animateDuration
            selectedStatistic.animate(list)
            selectedStatistic.animate()
            selectedStatistic.onDataPointClickListener = {index, _, _ ->
                Toast.makeText(requireContext(), list.toList().get(index).second.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun setModeGeneral(){
        selectionInfoVisibility(false)
        mainInfoVisibility(true)
        choosePeriodVisibility(false)
    }
    fun setModeSelection(){
        selectionInfoVisibility(true)
        mainInfoVisibility(false)
        choosePeriodVisibility(true)
    }


    fun selectionInfoVisibility(isVisibility: Boolean){ binding.selectedInfo.visibility = if(isVisibility) View.VISIBLE else View.GONE }
    fun mainInfoVisibility(isVisibility: Boolean){ binding.mainInfo.visibility = if(isVisibility) View.VISIBLE else View.GONE }
    fun choosePeriodVisibility(isVisibility: Boolean){ binding.choosePeriod.visibility = if(isVisibility) View.VISIBLE else View.GONE }
    fun chooseHabitVisibility(isVisibility: Boolean){ binding.chooseHabit.visibility = if(isVisibility) View.VISIBLE else View.GONE }

    fun decideTotalForChart(list: List<Float>): Float{
        var total = 0f
        list.forEach { total += it }
        return total
    }
}

