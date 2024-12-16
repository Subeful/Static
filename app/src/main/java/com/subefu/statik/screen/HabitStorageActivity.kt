package com.subefu.statik.screen

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.subefu.statik.R
import com.subefu.statik.databinding.ActivityHabitStorageBinding

class HabitStorageActivity : AppCompatActivity() {

    lateinit var binding: ActivityHabitStorageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}