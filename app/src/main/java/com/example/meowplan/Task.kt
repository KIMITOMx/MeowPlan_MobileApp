
package com.example.meowplan

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

@IgnoreExtraProperties
data class Task(
    var id: String = "",
    var title: String = "",
    var subjectId: String = "",
    var description: String = "",
    var dueAt: Long = 0L,
    var priority: String = "Medium",
    var reminderDaysBefore: Int = 0,
    @get:PropertyName("isDone")
    @set:PropertyName("isDone")
    var isDone: Boolean = false,
    var createdAt: Long = 0L
)
