package com.subefu.statik.screen.fragment

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.subefu.statik.R
import com.subefu.statik.databinding.FragmentSettingsBinding
import com.subefu.statik.recevier.NotificationReceiver
import com.subefu.statik.screen.HabitStorageActivity
import com.subefu.statik.utils.Constant
import com.subefu.statik.utils.UpdateFragment
import java.util.Calendar


class SettingsFragment : Fragment() {

    lateinit var binding: FragmentSettingsBinding
    lateinit var config: SharedPreferences

    lateinit var functionUpdateFragment: UpdateFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsBinding.inflate(inflater)

        config = requireContext().getSharedPreferences(Constant.CONFIG, 0)

        binding.setCustomTheme.setOnClickListener { showChooseTheme() }
//        binding.setCustomColorDesibn.setOnClickListener { }

        binding.setCustomLanguage.setOnClickListener { showChooseLanguage() }

        binding.setNotify.setOnClickListener { showNotificationConfig() }

        binding.setServiceShare.setOnClickListener { showShare() }
        binding.setServiceFeedback.setOnClickListener { showFeedback() }
        binding.setServiceRating.setOnClickListener { showRating() }

        binding.setHabitActive.setOnClickListener {
            val intent = Intent(requireContext(), HabitStorageActivity::class.java)
            intent.putExtra(Constant.MODE, Constant.ACTIVE)
            startActivity(intent)
        }
        binding.setHabitArchive.setOnClickListener {
            val intent = Intent(requireContext(), HabitStorageActivity::class.java)
            intent.putExtra(Constant.MODE, Constant.ARCHIVE)
            startActivity(intent)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        functionUpdateFragment = context as UpdateFragment
    }

