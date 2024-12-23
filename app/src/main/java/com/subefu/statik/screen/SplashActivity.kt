package com.subefu.statik.screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.subefu.statik.R
import com.subefu.statik.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val aminImage = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.anim_splash_logo)
        val aminText = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.anim_splash_text)
        binding.splashLogo.startAnimation(aminImage)
        binding.splashText.startAnimation(aminText)

        val handler = Handler()
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1800)

    }
}