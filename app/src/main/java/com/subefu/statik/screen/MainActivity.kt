package com.subefu.statik.screen

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.RED
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.subefu.statik.R
import com.subefu.statik.R.color.accent
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
        setConfigBotNav()
    }

    fun setFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().replace(binding.globalFrameLayout.id, fragment).commit()
    }

    @SuppressLint("ResourceType")
    fun setConfigBotNav(){
        botNavMain = binding.globalBotNav
        botNavMain.itemActiveIndicatorColor = getColorStateList(R.color.transparent)
        botNavMain.isItemActiveIndicatorEnabled = true
        botNavMain.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_SELECTED
        botNavMain.itemIconTintList = getColorStateList(R.drawable.selector_icon_bot_nav)
        botNavMain.selectedItemId = R.id.bot_nav_menu_home
    }

}