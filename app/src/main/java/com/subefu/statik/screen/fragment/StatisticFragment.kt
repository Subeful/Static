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
import com.subefu.statik.db.ModelDays
import com.subefu.statik.db.MyDatabase
import com.subefu.statik.screen.MainActivity
import com.subefu.statik.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
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

            delAllSelectChart()
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
                R.id.chipTime_week -> "week"
                R.id.chipTime_month -> "month"
                R.id.chipTime_year -> "year"
                else -> "week"
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
        setSelectedChartStatisticWeek(ArrayList<Pair<String, Float>>())
        setSelectedChartStatisticMonth(ArrayList<Pair<String, Float>>())
        setSelectedChartStatisticYear(ArrayList<Pair<String, Float>>())
    }

    fun setMainStatistic(){
        lifecycleScope.launch(Dispatchers.IO) {
            val period = ((config.getLong(Constant.LAST_ENTRANCE, 0) - config.getLong(Constant.FIRST_ENTRANCE, 0))
                    / 86_400_000) + 1
            launch(Dispatchers.Main) { binding.mainStatDayInApp.setText(period.toString()) }
            Log.d("Period", period.toString())
            //
            val listDays = dao.selectAllDays()
            val currentSeries = getCurrentSeries(listDays)
            launch(Dispatchers.Main) { binding.mainStatCurrentSeries.setText(currentSeries.toString()) }
            Log.d("Series", currentSeries.toString())
            //
            launch(Dispatchers.Main) {
                binding.mainStatRecordSeries.setText(config.getInt(Constant.MAX_SERIES, 0).toString())
            }
            Log.d("Max series", config.getInt(Constant.MAX_SERIES, 0).toString())
        }
    }
    fun getCurrentSeries(listDays: List<ModelDays>): Int{
        Log.d("INFO", "${listDays.size}")
        if(listDays.size == 0)
            return 0
        var series = 0

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = MainActivity.data
        calendar[Calendar.HOUR_OF_DAY] = 12
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

        for(day in listDays.size-1 downTo 0 ){
            if(listDays[day].days_date != calendar.timeInMillis)
                break
            else
                series++
            Log.d("Current series", " calendar: ${getTime(calendar.timeInMillis)}, " +
                    "date: ${getTime(listDays[day].days_date)}, series: $series")
            calendar.add(Calendar.DAY_OF_WEEK, -1)
        }

        if(config.getInt(Constant.MAX_SERIES, 0) < series)
            config.edit().putInt(Constant.MAX_SERIES, series).apply()

        return series
    }

    fun selectingHabit(habit: String, period: String){
        lifecycleScope.launch(Dispatchers.IO) {
            when(habit){
                "water" -> installWaterStatistic(period)
                "steps" -> installStepsStatistic(period)
                "words" -> installWordsStatistic(period)
                "weather" -> installWeatherStatistic(period)
                "mood" -> installMoodStatistic(period)
                "productive" -> installProductiveStatistic(period)
                "sport" -> installSportStatistic(period)
                "sleep" -> installSleepStatistic(period)
                "screenTime" -> installScreenTimeStatistic(period)
                "cost" -> installCostStatistic(period)
                else -> {}
            }
        }
    }

    fun loadMainChartLine(){
        lifecycleScope.launch(Dispatchers.IO) {
            val lineList = ArrayList<Pair<String, Float>>()
            val list = dao.selectPeriodForWater(listDate.get("month")!!.toLong(), (listDate.get("today")?.plus(1))!!.toLong())
            list.forEach {
                val date = SimpleDateFormat("dd").format(Date(it.day_date))
                lineList.add(Pair(date, it.day_result.toFloat()))
                Log.d("line chart", "${it.day_date}, ${it.day_result}")
            }
            //choose list
            if(lineList.size == 1){
                val newList = ArrayList<Pair<String, Float>>()
                newList.add(Pair(getTime((date-86400001)), 0f))
                newList.add(lineList[0])

                lineList.clear()
                lineList.addAll(newList)
            }
            else if(lineList.isEmpty()){
                lineList.addAll(filNothingListWeek())
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

            val period = ((dateLastEntrance - dateFirstEntrance) / 86_400_000) + 1

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

    fun installWaterStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForWater(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForWater(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForWater(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installStepsStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){

            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForSteps(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForSteps(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForSteps(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installWordsStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForWords(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForWords(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForWords(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installWeatherStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForWeather(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForWeather(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForWeather(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installMoodStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForMood(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForMood(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForMood(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installProductiveStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForProductive(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForProductive(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForProductive(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installSportStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForSport(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForSport(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForSport(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installSleepStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForSleep(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForSleep(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForSleep(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                    
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installScreenTimeStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForScreenTime(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForScreenTime(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForScreenTime(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
            }
        }
    }
    fun installCostStatistic(period: String){
        lifecycleScope.launch(Dispatchers.Default){
            var listData = ArrayList<Pair<String, Float>>()
            //set date for selected period
            if(period == "month"){
                val list = dao.selectPeriodForCost(listDate.get(period) as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForMonth(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticMonth(if(listData.isEmpty()) filNothingListMonth() else listData) }
            }
            else if(period == "year"){
                val list = dao.selectPeriodForCost(listDate.get("year") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForYear(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticYear(if(listData.isEmpty()) filNothingListYear() else listData) }
            }
            else if(period == "week"){
                val list = dao.selectPeriodForCost(listDate.get("week") as Long, date + 1)
                if(list.size != 0 && list[0] != null){
                    val map = LinkedHashMap<String, Float>()
                    list.forEach { map.put(getTime(it.day_date), it.day_result.toFloat()) }
                    listData = getMediumForWeek(map)
                }
                launch(Dispatchers.Main) { setSelectedChartStatisticWeek(if(listData.isEmpty()) filNothingListWeek() else listData) }
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
                    ( if(list.isEmpty()) filNothingListWeek() else list )
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
    fun setSelectedChartStatisticWeek(list: List<Pair<String, Float>>) {
        delAllSelectChart()
        binding.selectedStatisticWeek.visibility = View.VISIBLE

        val selectedStatistic = binding.selectedStatisticWeek
        selectedStatistic.apply {
            selectedStatistic.animation.duration = animateDuration
            selectedStatistic.animate(list)
            selectedStatistic.barsColor = requireContext().getColor(R.color.color_1)
            selectedStatistic.onDataPointClickListener = {index, _, _ ->
                Toast.makeText(requireContext(), list.toList().get(index).second.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun setSelectedChartStatisticMonth(list: List<Pair<String, Float>>) {
        delAllSelectChart()
        binding.selectedStatisticMonth.visibility = View.VISIBLE
        binding.selectedLineMonth.visibility = View.VISIBLE

        val selectedStatistic = binding.selectedStatisticMonth
        selectedStatistic.apply {
            selectedStatistic.animation.duration = animateDuration
            selectedStatistic.animate(list)
            selectedStatistic.barsColor = requireContext().getColor(R.color.color_1)
            selectedStatistic.onDataPointClickListener = {index, _, _ ->
                Toast.makeText(requireContext(), list.toList().get(index).second.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun setSelectedChartStatisticYear(list: List<Pair<String, Float>>) {
        delAllSelectChart()
        binding.selectedStatisticYear.visibility = View.VISIBLE
        binding.selectedLineYear.visibility = View.VISIBLE

        val selectedStatistic = binding.selectedStatisticYear
        selectedStatistic.apply {
            selectedStatistic.animation.duration = animateDuration
            selectedStatistic.animate(list)
            selectedStatistic.barsColor = requireContext().getColor(R.color.color_1)
            selectedStatistic.onDataPointClickListener = {index, _, _ ->
                Toast.makeText(requireContext(), list.toList().get(index).second.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setSelectedChartLineYear(list: List<Pair<String, Float>>) {
        val lineChart = binding.selectedLineYear
        lineChart.apply {
            lineChart.tooltip.onCreateTooltip(lineChart)
            lineChart.animation.duration = animateDuration
            lineChart.gradientFillColors = intArrayOf(Color.parseColor("#F2C788"), Color.TRANSPARENT)
            lineChart.animate(list)
            lineChart.onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    ( if(list.isEmpty()) filNothingListYear() else list )
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun setSelectedChartLineMonth(list: List<Pair<String, Float>>) {
        val lineChart = binding.selectedLineMonth
        lineChart.apply {
            lineChart.tooltip.onCreateTooltip(lineChart)
            lineChart.animation.duration = animateDuration
            lineChart.gradientFillColors = intArrayOf(Color.parseColor("#F2C788"), Color.TRANSPARENT)
            lineChart.animate(list)
            lineChart.onDataPointClickListener = { index, _, _ ->
                Toast.makeText(
                    requireContext(),
                    ( if(list.isEmpty()) filNothingListMonth() else list )
                        .toList().get(index).second.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    fun delAllSelectChart(){
        binding.selectedStatisticWeek.visibility = View.GONE
        binding.selectedStatisticMonth.visibility = View.GONE
        binding.selectedStatisticYear.visibility = View.GONE
        binding.selectedLineYear.visibility = View.GONE
        binding.selectedLineMonth.visibility = View.GONE
    }

    //support method
    fun getMediumForWeek(map: LinkedHashMap<String, Float>): ArrayList<Pair<String, Float>> {
        val calendarDate = Calendar.getInstance()
        calendarDate[Calendar.HOUR_OF_DAY] = 12
        calendarDate[Calendar.MINUTE] = 0
        calendarDate[Calendar.SECOND] = 0
        calendarDate[Calendar.MILLISECOND] = 0
        calendarDate.add(Calendar.WEEK_OF_MONTH, -1)

        val newMap = java.util.LinkedHashMap<String, Float>()

        for(i in 0..7){
            if(map.get(getTime(calendarDate.getTimeInMillis())) == null)
                newMap.put(getTime(calendarDate.getTimeInMillis()), 0f);
            else
                newMap.put(getTime(calendarDate.timeInMillis), map.get(getTime(calendarDate.getTimeInMillis()))!!.toFloat());

            calendarDate.add(Calendar.DAY_OF_WEEK, 1);
        }

        calendarDate.add(Calendar.WEEK_OF_MONTH, -1)

        val listForReturn = ArrayList<Pair<String, Float>>()
        
        listForReturn.add(Pair(getTime(calendarDate.timeInMillis), newMap[getTime(calendarDate.timeInMillis)]!!))
        calendarDate.add(Calendar.DAY_OF_WEEK, 1)
        listForReturn.add(Pair(getTime(calendarDate.timeInMillis), newMap[getTime(calendarDate.timeInMillis)]!!))
        calendarDate.add(Calendar.DAY_OF_WEEK, 1)
        listForReturn.add(Pair(getTime(calendarDate.timeInMillis), newMap[getTime(calendarDate.timeInMillis)]!!))
        calendarDate.add(Calendar.DAY_OF_WEEK, 1)
        listForReturn.add(Pair(getTime(calendarDate.timeInMillis), newMap[getTime(calendarDate.timeInMillis)]!!))
        calendarDate.add(Calendar.DAY_OF_WEEK, 1)
        listForReturn.add(Pair(getTime(calendarDate.timeInMillis), newMap[getTime(calendarDate.timeInMillis)]!!))
        calendarDate.add(Calendar.DAY_OF_WEEK, 1)
        listForReturn.add(Pair(getTime(calendarDate.timeInMillis), newMap[getTime(calendarDate.timeInMillis)]!!))
        calendarDate.add(Calendar.DAY_OF_WEEK, 1)
        listForReturn.add(Pair(getTime(calendarDate.timeInMillis), newMap[getTime(calendarDate.timeInMillis)]!!))
        
        return listForReturn
    }
    fun getMediumForMonth(map: LinkedHashMap<String, Float>): ArrayList<Pair<String, Float>> {
        val calendarDate = Calendar.getInstance()
        calendarDate[Calendar.HOUR_OF_DAY] = 12
        calendarDate[Calendar.MINUTE] = 0
        calendarDate[Calendar.SECOND] = 0
        calendarDate[Calendar.MILLISECOND] = 0
        calendarDate.add(Calendar.MONTH, -1)

        val newMap = java.util.LinkedHashMap<String, Float>()

        for(i in 0..30){
            if(map.get(getTime(calendarDate.getTimeInMillis())) == null)
                newMap.put(getTime(calendarDate.getTimeInMillis()), 0f);
            else
                newMap.put(getTime(calendarDate.timeInMillis), map.get(getTime(calendarDate.getTimeInMillis()))!!.toFloat());

            calendarDate.add(Calendar.DAY_OF_WEEK, 1);
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val listData = ArrayList<Pair<String, Float>>()
            newMap.forEach { date, result ->  listData.add(Pair("", result))}

            setSelectedChartLineMonth(listData)
        }

        calendarDate.add(Calendar.DAY_OF_WEEK, -30)

        var a = 0f; var b = 0f; var c = 0f; var d = 0f
        var a_ = 0f; var b_ = 0f; var c_ = 0f; var d_ = 0f

        for (i in 0..30) {
            if (i < 7) {
                a_++; a += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            } else if (i < 14 && i > 6) {
                b_++; b += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            } else if (i < 21 && i > 13) {
                c_++; c += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            } else {
                d_++; d += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            calendarDate.add(Calendar.DAY_OF_WEEK, 1)
//            Log.d("Grage month", "${newMap[getTime(calendarDate.timeInMillis)]!!} -- $a - $a_, $b - $b_, $c - $c_, $d - $d_")
        }
        val listForReturn = ArrayList<Pair<String, Float>>()

        calendarDate.add(Calendar.DAY_OF_WEEK, 3)
        listForReturn.add(Pair(".." + getTime(calendarDate.timeInMillis) + "..", a/a_))
        calendarDate.add(Calendar.DAY_OF_WEEK, 7)
        listForReturn.add(Pair(".." + getTime(calendarDate.timeInMillis) + "..", b/b_))
        calendarDate.add(Calendar.DAY_OF_WEEK, 7)
        listForReturn.add(Pair(".." + getTime(calendarDate.timeInMillis) + "..", c/c_))
        calendarDate.add(Calendar.DAY_OF_WEEK, 7)
        listForReturn.add(Pair(".." + getTime(calendarDate.timeInMillis) + "..", d/d_))

        return listForReturn
    }
    fun getMediumForYear(map: LinkedHashMap<String, Float>): ArrayList<Pair<String, Float>> {
        val calendarDate = Calendar.getInstance()
        calendarDate[Calendar.HOUR_OF_DAY] = 12
        calendarDate[Calendar.MINUTE] = 0
        calendarDate[Calendar.SECOND] = 0
        calendarDate[Calendar.MILLISECOND] = 0

        calendarDate.set(Calendar.MONTH, 0)
        calendarDate.set(Calendar.DAY_OF_MONTH, 1)

        val newMap = java.util.LinkedHashMap<String, Float>()

        for(i in 0..365){
            if(map.get(getTime(calendarDate.getTimeInMillis())) == null)
                newMap.put(getTime(calendarDate.getTimeInMillis()), 0f);
            else
                newMap.put(getTime(calendarDate.timeInMillis), map.get(getTime(calendarDate.getTimeInMillis()))!!.toFloat());

            calendarDate.add(Calendar.DAY_OF_WEEK, 1);
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val listPrepareData = ArrayList<Pair<String, Float>>()
            newMap.forEach { date, result ->  listPrepareData.add(Pair(date, result))}
            val listData = listPrepareData.joinConsecutive{a, b -> (a+b) / 2f}

            setSelectedChartLineYear(listData)
        }

        calendarDate.set(Calendar.MONTH, 0)
        calendarDate.set(Calendar.DAY_OF_MONTH, 1)

        val monthGrade = FloatArray(12)
        val monthCount = FloatArray(12)

        for (i in 0..365) {
            if (i < 30) {
                monthCount[0]++; monthGrade[0] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 60) {
                monthCount[1]++; monthGrade[1] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 90) {
                monthCount[2]++; monthGrade[2] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 120) {
                monthCount[3]++; monthGrade[3] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 150) {
                monthCount[4]++; monthGrade[4] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 180) {
                monthCount[5]++; monthGrade[5] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 210) {
                monthCount[6]++; monthGrade[6] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 240) {
                monthCount[7]++; monthGrade[7] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 270) {
                monthCount[8]++; monthGrade[8] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 300) {
                monthCount[9]++; monthGrade[9] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else if (i < 330) {
                monthCount[10]++; monthGrade[10] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            else {
                monthCount[11]++; monthGrade[11] += newMap[getTime(calendarDate.timeInMillis)] ?: 0f
            }
            calendarDate.add(Calendar.DAY_OF_WEEK, 1)

        }
        val listForReturn = ArrayList<Pair<String, Float>>()

        calendarDate.add(Calendar.DAY_OF_MONTH, 15)
        for(i in 0..11){
            val month = getString(when(i){
                0 -> R.string.month_Jan
                1 -> R.string.month_Feb
                2 -> R.string.month_Mar
                3 -> R.string.month_Apr
                4 -> R.string.month_May
                5 -> R.string.month_Jun
                6 -> R.string.month_Jul
                7 -> R.string.month_Aug
                8 -> R.string.month_Sep
                9 -> R.string.month_Okt
                10 -> R.string.month_Nov
                11 -> R.string.month_Dec
                else -> {R.string.month_Apr}
            })

            listForReturn.add(Pair(month, monthGrade[i]/monthCount[i]))
            calendarDate.add(Calendar.MONTH, 1)
        }


        return listForReturn
    }
    fun List<Pair<String, Float>>.joinConsecutive(reducer: (Float, Float) -> Float): List<Pair<String, Float>> {
        val result = mutableListOf<Pair<String, Float>>()
        var count = 0
        var firstValue: Float? = null
        var secondValue: Float? = null
        this.forEach { (day, value) ->
            when (count % 2) {
                0 -> {
                    firstValue = value
                }
                1 -> {
                    secondValue = value
                    if (firstValue != null && secondValue != null) {
                        result.add(Pair("", reducer(firstValue ?: 0f, secondValue ?: 0f)))
                    }
                    firstValue = null
                    secondValue = null
                }
            }
            count++
        }
        return result
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

    fun filNothingListWeek():List<Pair<String, Float>> {
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
    fun filNothingListMonth():List<Pair<String, Float>> {
        val nothingList = ArrayList<Pair<String, Float>>()
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_WEEK, -26)
        nothingList.add(Pair(".." + getTime(calendar.timeInMillis) + "..", 0f))
        calendar.add(Calendar.DAY_OF_WEEK, 7)
        nothingList.add(Pair(".." + getTime(calendar.timeInMillis) + "..", 0f))
        calendar.add(Calendar.DAY_OF_WEEK, 7)
        nothingList.add(Pair(".." + getTime(calendar.timeInMillis) + "..", 0f))
        calendar.add(Calendar.DAY_OF_WEEK, 7)
        nothingList.add(Pair(".." + getTime(calendar.timeInMillis) + "..", 0f))
        calendar.add(Calendar.DAY_OF_WEEK, 7)

        return nothingList
    }
    fun filNothingListYear():List<Pair<String, Float>> {
        val nothingList = ArrayList<Pair<String, Float>>()

        for(i in 0..11){
            val month = getString(when(i){
                0 -> R.string.month_Jan
                1 -> R.string.month_Feb
                2 -> R.string.month_Mar
                3 -> R.string.month_Apr
                4 -> R.string.month_May
                5 -> R.string.month_Jun
                6 -> R.string.month_Jul
                7 -> R.string.month_Aug
                8 -> R.string.month_Sep
                9 -> R.string.month_Okt
                10 -> R.string.month_Nov
                11 -> R.string.month_Dec
                else -> {R.string.month_Apr}
            })

            nothingList.add(Pair(month, 0f))
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