package com.example.interviewcontent.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.interviewcontent.models.Task

@Database(
    entities = [Task::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DailyTaskDatabase : RoomDatabase() {

    abstract fun getCalenderTaskDao(): DailyTasksDao
}
