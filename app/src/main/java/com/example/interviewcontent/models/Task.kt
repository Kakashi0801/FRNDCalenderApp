package com.example.interviewcontent.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.interviewcontent.db.Converters

@Entity(tableName = "taskObject")
data class Task(
    @TypeConverters(Converters::class)
    val task_detail: TaskDetail,
    @PrimaryKey()
    val task_id: Int
)