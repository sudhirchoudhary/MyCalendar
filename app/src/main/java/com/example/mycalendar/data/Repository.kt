package com.example.mycalendar.data

import com.example.mycalendar.data.api.ApiService
import com.example.mycalendar.data.model.CreateTaskRequest
import com.example.mycalendar.data.model.DeleteTaskRequest
import com.example.mycalendar.data.model.GetAllTasksRequest
import com.example.mycalendar.data.model.TaskResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class Repository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllTasks(getAllTasksRequest: GetAllTasksRequest): Flow<Response<TaskResponse>> {
        return apiService.getAllTasks(getAllTasksRequest).mapToResponse()
    }

    suspend fun createTask(createTaskRequest: CreateTaskRequest) = apiService.createTask(createTaskRequest).mapToResponse()
    suspend fun deleteTask(deleteTaskRequest: DeleteTaskRequest) = apiService.deleteTask(deleteTaskRequest).mapToResponse()
}