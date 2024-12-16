package com.subefu.statik.screen

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.subefu.statik.R
import com.subefu.statik.databinding.ActivityMainBinding
import com.subefu.statik.screen.fragment.GlobalFragment
import com.subefu.statik.screen.fragment.SettingsFragment
import com.subefu.statik.screen.fragment.StatisticFragment
import com.subefu.statik.utils.Constant
import com.subefu.statik.utils.UpdateFragment
import java.util.Locale


class MainActivity : AppCompatActivity(), UpdateFragment {

    lateinit var binding: ActivityMainBinding
    lateinit var botNavMain: BottomNavigationView

    lateinit var config: SharedPreferences

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

//        val manager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val chan = manager.getNotificationChannel("0209")
//            if(chan == null){
//                val channel = NotificationChannel("0209", "Напоминания", NotificationManager.IMPORTANCE_HIGH)
//                channel.description = "Напоминания об оценке статистики дня"
//                channel.enableLights(true)
//                channel.lightColor = Color.GREEN
//                channel.enableVibration(false)
//                manager.createNotificationChannel(channel)
//            }
//        }

    }

    override fun onResume() {
        super.onResume()
        setLanguage()
        botNavMain.selectedItemId = R.id.bot_nav_menu_home
    }

    fun init(){
        setFragment(GlobalFragment())
        setConfigBotNav()

        config = getSharedPreferences(Constant.CONFIG, MODE_PRIVATE)

        setTheme(config.getString(Constant.THEME, "system").toString())
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

    fun setTheme(themeMode: String){
        when(themeMode){
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "night" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    override fun updateFragment(fragment: Fragment) {
        finish()
        startActivity(Intent(intent))
        setFragment(fragment)
        setLanguage()
    }
    fun setLanguage(){
        val language = config.getString(Constant.LANGUAGE, "ru")
        val locale = Locale(language.toString())
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)

        botNavMain.menu.clear()
        botNavMain.inflateMenu(R.menu.bot_nav_menu)
    }
}