package com.subefu.statik.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subefu.statik.R
import com.subefu.statik.model.Habit

class HabitAdapter(val context: Context, val listHabit: List<Habit>): RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.model_card, parent, false)
        return HabitViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.count.text = listHabit[position].count.toString()

        when(listHabit[position].habit){
            "water" -> holder.image.background = context.getDrawable(R.drawable.ic_water)
            "steps" -> holder.image.background = context.getDrawable(R.drawable.ic_steps)
            "words" -> holder.image.background = context.getDrawable(R.drawable.ic_words)
            "weather" -> holder.image.background = context.getDrawable(R.drawable.ic_weather)
            "mood" -> holder.image.background = context.getDrawable(R.drawable.ic_mood)
            "productivity" -> holder.image.background = context.getDrawable(R.drawable.ic_productivity)
            "sleep" -> holder.image.background = context.getDrawable(R.drawable.ic_sleep)
            "sport" -> holder.image.background = context.getDrawable(R.drawable.ic_sport)
            "screen_time" -> holder.image.background = context.getDrawable(R.drawable.ic_screen_time)
            "cost" -> holder.image.background = context.getDrawable(R.drawable.ic_cust)
            "comment" -> holder.image.background = context.getDrawable(R.drawable.ic_comment)
        }
    }

    override fun getItemCount() = listHabit.size

    class HabitViewHolder(view: View): RecyclerView.ViewHolder(view){
        val count = view.findViewById<TextView>(R.id.model_habitCard_count)
        val image = view.findViewById<ImageView>(R.id.model_habitCard_image)

    }
}