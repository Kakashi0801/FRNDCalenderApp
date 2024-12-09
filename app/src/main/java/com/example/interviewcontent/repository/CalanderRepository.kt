package com.example.interviewcontent.repository

import com.example.interviewcontent.api.CalenderInterface
import com.example.interviewcontent.api.RetrofitInstance
import com.example.interviewcontent.db.DailyTaskDatabase
import com.example.interviewcontent.models.CalenderResponse
import com.example.interviewcontent.models.DeleteTaskRequest
import com.example.interviewcontent.models.StatusResponse
import com.example.interviewcontent.models.Task
import com.example.interviewcontent.models.TaskDetail
import com.example.interviewcontent.models.TaskRequest
import retrofit2.Response
import javax.inject.Inject

class CalanderRepository @Inject constructor(
    private val db: DailyTaskDatabase  ,
    private val calenderInterface: CalenderInterface
) {

    suspend fun fetchCalendarTaskList(userId: Int): Response<CalenderResponse> {
        val requestBodyUserId = mapOf("user_id" to userId)

        val response = RetrofitInstance.api.getCalendarTaskList(requestBodyUserId)
        if (response.isSuccessful) {
            val calendarResponse = response.body()
        } else {
            println("API Error: ${response.errorBody()?.string()}")
        }
        return response
    }

    suspend fun submitDailyTask(userId: Int, taskObj: TaskDetail): Response<StatusResponse> {
        val request = TaskRequest(user_id = userId, task = taskObj)
        val response = RetrofitInstance.api.submitDailyTask(request)
        if (response.isSuccessful) {
            val taskResponse = response.body()
            println("API Response: $taskResponse")
        } else {
            println("API Error: ${response.errorBody()?.string()}")
        }
        return response
    }

    suspend fun deleteDailyTask(userId: Int, taskId: Int): Response<StatusResponse> {
        val request = DeleteTaskRequest(user_id = userId, task_id = taskId)
        val response = RetrofitInstance.api.deleteDailyTask(request)
        if (response.isSuccessful) {
            val taskResponse = response.body()
            println("API Response: $taskResponse")
        } else {
            println("API Error: ${response.errorBody()?.string()}")
        }
        return response
    }

    suspend fun upsertTaskFromDb(task: Task) =
        db.getCalenderTaskDao().upsertTask(task)

    suspend fun deleteTaskFromDb(task: Task) =
        db.getCalenderTaskDao().deleteTask(task)

    fun getSavedArticlesFromDb() = db.getCalenderTaskDao().getAllTasks()
}
