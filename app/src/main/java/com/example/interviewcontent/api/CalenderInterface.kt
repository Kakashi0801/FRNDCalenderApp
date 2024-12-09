package com.example.interviewcontent.api

import com.example.interviewcontent.models.CalenderResponse
import com.example.interviewcontent.models.DeleteTaskRequest
import com.example.interviewcontent.models.StatusResponse
import com.example.interviewcontent.models.Task
import com.example.interviewcontent.models.TaskRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CalenderInterface {

    @POST("getCalendarTaskList")
    suspend fun getCalendarTaskList(
        @Body userId: Map<String, Int>
    ): Response<CalenderResponse>


    @POST("storeCalendarTask")
    suspend fun submitDailyTask(
        @Body request: TaskRequest
    ): Response<StatusResponse>


    @POST("deleteCalendarTask")
    suspend fun deleteDailyTask(
        @Body request: DeleteTaskRequest
    ): Response<StatusResponse>




}