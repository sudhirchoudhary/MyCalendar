package com.example.mycalendar.data.api

import com.example.mycalendar.data.model.BaseResponse
import com.example.mycalendar.data.model.CreateTaskRequest
import com.example.mycalendar.data.model.DeleteTaskRequest
import com.example.mycalendar.data.model.GetAllTasksRequest
import com.example.mycalendar.data.model.TaskResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/getCalendarTaskList")
    suspend fun getAllTasks(@Body getAllTasksRequest: GetAllTasksRequest): Response<TaskResponse>

    @POST("api/storeCalendarTask")
    suspend fun createTask(@Body createTaskRequest: CreateTaskRequest) : Response<BaseResponse>

    @POST("api/deleteCalendarTask")
    suspend fun deleteTask(@Body deleteTaskRequest: DeleteTaskRequest): Response<BaseResponse>
}