package com.subefu.statik.screen

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.transition.Visibility
import com.subefu.statik.R
import com.subefu.statik.databinding.ActivityGrageBinding
import com.subefu.statik.databinding.FragmentGlobalBinding
import com.subefu.statik.db.Dao
import com.subefu.statik.db.ModelDays
import com.subefu.statik.db.ModelHabitComment
import com.subefu.statik.db.ModelHabitCost
import com.subefu.statik.db.ModelHabitMood
import com.subefu.statik.db.ModelHabitProductive
import com.subefu.statik.db.ModelHabitScreenTime
import com.subefu.statik.db.ModelHabitSleep
import com.subefu.statik.db.ModelHabitSport
import com.subefu.statik.db.ModelHabitSteps
import com.subefu.statik.db.ModelHabitWater
import com.subefu.statik.db.ModelHabitWeather
import com.subefu.statik.db.ModelHabitWords
import com.subefu.statik.db.MyDatabase
import com.subefu.statik.model.HabitCard
import com.subefu.statik.screen.fragment.GlobalFragment
import com.subefu.statik.screen.fragment.GlobalFragment.Companion.adapter
import com.subefu.statik.utils.Constant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.LinkedHashMap
import java.util.LinkedList

class GradeActivity : AppCompatActivity() {

    lateinit var binding: ActivityGrageBinding
    lateinit var dao: Dao
    lateinit var config: SharedPreferences

    val habitList = LinkedList<String>()
    val resultList = LinkedList<String>()
    val listHabitCard = ArrayList<HabitCard>()

    var currentHabit = 0
    var date = MainActivity.data

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        binding.gradeActionNext.setOnClickListener {
            saveResults(habitList[currentHabit])
            currentHabit++
            showHabit(habitList[currentHabit], resultList[currentHabit])

            if(currentHabit == listHabitCard.size -1){
                binding.gradeActionNext.visibility = View.GONE
                binding.gradeActionOk.visibility = View.VISIBLE
            }

            updateLastEntrance()
        }
        binding.gradeActionOk.setOnClickListener {
            saveResults(habitList[currentHabit])
            finish()
        }
        binding.gradeActionComplete.setOnClickListener {
            saveResults(habitList[currentHabit])

            updateLastEntrance()

            finish()
        }

