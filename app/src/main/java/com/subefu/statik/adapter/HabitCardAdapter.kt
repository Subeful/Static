package com.subefu.statik.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.subefu.statik.R
import com.subefu.statik.model.HabitCard
import com.subefu.statik.screen.GradeActivity
import com.subefu.statik.utils.Constant

class HabitCardAdapter(val context: Context, var listHabit: List<HabitCard>): RecyclerView.Adapter<HabitCardAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.model_card, parent, false)
        return HabitViewHolder(view)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        if(listHabit[position].habit != "comment")
            holder.count.text = listHabit[position].count.toString()

        holder.image.background = when(listHabit[position].habit){
            "water" -> context.getDrawable(R.drawable.ic_water)
            "steps" -> context.getDrawable(R.drawable.ic_steps)
            "words" -> context.getDrawable(R.drawable.ic_words)
            "weather" -> context.getDrawable(R.drawable.ic_weather)
            "mood" -> context.getDrawable(R.drawable.ic_mood)
            "productive" -> context.getDrawable(R.drawable.ic_productivity)
            "sleep" -> context.getDrawable(R.drawable.ic_sleep)
            "sport" -> context.getDrawable(R.drawable.ic_sport)
            "screen time" -> context.getDrawable(R.drawable.ic_screen_time)
            "cost" -> context.getDrawable(R.drawable.ic_cust)
            "comment" -> context.getDrawable(R.drawable.ic_comment)
            else -> null
        }
        holder.itemView.setOnClickListener{
            val intent = Intent(context, GradeActivity::class.java)
            intent.putExtra(Constant.HABIT_NAME, listHabit[position].habit)
            context.startActivity(intent)
        }
    }

    fun updateList(newList: List<HabitCard>){
        listHabit = newList
        notifyDataSetChanged()
    }

    override fun getItemCount() = listHabit.size

    class HabitViewHolder(view: View): RecyclerView.ViewHolder(view){
        val count = view.findViewById<TextView>(R.id.model_habitCard_count)
        val image = view.findViewById<ImageView>(R.id.model_habitCard_image)
    }
}