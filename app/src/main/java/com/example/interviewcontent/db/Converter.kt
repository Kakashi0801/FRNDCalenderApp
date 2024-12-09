package com.example.interviewcontent.db

import androidx.room.TypeConverter
import com.example.interviewcontent.models.TaskDetail
import com.google.gson.Gson

class Converters {

    @TypeConverter
    fun fromTaskDetail(taskDetail: TaskDetail?): String? {
        return if (taskDetail == null) null else Gson().toJson(taskDetail)
    }

    @TypeConverter
    fun toTaskDetail(taskDetailString: String?): TaskDetail? {
        return if (taskDetailString == null) null else Gson().fromJson(taskDetailString, TaskDetail::class.java)
    }
}