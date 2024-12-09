package com.example.interviewcontent.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.interviewcontent.R
import com.example.interviewcontent.fragments.TaskBottomSheetDialog
import java.lang.ref.WeakReference

class CustomCalenderViewAdapter(private val days: List<String>, var delegate: WeakReference<OnItemClick>) :
    RecyclerView.Adapter<CustomCalenderViewAdapter.CalendarViewHolder>() {
    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.date_text)
        val cardView:CardView = view.findViewById(R.id.card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_date, parent, false)
        return CalendarViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        holder.dateText.text = day
        holder.cardView.setBackgroundColor(Color.WHITE)
        if (position < 7) {
            holder.dateText.setTextColor(Color.BLUE)
            holder.dateText.setTypeface(null, Typeface.BOLD)
        } else if (day.isEmpty()) {
            holder.dateText.visibility = View.INVISIBLE
        } else {
            holder.dateText.setTextColor(Color.BLACK)
            holder.dateText.setTypeface(null, Typeface.NORMAL)
        }

        if (delegate.get()?.isCurrentDate(day) == true) {
            val gradient = GradientDrawable()
            gradient.setStroke(2, Color.BLUE)
            holder.cardView.background = gradient
        }

        if (delegate.get()?.isSelectedDate(day) == true && position >= 7) {
            holder.cardView.setBackgroundResource(R.color.light_blue)
            holder.cardView.clipToOutline = true
        }

        holder.itemView.setOnClickListener {
            delegate.get()?.didClickItem(holder.dateText.text as String)
            delegate.get()?.selectedDate(holder.dateText.text as String)

        }
    }

    override fun getItemCount(): Int = days.size
}
interface OnItemClick {
    fun didClickItem(date: String)
    fun isSelectedDate(date: String): Boolean
    fun isCurrentDate(date: String): Boolean
    fun selectedDate(date: String)
}