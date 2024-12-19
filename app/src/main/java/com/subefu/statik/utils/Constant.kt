package com.subefu.statik.utils

import org.intellij.lang.annotations.Language

//Constant for config
class Constant{
    companion object{
        //config preferences
        val CONFIG = "config"

        val IS_FIRST_LAUNCH = "is_first_launch"

        val THEME = "theme"
        val LANGUAGE = "language"

        val NOTIFY_ENABLE = "notification_enable"
        val NOTIFY_TIME = "notification_time"

        //intent flag
        val HABIT_NAME = "habit_name"

        val listHabitInt = listOf("water","words","weather","mood","productive","sport")
        val listHabitFloat = listOf("steps","sleep","screen time","cost")

        //room
        val FIRST_ENTRANCE = "first_entrance"
        val LAST_ENTRANCE = "last_entrance"
        val ACTIVE_DAY = "active_day"
    }
}
