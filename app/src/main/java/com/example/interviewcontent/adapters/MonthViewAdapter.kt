package com.example.interviewcontent.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.interviewcontent.R
import com.example.interviewcontent.models.Task
import com.example.interviewcontent.util.NetworkUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MonthViewAdapter(
    private val taskList: List<Task>,
    var delegate: WeakReference<onTaskDelete>,
    private val context: Context?,
    private val isToDelete: Boolean
):
    RecyclerView.Adapter<MonthViewAdapter.MonthViewHolder>(){


    private val backgroundColors = listOf(
        Color.parseColor("#FFEBEE"), // Light Pink
        Color.parseColor("#E8F5E9"), // Light Green
        Color.parseColor("#E3F2FD"), // Light Blue
        Color.parseColor("#FFFDE7")  // Light Yellow
    )

    inner class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskName: TextView = view.findViewById(R.id.task_name)
        val taskDescription: TextView = view.findViewById(R.id.task_description)
        val redLine : View = view.findViewById(R.id.red_line)
        val rootLayout: View = view // Reference to the root layout of the item
        val deleteButton : ImageButton = view.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_layout_item, parent, false)
        return MonthViewHolder(view)    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val task = taskList[position]

        holder.taskName.text = task.task_detail.title
        holder.taskDescription.text = task.task_detail.description
        holder.rootLayout.setBackgroundColor(backgroundColors[position % backgroundColors.size])

        if(!isToDelete){
            holder.deleteButton.isVisible = false
        }

        holder.taskName.setOnClickListener {
            if (holder.taskDescription.visibility == View.GONE) {
                holder.taskDescription.visibility = View.VISIBLE
                val descriptionHeight = holder.taskDescription.height
                holder.redLine.layoutParams.height = descriptionHeight
            } else {
                holder.taskDescription.visibility = View.GONE
                holder.redLine.layoutParams.height = 0
            }
            holder.redLine.requestLayout()
        }

        holder.deleteButton.setOnClickListener {
            val task = taskList.get(position)
            delegate.get()?.onTaskDelete(task)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, taskList.size)
        }
    }

    private fun validateDateAndExtractDay(date: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        return try {
            val parsedDate = dateFormat.parse(date)
            val calendar = Calendar.getInstance()
            calendar.time = parsedDate
            calendar.get(Calendar.DAY_OF_MONTH).toString()
        }catch (e: Exception){
            return ""
        }
    }
}

interface onTaskDelete {
    fun onTaskDelete(task: Task)
}
