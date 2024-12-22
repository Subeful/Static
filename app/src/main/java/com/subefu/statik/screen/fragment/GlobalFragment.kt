package com.subefu.statik.screen.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.subefu.statik.R
import com.subefu.statik.databinding.FragmentGlobalBinding
import com.subefu.statik.model.HabitCard
import com.subefu.statik.adapter.HabitCardAdapter
import com.subefu.statik.db.ModelHabit
import com.subefu.statik.db.MyDatabase
import com.subefu.statik.screen.GradeActivity
import com.subefu.statik.screen.MainActivity
import com.subefu.statik.utils.Constant
import com.subefu.statik.utils.RemakeFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class GlobalFragment : Fragment() {

    lateinit var binding: FragmentGlobalBinding
    val listHabitCard = ArrayList<HabitCard>()
    lateinit var db: MyDatabase

    lateinit var remakeFragment: MainActivity

    var data = MainActivity.data
    @SuppressLint("StaticFieldLeak")
    companion object{
        lateinit var adapter: HabitCardAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentGlobalBinding.inflate(inflater)

        init()
        binding.globalStart.setOnClickListener {
            val intent = Intent(requireContext(), GradeActivity::class.java)
            requireContext().startActivity(intent)
        }

        binding.globalCalendar.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(requireContext().getString(R.string.system_select_date))
                    .setSelection(data)
                    .setTheme(R.style.ThemeOverlay_App_DatePicker)
                    .build()
            datePicker.addOnPositiveButtonClickListener {
                val newTime = datePicker.selection!!
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = newTime; calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0);calendar.set(Calendar.SECOND, 0);calendar.set(Calendar.MILLISECOND, 0);
                MainActivity.data = calendar.timeInMillis
                Log.d("New date global", SimpleDateFormat("dd.MM.yy").format(Date(MainActivity.data)))
                binding.currentDate.text = SimpleDateFormat("dd.MM.yy").format(Date(MainActivity.data))

                remakeFragment.setFragment(GlobalFragment())
            }
            datePicker.show(parentFragmentManager.beginTransaction(), "tag")
        }

        return binding.root
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        remakeFragment = activity!! as MainActivity
    }

    fun init() {
        db = MyDatabase.getDb(requireContext())
        Log.d("CREATE SCREEN", "Global fragment")
        binding.currentDate.setText(SimpleDateFormat("dd.MM.yy").format(Date(data)))

        adapter = HabitCardAdapter(requireContext(), listHabitCard)
        loadCard()
    }

    fun loadCard() {
        setEnambeHabit()

        val rvHabitCard = binding.rvHabitCard
        rvHabitCard.adapter = adapter
        rvHabitCard.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
    }

    fun setEnambeHabit() {
        db.getDao().selectAllEnableHabit().asLiveData().observe(viewLifecycleOwner) {
            loadHabitData(it)
        }
    }

    fun loadHabitData(habitsEnable: List<String>){
        lifecycleScope.launch(Dispatchers.IO) {
            if (habitsEnable.contains("water")) {
                val result = db.getDao().selectCurrentDataForWater(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("water", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("steps")) {
                val result = db.getDao().selectCurrentDataForSteps(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("steps", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("mood")) {
                val result = db.getDao().selectCurrentDataForMood(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("mood", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("weather")) {
                val result = db.getDao().selectCurrentDataForWeather(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("weather", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("cost")) {
                val result = db.getDao().selectCurrentDataForCost(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("cost", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("sport")) {
                val result = db.getDao().selectCurrentDataForSport(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("sport", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("comment")) {
                val result = db.getDao().selectCurrentDataForComment(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("comment", if (result != null) result.day_result else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("words")) {
                val result = db.getDao().selectCurrentDataForWords(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("words", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("productive")) {
                val result = db.getDao().selectCurrentDataForProductive(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard(
                            "productive",
                            if (result != null) result.day_result.toString() else "0"
                        )
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("sleep")) {
                val result = db.getDao().selectCurrentDataForSleep(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard("sleep", if (result != null) result.day_result.toString() else "0")
                    )
                    adapter.updateList(listHabitCard)
                }
            }
            if (habitsEnable.contains("screen_time")) {
                val result = db.getDao().selectCurrentDataForScreenTime(data)
                launch(Dispatchers.Main) {
                    listHabitCard.add(
                        HabitCard(
                            "screen time",
                            if (result != null) result.day_result.toString() else "0"
                        )
                    )
                    adapter.updateList(listHabitCard)
                }
            }
        }
    }
}