    @SuppressLint("MissingInflatedId")
    fun showRating(){
        val view = layoutInflater.inflate(R.layout.alert_rating, null)

        val rating = view.findViewById<RatingBar>(R.id.ratingBar)
        val complete = view.findViewById<Button>(R.id.rating_complete)

        val alert = AlertDialog.Builder(requireContext()).setView(view)
        val dialog = alert.create()
        dialog.window?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.shape_alert))
        dialog.setOnCancelListener {
        }
        complete.setOnClickListener {
            dialog.cancel()

            Toast.makeText(requireContext(), getString(when(rating.rating){
                in 0f..3f -> R.string.system_rating_bed
                in 3.5f..4.5f -> R.string.system_rating_notmal
                5f -> R.string.system_rating_best
                else -> {1}
            }), Toast.LENGTH_LONG).show()
        }
        dialog.show()
    }

    fun showShare(){
        val sendIntent = Intent()
        sendIntent.setAction(Intent.ACTION_SEND)
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Приложение Statik, скачивай от сюда - https://youtu.be/dQw4w9WgXcQ")
        sendIntent.setType("text/plain")
        startActivity(Intent.createChooser(sendIntent, "Поделиться"))
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun showFeedback(){
        val shareIntent = ShareCompat.IntentBuilder(requireContext())
            .setEmailTo(arrayOf("subefu@mail.ru"))
            .setType("text/plain")
            .setChooserTitle("Выберите приложение для отправки электронной почты")
            .setSubject(requireContext().getString(R.string.set_service_feedback_title))
            .setText(requireContext().getString(R.string.set_service_feedback_message))
        if (requireContext().getPackageManager() != null){
            shareIntent.startChooser()
        }


    }

    @SuppressLint("MissingInflatedId", "NewApi")
    fun showNotificationConfig(){
        val view = layoutInflater.inflate(R.layout.alert_set_notification, null)

        val notifyEnable = view.findViewById<CheckBox>(R.id.alert_notify_enable)
        val notifyTime = view.findViewById<ImageButton>(R.id.alert_notify_time)
        val notifyTimeText = view.findViewById<TextView>(R.id.alert_notify_time_text)
        val notifySettings = view.findViewById<ImageButton>(R.id.alert_notify_settings)

        val complete = view.findViewById<Button>(R.id.alert_notify_complete)

        val alert = AlertDialog.Builder(requireContext()).setView(view)
        val dialog = alert.create()
        dialog.window?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.shape_alert))

        val theme = config.getString(Constant.THEME, "system")
        var timeAlarm = 0L

        when (config.getString(Constant.NOTIFY_ENABLE, "false")) {
            "true" -> {
                notifyEnable.isChecked = true
                notifyTime.isClickable = true
                notifyTime.isEnabled = true
                if(theme == "light"){
                    notifyTime.background = (resources.getDrawable(R.drawable.ic_system_notify_alarm_on))
                    notifyTimeText.setTextColor(resources.getColor(R.color.l_dark))
                }
                else{
                    notifyTime.background = (resources.getDrawable(R.drawable.ic_system_notify_alarm_on_n))
                    notifyTimeText.setTextColor(resources.getColor(R.color.n_light))
                }
            }
            else -> {
                notifyEnable.isChecked = false
                notifyTime.isClickable = false
                notifyTime.isEnabled = false
                notifyTime.background = (resources.getDrawable(R.drawable.ic_system_notify_alarm_off))
                notifyTimeText.setTextColor(resources.getColor(R.color.grey))
            }
        }

        notifyEnable.setOnCheckedChangeListener { buttonView, isChecked ->
            notifyTime.isClickable = isChecked
            notifyTime.isEnabled = isChecked
            if(isChecked) {
                checkPermissionNotification()
                if (theme == "light") {
                    notifyTime.background =
                        (resources.getDrawable(R.drawable.ic_system_notify_alarm_on))
                    notifyTimeText.setTextColor(resources.getColor(R.color.l_dark))
                }
                else {
                    notifyTime.background =
                        (resources.getDrawable(R.drawable.ic_system_notify_alarm_on_n))
                    notifyTimeText.setTextColor(resources.getColor(R.color.n_light))
                }
                config.edit().putString(Constant.NOTIFY_ENABLE, "true").apply()
            }
            else{
                notifyTime.background = (resources.getDrawable(R.drawable.ic_system_notify_alarm_off))
                notifyTimeText.setTextColor(resources.getColor(R.color.grey))
                config.edit().putString(Constant.NOTIFY_ENABLE, "false").apply()
            }
        }

        val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(22).setMinute(0).setTitleText("Select Appointment time").build()
        notifyTime.setOnClickListener {
            timePicker.show(requireFragmentManager(), "tag")
        }

        timePicker.addOnPositiveButtonClickListener {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour);
            calendar.set(Calendar.MINUTE, timePicker.minute);
            timeAlarm = calendar.timeInMillis
        }

        notifySettings.setOnClickListener {
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel: NotificationChannel = notificationManager.getNotificationChannel("0209")

            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.id)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName())
            startActivity(intent)
        }

        complete.setOnClickListener {
            config.edit().putLong(Constant.NOTIFY_TIME, timeAlarm).apply()
            setNotificationRecevier(requireContext(), timeAlarm)

            dialog.cancel()
        }

        dialog.show()
    }
    @SuppressLint("ScheduleExactAlarm")
    fun setNotificationRecevier(context: Context, timeInMillis: Long){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(PendingIntent.getBroadcast(context, 0, Intent(context, NotificationReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
    @SuppressLint("NewApi")
    fun checkPermissionNotification(){
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel: NotificationChannel = notificationManager.getNotificationChannel("0209")

        if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
            val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, channel.id)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName())
            startActivity(intent)
        }
    }

    @SuppressLint("MissingInflatedId")
    fun showChooseLanguage(){
        val view = layoutInflater.inflate(R.layout.alert_choose_language, null)

        val ru_bt = view.findViewById<RadioButton>(R.id.choose_language_ru_bt)
        val en_bt = view.findViewById<RadioButton>(R.id.choose_language_en_bt)

        val complete = view.findViewById<Button>(R.id.choose_language_complete)

        val alert = AlertDialog.Builder(requireContext()).setView(view)
        val dialog = alert.create()
        dialog.window?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.shape_alert))

        var language = ""

        when (config.getString(Constant.LANGUAGE, "ru")) {
            "ru" -> {ru_bt.isChecked = true
                language = "ru"}
            else -> {en_bt.isChecked = true
                language = "en"}
        }

        ru_bt.setOnClickListener {
            en_bt.isChecked = false
            language = "ru"
        }
        en_bt.setOnClickListener {
            ru_bt.isChecked = false
            language = "en"
        }

        complete.setOnClickListener {
            config.edit().putString(Constant.LANGUAGE, language).apply()
            dialog.cancel()
            functionUpdateFragment.updateFragment(this)
        }

        dialog.show()
    }

    //color
    /*fun showChooseColor(){
        val view = layoutInflater.inflate(R.layout.alert_choose_color, null)

        val color_1 = view.findViewById<RadioButton>(R.id.choose_color_1)
        val color_2 = view.findViewById<RadioButton>(R.id.choose_color_2)
        val color_3 = view.findViewById<RadioButton>(R.id.choose_color_3)
        val color_4 = view.findViewById<RadioButton>(R.id.choose_color_4)
        val color_5 = view.findViewById<RadioButton>(R.id.choose_color_5)
        val color_6 = view.findViewById<RadioButton>(R.id.choose_color_6)

        val complete = view.findViewById<Button>(R.id.alert_color_complete)

        val alert = AlertDialog.Builder(requireContext()).setView(view)
        val dialog = alert.create()
        dialog.window?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.shape_alert))

        var color = ""

        when(getColor()){
            "#E9BC7A" -> {color_1.isChecked = true; color = "#E9BC7A"}
            "#9CBA6B" -> {color_2.isChecked = true; color = "#9CBA6B"}
            "#6BBACC" -> {color_3.isChecked = true; color = "#6BBACC"}
            "#E789E9" -> {color_4.isChecked = true; color = "#E789E9"}
            "#DE8768" -> {color_5.isChecked = true; color = "#DE8768"}
            "#EFDC71" -> {color_6.isChecked = true; color = "#EFDC71"}
        }

        color_1.setOnClickListener {
            clearChooseColor(view)
            color_1.isChecked = true
            color = "#E9BC7A"
        }
        color_2.setOnClickListener {
            clearChooseColor(view)
            color_2.isChecked = true
            color = "#9CBA6B"
        }
        color_3.setOnClickListener {
            clearChooseColor(view)
            color_3.isChecked = true
            color = "#6BBACC"
        }
        color_4.setOnClickListener {
            clearChooseColor(view)
            color_4.isChecked = true
            color = "#E789E9"
        }
        color_5.setOnClickListener {
            clearChooseColor(view)
            color_5.isChecked = true
            color = "#DE8768"
        }
        color_6.setOnClickListener {
            clearChooseColor(view)
            color_6.isChecked = true
            color = "#EFDC71"
        }

        complete.setOnClickListener {
            updateColor(color)
            dialog.cancel()
        }

        dialog.show()
    }

    fun updateColor(color: String){
        config.edit().putString("color", color).apply()
    }
    fun getColor() = config.getString("color", "#F2C788")
    fun clearChooseColor(views: View){
        views.findViewById<RadioButton>(R.id.choose_color_1).isChecked = false
        views.findViewById<RadioButton>(R.id.choose_color_2).isChecked = false
        views.findViewById<RadioButton>(R.id.choose_color_3).isChecked = false
        views.findViewById<RadioButton>(R.id.choose_color_4).isChecked = false
        views.findViewById<RadioButton>(R.id.choose_color_5).isChecked = false
        views.findViewById<RadioButton>(R.id.choose_color_6).isChecked = false
    }*/


    fun showChooseTheme(){
        val view = layoutInflater.inflate(R.layout.alert_choose_theme, null)

        val light = view.findViewById<RadioButton>(R.id.choose_theme_light)
        val night = view.findViewById<RadioButton>(R.id.choose_theme_night)
        val system = view.findViewById<RadioButton>(R.id.choose_theme_system)

        val complete = view.findViewById<Button>(R.id.alert_theme_complete)

        val alert = AlertDialog.Builder(requireContext()).setView(view)
        val dialog = alert.create()
        dialog.window?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.shape_alert))

        var theme = ""

        when(getTheme()){
            "light" -> {light.isChecked = true; theme = "light"}
            "night" -> {night.isChecked = true; theme = "night"}
            "system" -> {system.isChecked = true; theme = "system"}
        }

        light.setOnClickListener {
            night.isChecked = false; system.isChecked = false
            theme = "light"
        }
        night.setOnClickListener {
            light.isChecked = false; system.isChecked = false
            theme = "night"
        }
        system.setOnClickListener {
            night.isChecked = false; light.isChecked = false
            theme = "system"
        }

        complete.setOnClickListener {
            setTheme(theme)
            dialog.cancel()
        }

        dialog.show()
    }

    fun getTheme() = config.getString(Constant.THEME, "system")
    fun setTheme(themeMode: String){
        when(themeMode){
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "night" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" ->AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        val editor = config.edit()
        editor.putString(Constant.THEME, themeMode).apply()
    }

}