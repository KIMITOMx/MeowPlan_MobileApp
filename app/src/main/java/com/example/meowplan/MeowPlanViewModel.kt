package com.example.meowplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MeowPlanViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userId = MutableStateFlow(auth.currentUser?.uid ?: "")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _authMessage = MutableStateFlow("")
    val authMessage: StateFlow<String> = _authMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dataError = MutableStateFlow("")
    val dataError: StateFlow<String> = _dataError.asStateFlow()

    // Data State Flows
    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _classScheduleState = MutableStateFlow(ClassScheduleState())
    val classScheduleState: StateFlow<ClassScheduleState> = _classScheduleState.asStateFlow()

    private val _notifications = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val notifications: StateFlow<List<Map<String, Any>>> = _notifications.asStateFlow()

    init {
        if (auth.currentUser != null) {
            loadAllUserData()
        }
    }

    // --- Authentication Operations ---

    fun registerUser(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authMessage.value = "กำลังสมัครสมาชิก..."
            _isLoading.value = true
            try {
                val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val uid = result.user?.uid ?: throw Exception("ไม่พบ User ID")

                val user = User(
                    uid = uid,
                    email = email.trim(),
                    username = username.trim(),
                    photoUrl = null
                )

                db.collection("users")
                    .document(uid)
                    .set(user)
                    .await()

                _userId.value = uid
                _username.value = user.username
                _userProfile.value = user
                _isLoggedIn.value = true
                _authMessage.value = "สมัครสมาชิกสำเร็จ!"
                loadAllUserData()
            } catch (e: Exception) {
                _authMessage.value = "สมัครสมาชิกไม่สำเร็จ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authMessage.value = "กำลังเข้าสู่ระบบ..."
            _isLoading.value = true
            try {
                val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
                val uid = result.user?.uid ?: throw Exception("ไม่พบ User ID")

                _userId.value = uid
                _isLoggedIn.value = true
                _authMessage.value = "เข้าสู่ระบบสำเร็จ!"
                loadAllUserData()
            } catch (e: Exception) {
                _authMessage.value = "เข้าสู่ระบบไม่สำเร็จ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank()) {
                onResult(false, "กรุณากรอกอีเมล")
                return@launch
            }
            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                onResult(true, "ส่งลิงก์รีเซ็ตรหัสผ่านไปยังอีเมลของคุณแล้ว")
            } catch (e: Exception) {
                onResult(false, "เกิดข้อผิดพลาด: ${e.message}")
            }
        }
    }

    fun logoutUser() {
        auth.signOut()
        _userId.value = ""
        _isLoggedIn.value = false
        _username.value = ""
        _userProfile.value = null
        _authMessage.value = ""
        clearAllUserData()
    }

    private fun clearAllUserData() {
        _subjects.value = emptyList()
        _tasks.value = emptyList()
        _classScheduleState.value = ClassScheduleState()
        _notifications.value = emptyList()
        _dataError.value = ""
    }

    fun loadAllUserData() {
        val uid = auth.currentUser?.uid ?: return
        _userId.value = uid
        viewModelScope.launch {
            _isLoading.value = true
            try {
                loadUserProfileInternal(uid)
                loadSubjectsInternal(uid)
                loadTasksInternal(uid)
                loadClassesInternal(uid)
                loadNotificationsInternal(uid)
            } catch (e: Exception) {
                _dataError.value = "โหลดข้อมูลไม่สำเร็จ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- User Profile ---

    private suspend fun loadUserProfileInternal(uid: String) {
        val doc = db.collection("users").document(uid).get().await()
        if (doc.exists()) {
            val u = doc.toObject(User::class.java)
            _userProfile.value = u
            _username.value = u?.username?.ifBlank { u.email.substringBefore("@") } ?: ""
        } else {
            val defaultUser = User(
                uid = uid,
                email = auth.currentUser?.email ?: "",
                username = auth.currentUser?.email?.substringBefore("@") ?: "Student"
            )
            db.collection("users").document(uid).set(defaultUser).await()
            _userProfile.value = defaultUser
            _username.value = defaultUser.username
        }
    }

    fun updateUserProfile(newUsername: String, photoUrl: String? = null, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val updated = mapOf(
                    "username" to newUsername.trim(),
                    "photoUrl" to photoUrl
                )
                db.collection("users").document(uid).update(updated).await()
                _username.value = newUsername.trim()
                _userProfile.value = _userProfile.value?.copy(username = newUsername.trim(), photoUrl = photoUrl)
                onComplete(true, "อัปเดตโปรไฟล์สำเร็จ")
            } catch (e: Exception) {
                onComplete(false, "เกิดข้อผิดพลาด: ${e.message}")
            }
        }
    }

    // --- Subject Operations ---

    fun loadSubjects() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            loadSubjectsInternal(uid)
        }
    }

    private suspend fun loadSubjectsInternal(uid: String) {
        val snapshot = db.collection("users").document(uid).collection("subjects").get().await()
        val list = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Subject::class.java)?.copy(id = doc.id)
        }
        _subjects.value = list
    }

    fun addSubject(subject: Subject, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val docRef = db.collection("users").document(uid).collection("subjects").document()
                val newSubject = subject.copy(id = docRef.id)
                docRef.set(newSubject).await()
                loadSubjectsInternal(uid)
                onComplete(true, "เพิ่มวิชาเรียบร้อยแล้ว")
            } catch (e: Exception) {
                onComplete(false, "เพิ่มวิชาไม่สำเร็จ: ${e.message}")
            }
        }
    }

    fun updateSubject(subject: Subject, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .collection("subjects").document(subject.id).set(subject).await()
                loadSubjectsInternal(uid)
                onComplete(true, "แก้ไขวิชาเรียบร้อยแล้ว")
            } catch (e: Exception) {
                onComplete(false, "แก้ไขไม่สำเร็จ: ${e.message}")
            }
        }
    }

    fun deleteSubject(subjectId: String, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val isUsedInClasses = _classScheduleState.value.classes.any { it.subjectId == subjectId }
                val isUsedInTasks = _tasks.value.any { it.subjectId == subjectId }

                if (isUsedInClasses || isUsedInTasks) {
                    onComplete(false, "ไม่สามารถลบวิชานี้ได้ เนื่องจากมีตารางเรียนหรือ งาน ที่ใช้วิชานี้อยู่")
                    return@launch
                }

                db.collection("users").document(uid)
                    .collection("subjects").document(subjectId).delete().await()
                loadSubjectsInternal(uid)
                onComplete(true, "ลบวิชาเรียบร้อยแล้ว")
            } catch (e: Exception) {
                onComplete(false, "ลบวิชาไม่สำเร็จ: ${e.message}")
            }
        }
    }

    // --- Task Operations ---

    fun loadTasks() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            loadTasksInternal(uid)
        }
    }

    private suspend fun loadTasksInternal(uid: String) {
        try {
            val snapshot = db.collection("users")
                .document(uid)
                .collection("tasks")
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                val status = doc.getBoolean("isDone")
                    ?: doc.getBoolean("done")
                    ?: false

                val task = doc.toObject(Task::class.java)?.copy(
                    id = doc.id,
                    isDone = status
                ) ?: Task(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    subjectId = doc.getString("subjectId") ?: "",
                    description = doc.getString("description") ?: "",
                    dueAt = doc.getLong("dueAt") ?: 0L,
                    priority = doc.getString("priority") ?: "Medium",
                    reminderDaysBefore = (doc.getLong("reminderDaysBefore") ?: 0L).toInt(),
                    isDone = status,
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
                task
            }

            _tasks.value = list
            android.util.Log.d("MeowPlan", "loadTasksInternal success: loaded ${list.size} tasks for uid=$uid")
        } catch (e: Exception) {
            android.util.Log.e("MeowPlan", "loadTasksInternal failed for uid=$uid", e)
            _dataError.value = "โหลดข้อมูลงานไม่สำเร็จ: ${e.message}"
        }
    }

    fun addTask(task: Task, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val docRef = db.collection("users").document(uid).collection("tasks").document()
                val newTask = task.copy(
                    id = docRef.id,
                    createdAt = System.currentTimeMillis()
                )
                android.util.Log.d("MeowPlan", "addTask: uid=$uid, taskId=${newTask.id}, isDone=${newTask.isDone}")
                docRef.set(newTask).await()
                loadTasksInternal(uid)
                onComplete(true, "เพิ่มงานเรียบร้อยแล้ว")
            } catch (e: Exception) {
                android.util.Log.e("MeowPlan", "addTask failed", e)
                onComplete(false, "เพิ่มงานไม่สำเร็จ: ${e.message}")
            }
        }
    }

    fun updateTask(task: Task, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                android.util.Log.d("MeowPlan", "updateTask: uid=$uid, taskId=${task.id}, isDone=${task.isDone}")
                db.collection("users").document(uid)
                    .collection("tasks").document(task.id).set(task).await()
                loadTasksInternal(uid)
                onComplete(true, "อัปเดตงานเรียบร้อยแล้ว")
            } catch (e: Exception) {
                android.util.Log.e("MeowPlan", "updateTask failed for taskId=${task.id}", e)
                onComplete(false, "อัปเดตงานไม่สำเร็จ: ${e.message}")
            }
        }
    }

    fun toggleTaskDone(task: Task) {
        val latestTask = _tasks.value.firstOrNull { it.id == task.id } ?: task
        setTaskDone(latestTask.id, !latestTask.isDone)
    }

    fun setTaskDone(taskId: String, newValue: Boolean) {
        val uid = auth.currentUser?.uid

        if (uid.isNullOrBlank()) {
            _dataError.value = "ไม่พบผู้ใช้ที่ Login อยู่ กรุณา Login ใหม่"
            android.util.Log.e("MeowPlan", "setTaskDone failed: uid is null")
            return
        }

        if (taskId.isBlank()) {
            _dataError.value = "ไม่พบรหัสงาน"
            android.util.Log.e("MeowPlan", "setTaskDone failed: taskId is blank")
            return
        }

        val previousValue = _tasks.value.firstOrNull { it.id == taskId }?.isDone

        android.util.Log.d(
            "MeowPlan",
            "setTaskDone starting: uid=$uid, taskId=$taskId, newValue=$newValue, previousValue=$previousValue"
        )

        // Optimistically update local state immediately so UI updates instantly
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(isDone = newValue)
            } else {
                task
            }
        }

        val taskRef = db.collection("users")
            .document(uid)
            .collection("tasks")
            .document(taskId)

        viewModelScope.launch {
            try {
                // Write isDone and delete legacy 'done' field to clean up duplicate fields in Firestore
                val updates = mapOf(
                    "isDone" to newValue,
                    "done" to com.google.firebase.firestore.FieldValue.delete()
                )
                taskRef.set(updates, com.google.firebase.firestore.SetOptions.merge()).await()

                _dataError.value = ""

                android.util.Log.d(
                    "MeowPlan",
                    "setTaskDone successful: taskId=$taskId, isDone=$newValue"
                )
            } catch (e: Exception) {
                android.util.Log.e(
                    "MeowPlan",
                    "setTaskDone failed: taskId=$taskId",
                    e
                )

                _dataError.value =
                    "บันทึกสถานะไม่สำเร็จ: ${e.message ?: "ไม่ทราบสาเหตุ"}"

                // Rollback local state if Firestore write fails
                if (previousValue != null) {
                    _tasks.value = _tasks.value.map { task ->
                        if (task.id == taskId) {
                            task.copy(isDone = previousValue)
                        } else {
                            task
                        }
                    }
                }
            }
        }
    }

    fun deleteTask(taskId: String, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                android.util.Log.d("MeowPlan", "deleteTask: uid=$uid, taskId=$taskId")
                db.collection("users").document(uid)
                    .collection("tasks").document(taskId).delete().await()
                loadTasksInternal(uid)
                onComplete(true, "ลบงานเรียบร้อยแล้ว")
            } catch (e: Exception) {
                android.util.Log.e("MeowPlan", "deleteTask failed for taskId=$taskId", e)
                onComplete(false, "ลบงานไม่สำเร็จ: ${e.message}")
            }
        }
    }

    // --- Class Schedule Operations with Conflict Validation ---

    private fun isTimeConflict(
        start1: String, end1: String,
        start2: String, end2: String
    ): Boolean {
        // newStart < existingEnd && newEnd > existingStart
        return start1 < end2 && end1 > start2
    }

    fun loadClasses() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            loadClassesInternal(uid)
        }
    }

    private suspend fun loadClassesInternal(uid: String) {
        _classScheduleState.value = _classScheduleState.value.copy(isLoading = true, message = "")
        try {
            val snapshot = db.collection("users").document(uid).collection("classes").get().await()
            val classes = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ClassSession::class.java)?.copy(id = doc.id)
            }
            _classScheduleState.value = ClassScheduleState(
                classes = classes,
                message = if (classes.isEmpty()) "ยังไม่มีตารางเรียน" else "โหลดตารางเรียนสำเร็จ"
            )
        } catch (e: Exception) {
            _classScheduleState.value = _classScheduleState.value.copy(
                isLoading = false,
                message = "โหลดตารางเรียนไม่สำเร็จ: ${e.message}"
            )
        }
    }

    fun addClassSession(session: ClassSession, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onComplete(false, "กรุณาเข้าสู่ระบบก่อน")
            return
        }

        viewModelScope.launch {
            _classScheduleState.value = _classScheduleState.value.copy(isLoading = true, message = "")
            try {
                val timeRegex = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
                if (!session.startTime.matches(timeRegex) || !session.endTime.matches(timeRegex)) {
                    val err = "รูปแบบเวลาต้องเป็น HH:mm เช่น 09:00"
                    _classScheduleState.value = _classScheduleState.value.copy(isLoading = false, message = err)
                    onComplete(false, err)
                    return@launch
                }
                if (session.startTime >= session.endTime) {
                    val err = "เวลาเริ่มเรียนต้องก่อนเวลาเลิกเรียน"
                    _classScheduleState.value = _classScheduleState.value.copy(isLoading = false, message = err)
                    onComplete(false, err)
                    return@launch
                }

                // Check conflict against fresh Firestore records
                val snapshot = db.collection("users").document(uid).collection("classes").get().await()
                val existingClasses = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ClassSession::class.java)?.copy(id = doc.id)
                }

                val conflictClass = existingClasses.find { existing ->
                    existing.dayOfWeek.equals(session.dayOfWeek, ignoreCase = true) &&
                            isTimeConflict(session.startTime, session.endTime, existing.startTime, existing.endTime)
                }

                if (conflictClass != null) {
                    val errorMsg = "ช่วงเวลานี้มีตารางเรียนอยู่แล้ว (${conflictClass.subjectId} ${conflictClass.startTime}-${conflictClass.endTime}) กรุณาเลือกเวลาใหม่"
                    _classScheduleState.value = _classScheduleState.value.copy(
                        isLoading = false,
                        message = errorMsg
                    )
                    onComplete(false, errorMsg)
                    return@launch
                }

                val docRef = db.collection("users").document(uid).collection("classes").document()
                val newSession = session.copy(id = docRef.id)
                docRef.set(newSession).await()
                loadClassesInternal(uid)
                val successMsg = "บันทึกตารางเรียนสำเร็จ!"
                _classScheduleState.value = _classScheduleState.value.copy(
                    isLoading = false,
                    message = successMsg
                )
                onComplete(true, successMsg)
            } catch (e: Exception) {
                val err = "บันทึกไม่สำเร็จ: ${e.message}"
                _classScheduleState.value = _classScheduleState.value.copy(
                    isLoading = false,
                    message = err
                )
                onComplete(false, err)
            }
        }
    }

    fun updateClassSession(session: ClassSession, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onComplete(false, "กรุณาเข้าสู่ระบบก่อน")
            return
        }

        viewModelScope.launch {
            try {
                val timeRegex = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
                if (!session.startTime.matches(timeRegex) || !session.endTime.matches(timeRegex)) {
                    onComplete(false, "รูปแบบเวลาต้องเป็น HH:mm เช่น 09:00")
                    return@launch
                }
                if (session.startTime >= session.endTime) {
                    onComplete(false, "เวลาเริ่มเรียนต้องก่อนเวลาเลิกเรียน")
                    return@launch
                }

                val snapshot = db.collection("users").document(uid).collection("classes").get().await()
                val existingClasses = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ClassSession::class.java)?.copy(id = doc.id)
                }

                val conflictClass = existingClasses.find { existing ->
                    existing.id != session.id &&
                            existing.dayOfWeek.equals(session.dayOfWeek, ignoreCase = true) &&
                            isTimeConflict(session.startTime, session.endTime, existing.startTime, existing.endTime)
                }

                if (conflictClass != null) {
                    val errorMsg = "ช่วงเวลานี้มีตารางเรียนอยู่แล้ว (${conflictClass.subjectId} ${conflictClass.startTime}-${conflictClass.endTime}) กรุณาเลือกเวลาใหม่"
                    onComplete(false, errorMsg)
                    return@launch
                }

                db.collection("users").document(uid)
                    .collection("classes").document(session.id).set(session).await()
                loadClassesInternal(uid)
                onComplete(true, "แก้ไขตารางเรียนสำเร็จ!")
            } catch (e: Exception) {
                onComplete(false, "แก้ไขไม่สำเร็จ: ${e.message}")
            }
        }
    }

    fun deleteClassSession(classId: String, onComplete: (Boolean, String) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .collection("classes").document(classId).delete().await()
                loadClassesInternal(uid)
                onComplete(true, "ลบตารางเรียนเรียบร้อยแล้ว")
            } catch (e: Exception) {
                onComplete(false, "ลบตารางเรียนไม่สำเร็จ: ${e.message}")
            }
        }
    }

    // --- Notification Operations ---

    fun loadNotifications() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            loadNotificationsInternal(uid)
        }
    }

    private suspend fun loadNotificationsInternal(uid: String) {
        try {
            val snapshot = db.collection("users").document(uid).collection("notifications").get().await()
            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.plus("id" to doc.id)
            }
            _notifications.value = list
        } catch (_: Exception) {
            _notifications.value = emptyList()
        }
    }

    fun markNotificationRead(notificationId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(uid)
                    .collection("notifications").document(notificationId).update("read", true).await()
                loadNotificationsInternal(uid)
            } catch (_: Exception) {}
        }
    }

    fun addNotification(title: String, message: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val data = mapOf(
                    "title" to title,
                    "message" to message,
                    "createdAt" to System.currentTimeMillis(),
                    "read" to false
                )
                db.collection("users").document(uid).collection("notifications").add(data).await()
                loadNotificationsInternal(uid)
            } catch (_: Exception) {}
        }
    }
}
