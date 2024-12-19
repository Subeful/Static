package com.subefu.statik.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ModelHabit::class, ModelHabitWater::class, ModelHabitSteps::class, ModelHabitWords::class,
                     ModelHabitWeather::class, ModelHabitMood::class, ModelHabitProductive::class, ModelHabitSport::class,
                     ModelHabitSleep::class, ModelHabitScreenTime::class, ModelHabitCost::class, ModelHabitComment::class],
    version = 1)
abstract class MyDatabase: RoomDatabase() {

    abstract fun getDao(): Dao
    companion object{
        fun getDb(context: Context) = Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, "habit_3.db")
            .build()
    }
}