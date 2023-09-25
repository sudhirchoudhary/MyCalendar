package com.example.mycalendar.data.model

data class DeleteTaskRequest(
    val user_id: Int,
    val task_id: Int
)