        binding.gradeActionPlus.setOnClickListener {
            var currentResult = binding.gradeActionResult.text.toString()
            if(currentResult.isEmpty() || currentResult.toFloat() == null)
                currentResult = "0"
            val habit = habitList[currentHabit]
            if(Constant.listHabitInt.contains(habit)){
                currentResult = (currentResult.toInt() + 1).toString()
            }
            else if(Constant.listHabitFloat.contains(habit)){
                currentResult = (currentResult.toFloat() + 0.1).toString()
                if(currentResult.length > 5)
                    currentResult = currentResult.substring(0, 3)
            }
            binding.gradeActionResult.setText(currentResult)
        }
        binding.gradeActionMinus.setOnClickListener {
            var currentResult = binding.gradeActionResult.text.toString()
            if (currentResult.isEmpty() || currentResult.toFloatOrNull() == null) {
                currentResult = "0"
            }
            val habit = habitList[currentHabit]

            if (currentResult != "0") {
                if (Constant.listHabitInt.contains(habit)) {
                    currentResult = (currentResult.toInt() - 1).toString()
                } else if (Constant.listHabitFloat.contains(habit)) {
                    val newValue = (currentResult.toFloat() - 0.1).toString()
                    currentResult = newValue.substring(0, 3)
                }
            }
            if (currentResult.toFloat() < 0.1) currentResult = "0"
            binding.gradeActionResult.setText(currentResult)
        }
    }
    fun updateLastEntrance(){
        if(config.getLong(Constant.LAST_ENTRANCE, 0L) != date){
            Log.d("Entrance info", "${config.getLong(Constant.FIRST_ENTRANCE, 0L)} " +
                    "- ${config.getLong(Constant.LAST_ENTRANCE, 0L)}")
            config.edit().putLong(Constant.LAST_ENTRANCE, date).apply()
            config.edit().putInt(Constant.ACTIVE_DAY, config.getInt(Constant.ACTIVE_DAY, 0) + 1).apply()
            Log.d("Entrance info", "${config.getLong(Constant.FIRST_ENTRANCE, 0L)} " +
                    "- ${config.getLong(Constant.LAST_ENTRANCE, 0L)}")
        }
        lifecycleScope.launch(Dispatchers.IO) {
            if(dao.findDayForDate(date) == null)
                dao.addNewDays(ModelDays(days_date = date))
        }
    }

    fun init(){
        dao = MyDatabase.getDb(baseContext).getDao()
        config = getSharedPreferences(Constant.CONFIG, 0)
        Log.d("Data for grade", "$date")

        loadHabitList()
        setHabitMap()

        Log.d("now habit: ", "${habitList[currentHabit]}, ${resultList[currentHabit]}")
        showHabit(habitList[currentHabit] ,resultList[currentHabit])

        if(config.getLong(Constant.FIRST_ENTRANCE, 0L) == 0L){
            config.edit().putLong(Constant.FIRST_ENTRANCE, date).apply()
        }
        Log.d("Entrance info", "${config.getLong(Constant.FIRST_ENTRANCE, 0L)} " +
        "- ${config.getLong(Constant.LAST_ENTRANCE, 0L)}")
    }

    fun saveResults(habit: String){
        val result = binding.gradeActionResult.text.toString()
        lifecycleScope.launch(Dispatchers.IO) {
            if(habit == "water"){
                launch { val isExist = dao.selectCurrentDataForWater(date)
                    if(isExist == null) dao.addNewRecordForWater(ModelHabitWater(day_date = date, day_result = result.toInt()))
                    else dao.updateRecordWater(result.toInt(), date)
            }}
            else if(habit == "steps"){
                launch { val isExist = dao.selectCurrentDataForSteps(date)
                    if(isExist == null) dao.addNewRecordForSteps(ModelHabitSteps(day_date = date, day_result = result.toFloat()))
                    else dao.updateRecordSteps(result.toFloat(), date)
                }
            }
            else if(habit == "mood"){
                launch { val isExist = dao.selectCurrentDataForMood(date)
                    if(isExist == null) dao.addNewRecordForMood(ModelHabitMood(day_date = date, day_result = result.toInt()))
                    else dao.updateRecordMood(result.toInt(), date)
                }
            }
            else if(habit == "weather"){
                launch { val isExist = dao.selectCurrentDataForWeather(date)
                    if(isExist == null) dao.addNewRecordForWeather(ModelHabitWeather(day_date = date, day_result = result.toInt()))
                    else dao.updateRecordWeather(result.toInt(), date)
                }
            }
            else if(habit == "cost"){
                launch { val isExist = dao.selectCurrentDataForCost(date)
                    if(isExist == null) dao.addNewRecordForCost(ModelHabitCost(day_date = date, day_result = result.toFloat()))
                    else dao.updateRecordCost(result.toFloat(), date)
                }
            }
            else if(habit == "sport"){
                launch { val isExist = dao.selectCurrentDataForSport(date)
                    if(isExist == null) dao.addNewRecordForSport(ModelHabitSport(day_date = date, day_result = result.toInt()))
                    else dao.updateRecordSport(result.toInt(), date)
                }
            }
            else if(habit == "comment"){
                launch { val isExist = dao.selectCurrentDataForComment(date)
                    if(isExist == null) dao.addNewRecordForComment(ModelHabitComment(day_date = date, day_result = result.toString()))
                    else dao.updateRecordComment(result.toString(), date)
                }
            }
            else if(habit == "words"){
                launch { val isExist = dao.selectCurrentDataForWords(date)
                    if(isExist == null) dao.addNewRecordForWords(ModelHabitWords(day_date = date, day_result = result.toInt()))
                    else dao.updateRecordWords(result.toInt(), date)
                }
            }
            else if(habit == "productive"){
                launch { val isExist = dao.selectCurrentDataForProductive(date)
                    if(isExist == null) dao.addNewRecordForProductive(ModelHabitProductive(day_date = date, day_result = result.toInt()))
                    else dao.updateRecordProductive(result.toInt(), date)
                }
            }
            else if(habit == "sleep"){
                launch { val isExist = dao.selectCurrentDataForSleep(date)
                    if(isExist == null) dao.addNewRecordForSleep(ModelHabitSleep(day_date = date, day_result = result.toFloat()))
                    else dao.updateRecordSleep(result.toFloat(), date)
                }
            }
            else if(habit == "screen time"){
                launch { val isExist = dao.selectCurrentDataForScreenTime(date)
                    if(isExist == null) dao.addNewRecordForScreenTime(ModelHabitScreenTime(day_date = date, day_result = result.toFloat()))
                    else dao.updateRecordScreenTime(result.toFloat(), date)
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun showHabit(habit: String, result: String){
        Log.d("now habit: ", "${habitList[currentHabit]}, ${resultList[currentHabit]}")
        Log.d("new habit: ", "-$habit, $result")
        binding.gradeHabitImage.background = when(habit){
            "water" -> getDrawable(R.drawable.habit_wather_png)
            "steps" -> getDrawable(R.drawable.habit_steps_png)
            "words" -> getDrawable(R.drawable.habit_words_png)
            "weather" -> getDrawable(R.drawable.habit_weather_png)
            "mood" -> getDrawable(R.drawable.habit_mood_png)
            "productive" -> getDrawable(R.drawable.habit_productive_png)
            "sleep" -> getDrawable(R.drawable.habit_sleep_png)
            "sport" -> getDrawable(R.drawable.habit_sport_png)
            "screen time" -> getDrawable(R.drawable.habit_screen_time_png)
            "cost" -> getDrawable(R.drawable.habit_cost_png)
            "comment" -> getDrawable(R.drawable.habit_comment_png)
            else -> null
        }
        binding.gradeHabitTitle.text = habit
        binding.gradeActionResult.setText(result)

        if(habitList[currentHabit] == "comment"){
            binding.gradeActionMinus.visibility = View.GONE
            binding.gradeActionPlus.visibility = View.GONE
            binding.gradeActionResult.textSize = 20f
        }
        else{
            binding.gradeActionMinus.visibility = View.VISIBLE
            binding.gradeActionPlus.visibility = View.VISIBLE
            binding.gradeActionResult.textSize = 35f
        }
    }
    fun loadHabitList(){
        listHabitCard.addAll(GlobalFragment.adapter.listHabit)
        listHabitCard.forEach {
            habitList.add(it.habit)
            resultList.add(it.count)
        }
    }

    fun setHabitMap(){
        val currentHabit = intent.getStringExtra(Constant.HABIT_NAME)
        if(currentHabit != null){
            val id = habitList.indexOf(currentHabit)
            val result = resultList[id]
            habitList.remove(currentHabit)
            resultList.removeAt(id)
            habitList.add(0, currentHabit)
            resultList.add(0, result)
            Log.d("First habit", "$currentHabit -- $result")
        }
    }

//    fun getDate(): Long{
//        var time = intent.getLongExtra(Constant.DATE, 0)
//        if(time == 0L){
//            val calendar = Calendar.getInstance()
//            calendar.set(Calendar.HOUR_OF_DAY, 12)
//            calendar.set(Calendar.MINUTE, 0)
//            calendar.set(Calendar.SECOND, 0)
//            calendar.set(Calendar.MILLISECOND, 0)
//            time = calendar.timeInMillis
//        }
//        Log.d("Current date", time.toString())
//        return time
//    }
}