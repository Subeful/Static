package com.subefu.statik.screen.fragment

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.subefu.statik.R
import com.subefu.statik.databinding.FragmentStatisticBinding
import com.subefu.statik.db.Dao
import com.subefu.statik.db.ModelHabitWater
import com.subefu.statik.db.MyDatabase
import com.subefu.statik.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class StatisticFragment : Fragment() {

    lateinit var binding: FragmentStatisticBinding
    lateinit var dao: Dao
    lateinit var config: SharedPreferences

    val animateDuration = 1500L

    val listDate = HashMap<String, Long>()
    var date = 0L
    var currentSelectHabit = ""
    var currentSelectPeriod = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStatisticBinding.inflate(inflater)

        init()

        binding.chipGroupHabit.setOnCheckedChangeListener() { group, checkedId ->
            if (checkedId == View.NO_ID){
                setModeMain();
                currentSelectHabit = ""
                return@setOnCheckedChangeListener
            }

            toggleChildLayout(binding.choosePeriod)
            setModeSelection()

            currentSelectHabit = when(checkedId){
                R.id.chipHabit_water -> "water"
                R.id.chipHabit_steps -> "steps"
                R.id.chipHabit_words -> "words"
                R.id.chipHabit_weather -> "weather"
                R.id.chipHabit_mood -> "mood"
                R.id.chipHabit_productive -> "productive"
                R.id.chipHabit_sport -> "sport"
                R.id.chipHabit_sleep -> "sleep"
                R.id.chipHabit_screenTime -> "screenTime"
                R.id.chipHabit_cost -> "cost"
                R.id.chipHabit_comment -> "comment"
                else -> "error chip"
            }

            selectingHabit(currentSelectHabit, if(currentSelectPeriod == "") "week" else currentSelectPeriod)
        }
        binding.chipGroupTime.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == View.NO_ID){
                currentSelectPeriod = ""
                selectingHabit(currentSelectHabit, "week")
                return@setOnCheckedChangeListener
            }

            currentSelectPeriod = when(checkedId){
                R.id.chipTime_today -> "today"
                R.id.chipTime_yesterday -> "yesterday"
                R.id.chipTime_week -> "week"
                R.id.chipTime_month -> "month"
                R.id.chipTime_year -> "year"
                else -> "error time"
            }

            selectingHabit(currentSelectHabit, currentSelectPeriod)
        }

        binding.statFilter.setOnClickListener {
            it.visibility = View.GONE
            toggleChildLayout(binding.chooseHabit)
        }
        binding.statisticIconPeriud.setOnClickListener{
            binding.chipGroupTime.clearCheck()
        }
        binding.statisticIconCategory.setOnClickListener{
            binding.chipGroupHabit.clearCheck()
        }

        return binding.root
    }

    fun init(){
        dao = MyDatabase.getDb(requireContext()).getDao()
        config = requireContext().getSharedPreferences(Constant.CONFIG, 0)

        setListDate()

        setMainStatistic()

        loadMainChartLine()
        loadMainChartDonat()

        //mandatory initialisation. don't pay attention
        setMainChartLine(ArrayList<Pair<String, Float>>())
        setMainChartDonat(ArrayList<Float>())
        setSelectedChartOvercomeTarget(ArrayList<Float>())
        setSelectedChartStatistic(ArrayList<Pair<String, Float>>())
    }

    fun setMainStatistic(){
        //заполнить верхнюю панель, 3 числа
        /*binding.mainStatDayInApp.setText("")
        binding.mainStatCurrentSeries.setText("")
        binding.mainStatRecordSeries.setText("")*/
    }
    fun selectingHabit(habit: String, period: String){
        //TODO this get data of selection chart from database and load it in list
        /*Log.d("Select habit", "currentHabit: $currentSelectHabit, currentPeriod: $currentSelectPeriod")
        Log.d("Select habit", "habit: $habit, period: $period")*/

        lifecycleScope.launch(Dispatchers.IO) {
            when(habit){
                "water" -> installWaterSetting(period)
                else -> {}
            }
        }

    }

    fun loadMainChartLine(){
        lifecycleScope.launch(Dispatchers.IO) {
            val lineList = ArrayList<Pair<String, Float>>()
            val list = dao.selectPeriodForWater(listDate.get("month")!!.toLong(), (listDate.get("today")?.plus(1))!!.toLong())
            list.forEach {
                val date = getTime(it.day_date)
                lineList.add(Pair(date, it.day_result.toFloat()))
                Log.d("line chart", "${it.day_date}, ${it.day_result}")
            }
            //choose list
            if(lineList.size == 1){
                val newList = ArrayList<Pair<String, Float>>()
                newList.add(Pair((date-86400001).toString(), 0f))
                newList.add(lineList[0])

                lineList.clear()
                lineList.addAll(newList)
            }
            else if(lineList.isEmpty()){
                lineList.addAll(filNothingList())
            }

            launch(Dispatchers.Main) {
                setMainChartLine(lineList)
            }
        }
    }
    fun loadMainChartDonat(){
        lifecycleScope.launch(Dispatchers.IO) {
            val dateFirstEntrance = config.getLong(Constant.FIRST_ENTRANCE, 0)
            val dateLastEntrance = config.getLong(Constant.LAST_ENTRANCE, 0)
            val activeDay = config.getInt(Constant.ACTIVE_DAY, 0)
            if(dateLastEntrance == 0L || dateFirstEntrance == 0L)
                return@launch

            val period = (dateLastEntrance - dateFirstEntrance) / 86_400_000

            Log.d("Donat date", dateFirstEntrance.toString())
            Log.d("Donat date", dateLastEntrance.toString())
            Log.d("Donat date", activeDay.toString())
            Log.d("Donat date", period.toString())

            launch(Dispatchers.Main) {
                val listData = ArrayList<Float>()
                listData.add(activeDay.toFloat())
                listData.add((period - activeDay).toFloat())
                Log.d("Donat list", listData[0].toString())
                Log.d("Donat list", listData[1].toString())
                setMainChartDonat(listData)

                binding.mainDonatTextActive.append(" ($activeDay)")
                binding.mainDonatTextPassive.append(" (${(period - activeDay)})")
            }
        }
    }

    suspend fun installWaterSetting(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            val listModelWater = ArrayList<ModelHabitWater>()

            //set date for selected period
            if(period == "today")
                listModelWater.add(dao.selectCurrentDataForWater(listDate.get("today") as Long))
            else if(period == "yesterday")
                listModelWater.add(dao.selectCurrentDataForWater((listDate.get("today") as Long) - 86400000))
            else if(period == "week")
                listModelWater.addAll(dao.selectPeriodForWater(listDate.get("week") as Long, listDate.get("today")!!.plus(1)))
            else if(period == "month")
                listModelWater.addAll(dao.selectPeriodForWater(listDate.get("month") as Long, listDate.get("today")!!.plus(1)))
            else if(period == "year")
                listModelWater.addAll(dao.selectPeriodForWater(listDate.get("year") as Long, listDate.get("today")!!.plus(1)))
            else
                listModelWater.addAll(dao.selectPeriodForWater(listDate.get("week") as Long, listDate.get("today")!!.plus(1)))

            val listData = ArrayList<Pair<String, Float>>()

            listModelWater.forEachIndexed { index, it ->
                Log.d("List water", "$index. ${it.day_date} - ${it.day_result}")
                val date = if(it.day_date == 0L) "" else getTime(it.day_date)
                listData.add(Pair(date, it.day_result.toFloat()))
            }
            // when size == 1< chart don`t draw column
            if(listModelWater.size == 1){
                listData.add(Pair("", 0f))
                listData.add(Pair(" ", 0f))
            }

            launch(Dispatchers.Main) {
                setSelectedChartStatistic(listData)
            }
        }
    }


    fun setMainChartLine(list: ArrayList<Pair<String, Float>>) {
        val lineChart = binding.mainStorageFrequency
        lineChart.apply {
            lineChart.tooltip.onCreateTooltip(lineChart)
            lineChart.animation.duration = animateDuration
            lineChart.gradientFillColors = intArrayOf(Color.parseColor("#F2C788"), Color.TRANSPARENT)
            lineChart.animate(list)
            lineChart.onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    ( if(list.isEmpty()) filNothingList() else list )
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun setMainChartDonat(list: List<Float>) {
        val donatChart = binding.mainDayOnApp
        donatChart.apply {
            donatChart.animation.duration = animateDuration
            donatChart.donutTotal = decideTotalForChart(list)
            donatChart.animate(list)
            donatChart.donutColors =
                intArrayOf(resources.getColor(R.color.green), resources.getColor(R.color.pink))
        }
    }

    fun setSelectedChartOvercomeTarget(list: List<Float>) {
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
            selectedStatistic.onDataPointClickListener = {index, _, _ ->
                Toast.makeText(requireContext(), list.toList().get(index).second.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }


    //support method
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

    fun setListDate(){
        var calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 12)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        listDate.put("today", calendar.timeInMillis)
        date = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_WEEK, -1)
        listDate.put("yesterday", calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_WEEK, -6)
        listDate.put("week", calendar.timeInMillis-1)
        //new calendar
        calendar = Calendar.getInstance()
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MONTH, -1)
        listDate.put("month", calendar.timeInMillis-1)
        calendar.add(Calendar.MONTH, -11)
        listDate.put("year", calendar.timeInMillis-1)

        listDate.forEach { period, date ->
            Log.d("Date", "$period - $date")
        }
    }

    fun filNothingList():List<Pair<String, Float>> {
        val nothingList = ArrayList<Pair<String, Float>>()
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_WEEK, -7)
        for(i in 0..7){
            val date = getTime(calendar.timeInMillis).toString()
            nothingList.add(Pair(date, 0f))
            calendar.add(Calendar.DAY_OF_WEEK, 1)
        }

        return nothingList
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(time: Long) = SimpleDateFormat("dd.MM").format(Date(time))
    
    fun setModeMain(){
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
        list.forEach {
            total += it }
        return total
    }
}