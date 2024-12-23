package com.subefu.statik.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface Dao {
    //util


    //Habit
    @Insert
    fun addNewHabit(habit: ModelHabit)
    @Query("update habits set habit_enable = :isEnable where habit_name = :habit")
    fun changeHabitEnable(habit: String, isEnable: Boolean)
    @Query("select habit_name from habits where habit_enable = 1")
    fun selectAllEnableHabit(): Flow<List<String>>
    @Query("select habit_name from habits where habit_enable = 0")
    fun selectAllArchiveHabit(): Flow<List<String>>

    //Days
    @Insert
    fun addNewDays(days: ModelDays)
    @Query("select count(*) from days")
    fun getCountDaysInApp(): Int
    @Query("select * from days where days_date = :date limit 1")
    fun findDayForDate(date: Long): ModelDays
    @Query("select * from days")
    fun selectAllDays(): List<ModelDays>

    //Water
    @Insert
    fun addNewRecordForWater(dayWater: ModelHabitWater)
    @Query("update water_days set day_result = :newResult where day_date = :date")
    fun updateRecordWater(newResult: Int, date: Long)
    @Query("select * from water_days where day_date between :startDate and :endDate")
    fun selectPeriodForWater(startDate: Long, endDate: Long): List<ModelHabitWater>
    @Query("select * from water_days where day_date = :date limit 1")
    fun selectCurrentDataForWater(date: Long): ModelHabitWater

    //Steps
    @Insert
    fun addNewRecordForSteps(daySteps: ModelHabitSteps)
    @Query("update steps_days set day_result = :newResult where day_date = :date")
    fun updateRecordSteps(newResult: Float, date: Long)
    @Query("select * from steps_days where day_date between :startDate and :endDate")
    fun selectPeriodForSteps(startDate: Long, endDate: Long): List<ModelHabitSteps>
    @Query("select * from steps_days where day_date = :date limit 1")
    fun selectCurrentDataForSteps(date: Long): ModelHabitSteps?

    //Words
    @Insert
    fun addNewRecordForWords(dayWords: ModelHabitWords)
    @Query("update words_days set day_result = :newResult where day_date = :date")
    fun updateRecordWords(newResult: Int, date: Long)
    @Query("select * from words_days where day_date between :startDate and :endDate")
    fun selectPeriodForWords(startDate: Long, endDate: Long): List<ModelHabitWords>
    @Query("select * from words_days where day_date = :date limit 1")
    fun selectCurrentDataForWords(date: Long): ModelHabitWords

    //Weather
    @Insert
    fun addNewRecordForWeather(dayWeather: ModelHabitWeather)
    @Query("update weather_days set day_result = :newResult where day_date = :date")
    fun updateRecordWeather(newResult: Int, date: Long)
    @Query("select * from weather_days where day_date between :startDate and :endDate")
    fun selectPeriodForWeather(startDate: Long, endDate: Long): List<ModelHabitWeather>
    @Query("select * from weather_days where day_date = :date limit 1")
    fun selectCurrentDataForWeather(date: Long): ModelHabitWeather

    //Mood
    @Insert
    fun addNewRecordForMood(dayMood: ModelHabitMood)
    @Query("update mood_days set day_result = :newResult where day_date = :date")
    fun updateRecordMood(newResult: Int, date: Long)
    @Query("select * from mood_days where day_date between :startDate and :endDate")
    fun selectPeriodForMood(startDate: Long, endDate: Long): List<ModelHabitMood>
    @Query("select * from mood_days where day_date = :date limit 1")
    fun selectCurrentDataForMood(date: Long): ModelHabitMood

    //Productive
    @Insert
    fun addNewRecordForProductive(dayProductive: ModelHabitProductive)
    @Query("update productive_days set day_result = :newResult where day_date = :date")
    fun updateRecordProductive(newResult: Int, date: Long)
    @Query("select * from productive_days where day_date between :startDate and :endDate")
    fun selectPeriodForProductive(startDate: Long, endDate: Long): List<ModelHabitProductive>
    @Query("select * from productive_days where day_date = :date limit 1")
    fun selectCurrentDataForProductive(date: Long): ModelHabitProductive

    //Sport
    @Insert
    fun addNewRecordForSport(daySport: ModelHabitSport)
    @Query("update sport_days set day_result = :newResult where day_date = :date")
    fun updateRecordSport(newResult: Int, date: Long)
    @Query("select * from sport_days where day_date between :startDate and :endDate")
    fun selectPeriodForSport(startDate: Long, endDate: Long): List<ModelHabitSport>
    @Query("select * from sport_days where day_date = :date limit 1")
    fun selectCurrentDataForSport(date: Long): ModelHabitSport

    //ScreenTime
    @Insert
    fun addNewRecordForScreenTime(dayScreenTime: ModelHabitScreenTime)
    @Query("update screen_days set day_result = :newResult where day_date = :date")
    fun updateRecordScreenTime(newResult: Float, date: Long)
    @Query("select * from screen_days where day_date between :startDate and :endDate")
    fun selectPeriodForScreenTime(startDate: Long, endDate: Long): List<ModelHabitScreenTime>
    @Query("select * from screen_days where day_date = :date limit 1")
    fun selectCurrentDataForScreenTime(date: Long): ModelHabitScreenTime

    //Sleep
    @Insert
    fun addNewRecordForSleep(daySleep: ModelHabitSleep)
    @Query("update sleep_days set day_result = :newResult where day_date = :date")
    fun updateRecordSleep(newResult: Float, date: Long)
    @Query("select * from sleep_days where day_date between :startDate and :endDate")
    fun selectPeriodForSleep(startDate: Long, endDate: Long): List<ModelHabitSleep>
    @Query("select * from sleep_days where day_date = :date limit 1")
    fun selectCurrentDataForSleep(date: Long): ModelHabitSleep

    //Cost
    @Insert
    fun addNewRecordForCost(dayCost: ModelHabitCost)
    @Query("update cost_days set day_result = :newResult where day_date = :date")
    fun updateRecordCost(newResult: Float, date: Long)
    @Query("select * from cost_days where day_date between :startDate and :endDate")
    fun selectPeriodForCost(startDate: Long, endDate: Long): List<ModelHabitCost>
    @Query("select * from cost_days where day_date = :date limit 1")
    fun selectCurrentDataForCost(date: Long): ModelHabitCost

    //Comment
    @Insert
    fun addNewRecordForComment(dayComment: ModelHabitComment)
    @Query("update comment_days set day_result = :newResult where day_date = :date")
    fun updateRecordComment(newResult: String, date: Long)
    @Query("select * from comment_days where day_date between :startDate and :endDate")
    fun selectPeriodForComment(startDate: Long, endDate: Long): List<ModelHabitComment>
    @Query("select * from comment_days where day_date = :date limit 1")
    fun selectCurrentDataForComment(date: Long): ModelHabitComment

}