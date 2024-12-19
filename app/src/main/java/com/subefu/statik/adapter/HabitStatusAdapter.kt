package com.subefu.statik.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.room.CoroutinesRoom
import com.subefu.statik.R
import com.subefu.statik.db.MyDatabase
import com.subefu.statik.model.HabitCard
import com.subefu.statik.model.HabitStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext

class HabitStatusAdapter(val context: Context, var listHabit: ArrayList<HabitStatus>, val mode: String, val lifecycle: LifecycleCoroutineScope)
    : RecyclerView.Adapter<HabitStatusAdapter.HabitViewHolder>() {

    val dao = MyDatabase.getDb(context).getDao()
    val scope = CoroutineScope(lifecycle.coroutineContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.model_habit_status, parent, false)
        return HabitViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.habit.text = listHabit[position].habit
        when(listHabit[position].habit){
            "water" -> holder.image.background = context.getDrawable(R.drawable.ic_water)
            "steps" -> holder.image.background = context.getDrawable(R.drawable.ic_steps)
            "words" -> holder.image.background = context.getDrawable(R.drawable.ic_words)
            "weather" -> holder.image.background = context.getDrawable(R.drawable.ic_weather)
            "mood" -> holder.image.background = context.getDrawable(R.drawable.ic_mood)
            "productive" -> holder.image.background = context.getDrawable(R.drawable.ic_productivity)
            "sleep" -> holder.image.background = context.getDrawable(R.drawable.ic_sleep)
            "sport" -> holder.image.background = context.getDrawable(R.drawable.ic_sport)
            "screen_time" -> holder.image.background = context.getDrawable(R.drawable.ic_screen_time)
            "cost" -> holder.image.background = context.getDrawable(R.drawable.ic_cust)
            "comment" -> holder.image.background = context.getDrawable(R.drawable.ic_comment)
        }

        if(mode == "archive"){
            holder.action.background = context.getDrawable(R.drawable.ic_system_habit_add)
            holder.action.setOnClickListener {
                updateHabitStatus(position, true)
                notifyDataSetChanged()
            }
        } else {
            holder.action.background = context.getDrawable(R.drawable.ic_system_habit_del)
            holder.action.setOnClickListener {
                updateHabitStatus(position, false)
                notifyDataSetChanged()
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: ArrayList<HabitStatus>){
        listHabit.clear()
        listHabit.addAll(newList)
        notifyDataSetChanged()
        Log.d("Adapter_notify", "${newList.size}")
        Log.d("Adapter_notify", "${listHabit.size}")
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateHabitStatus(position: Int, status: Boolean){
        val count = itemCount
        Log.d("Adapter", "$count - $position")
        scope.launch(Dispatchers.IO) {
            dao.changeHabitEnable(listHabit[position].habit, status)
            launch(Dispatchers.Main) {
                listHabit.removeAt(position)
                notifyDataSetChanged() }
        }
    }

    override fun getItemCount() = listHabit.size

    class HabitViewHolder(view: View): RecyclerView.ViewHolder(view){
        val habit = view.findViewById<TextView>(R.id.model_habit_text)
        val image = view.findViewById<ImageView>(R.id.model_habit_image)
        val action = view.findViewById<ImageButton>(R.id.model_habit_action)
    }
}