package com.example.meowplan

data class ClassSession(
    val id: String = "",
    val subjectId: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val room: String = "",
    val repeatWeekly: Boolean = true
)