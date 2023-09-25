package com.example.mycalendar.data.model

data class CreateTaskRequest(
    val user_id: Int,
    val task: TaskDetail
)