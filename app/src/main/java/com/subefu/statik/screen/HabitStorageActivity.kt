package com.subefu.statik.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect.Descriptor
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.subefu.statik.R
import com.subefu.statik.adapter.HabitStatusAdapter
import com.subefu.statik.databinding.ActivityHabitStorageBinding
import com.subefu.statik.db.Dao
import com.subefu.statik.db.MyDatabase
import com.subefu.statik.model.HabitCard
import com.subefu.statik.model.HabitStatus
import com.subefu.statik.screen.fragment.SettingsFragment
import com.subefu.statik.utils.Constant
import com.subefu.statik.utils.UpdateFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class HabitStorageActivity : AppCompatActivity() {

    lateinit var binding: ActivityHabitStorageBinding
    lateinit var dao: Dao

    lateinit var adapter: HabitStatusAdapter
    lateinit var mode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

        if(mode == "active"){
            binding.habitStatusTitle.text = getString(R.string.set_habit_activate)
            dao.selectAllEnableHabit().asLiveData().observe(this){
                val newList = ArrayList<HabitStatus>()
                it.forEach { i ->
                    newList.add(HabitStatus(i, false))
                    Log.d("HabitStatus", i); Log.d("HabitList", "size ${newList.size}")
                }
                adapter.updateList(newList)
                if(newList.size == 0)
                    showLink()
            }
        }
        else if(mode == "archive"){
            binding.habitStatusTitle.text = getString(R.string.set_habit_archive)
            dao.selectAllArchiveHabit().asLiveData().observe(this) {
                val newList = ArrayList<HabitStatus>()
                it.forEach { i ->
                    newList.add(HabitStatus(i, false))
                    Log.d("HabitStatus", i); Log.d("HabitList", "size ${newList.size}")
                }
                adapter.updateList(newList)
                if(newList.size == 0)
                    showLink()
            }
        }

        binding.habitStatusBack.setOnClickListener { finish() }
    }

    fun init(){
        mode = intent.getStringExtra(Constant.MODE).toString()
        dao = MyDatabase.getDb(baseContext).getDao()
        adapter = HabitStatusAdapter(baseContext, ArrayList<HabitStatus>(), mode, lifecycleScope)

        binding.habitStatusRv.adapter = adapter
    }

    fun showLink(){
        Log.d("Status habit", "${adapter.listHabit.size}")
        if(adapter.listHabit.size != 0)
            return

        if(mode == "active"){
            binding.storageNotFoundText.setText(getString(R.string.system_not_found_active_habit))
            binding.storageNotFoundLink.setText(getString(R.string.system_not_found_habit_go_archive))
            binding.storageNotFoundLink.setOnClickListener {
                finish();
                val intent = Intent(this, HabitStorageActivity::class.java)
                intent.putExtra(Constant.MODE, Constant.ARCHIVE)
                startActivity(intent)
            }
        } else{
            binding.storageNotFoundText.setText(getString(R.string.system_not_found_archive_habit))
            binding.storageNotFoundLink.setText(getString(R.string.system_not_found_habit_go_active))
            binding.storageNotFoundLink.setOnClickListener {
                finish();
                val intent = Intent(this, HabitStorageActivity::class.java)
                intent.putExtra(Constant.MODE, Constant.ACTIVE)
                startActivity(intent)
            }
        }

        binding.storageNotFound.visibility = View.VISIBLE
    }
}