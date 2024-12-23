package com.subefu.statik.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "habits")
data class ModelHabit(
    @PrimaryKey(autoGenerate = true)
    var habit_id: Int = 0,
    @ColumnInfo(name = "habit_name")
    var habit_name: String = "",
    @ColumnInfo(name = "habit_enable")
    var habit_enable: Boolean = true,

    @ColumnInfo(name = "habit_target")
    var habit_target: Float = 0f
)

@Entity(tableName = "days")
data class ModelDays(
    @PrimaryKey(autoGenerate = true)
    var days_id: Int = 0,
    @ColumnInfo(name = "days_date")
    var days_date: Long = 0L
)

@Entity(tableName = "water_days")
data class ModelHabitWater(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Int = 0,
)
@Entity(tableName = "steps_days")
data class ModelHabitSteps(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Float = 0f,
)
@Entity(tableName = "words_days")
data class ModelHabitWords(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Int = 0,
)
@Entity(tableName = "weather_days")
data class ModelHabitWeather(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Int = 0,
)
@Entity(tableName = "mood_days")
data class ModelHabitMood(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Int = 0,
)
@Entity(tableName = "productive_days")
data class ModelHabitProductive(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Int = 0,
)
@Entity(tableName = "sport_days")
data class ModelHabitSport(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Int = 0,
)
@Entity(tableName = "sleep_days")
data class ModelHabitSleep(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Float = 0f,
)
@Entity(tableName = "screen_days")
data class ModelHabitScreenTime(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Float = 0f,
)
@Entity(tableName = "cost_days")
data class ModelHabitCost(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: Float = 0f,
)
@Entity(tableName = "comment_days")
data class ModelHabitComment(
    @PrimaryKey(autoGenerate = true)
    var day_id: Int = 0,
    @ColumnInfo(name = "day_date")
    var day_date: Long = 0L,
    @ColumnInfo(name = "day_result")
    var day_result: String = "",
)


