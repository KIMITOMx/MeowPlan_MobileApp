package com.example.meowplan

data class ClassScheduleState(
    val classes: List<ClassSession> = emptyList(),
    val isLoading: Boolean = false,
    val message: String = ""
)