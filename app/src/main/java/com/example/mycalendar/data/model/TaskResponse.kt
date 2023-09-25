package com.example.mycalendar.data.model

data class TaskResponse(
    val tasks: List<TaskMetaData>
)

data class TaskMetaData(
    val task_id: Int,
    val task_detail: TaskDetail
)

data class TaskDetail(
    val title: String,
    val description: String,
    val created_date: String
)