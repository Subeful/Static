package com.subefu.statik.screen

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.subefu.statik.R
import com.subefu.statik.databinding.ActivityMainBinding
import com.subefu.statik.screen.fragment.GlobalFragment
import com.subefu.statik.screen.fragment.SettingsFragment
import com.subefu.statik.screen.fragment.StatisticFragment

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var botNavMain: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        botNavMain.setOnItemSelectedListener{
            when(it.itemId){
                R.id.bot_nav_menu_statistic -> setFragment(StatisticFragment())
                R.id.bot_nav_menu_home -> setFragment(GlobalFragment())
                R.id.bot_nav_menu_settings -> setFragment(SettingsFragment())
            }
            true
        }

    }
    fun init(){
        setFragment(GlobalFragment())
        botNavMain = binding.globalBotNav
        botNavMain.itemActiveIndicatorColor = getColorStateList(R.color.transparent)
    }

    fun setFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.globalFrameLayout.id, fragment).commit()
    }

}