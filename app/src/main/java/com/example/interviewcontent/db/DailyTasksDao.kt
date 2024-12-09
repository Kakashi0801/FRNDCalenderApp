package com.example.interviewcontent.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.interviewcontent.models.Task

@Dao
interface DailyTasksDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun upsertTask( article: Task):Long

    @Query("SELECT * FROM taskobject")
    fun getAllTasks(): LiveData<List<Task>>

    @Delete
    suspend fun deleteTask(task: Task)
}