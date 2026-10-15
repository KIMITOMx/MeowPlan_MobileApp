package com.example.meowplan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.meowplan.ui.theme.CardWhite
import com.example.meowplan.ui.theme.MeowPlanTheme
import com.example.meowplan.ui.theme.PinkPrimary
import com.example.meowplan.ui.theme.TextDark
import com.example.meowplan.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    private val viewModel: MeowPlanViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            MeowPlanTheme {
                MeowPlanApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MeowPlanApp(viewModel: MeowPlanViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    var currentScreen by remember { mutableStateOf("signIn") }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var selectedTaskId by remember { mutableStateOf<String?>(null) }
    var classToEdit by remember { mutableStateOf<ClassSession?>(null) }
    var subjectToEdit by remember { mutableStateOf<Subject?>(null) }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            viewModel.loadAllUserData()
            currentScreen = "home"
        } else {
            currentScreen = "signIn"
        }
    }

    val navBarScreens = listOf("home", "calendar", "tasks", "classes")
    val showBottomBar = isLoggedIn && currentScreen in navBarScreens

    if (isLoggedIn) {
        BackHandler(enabled = true) {
            when (currentScreen) {
                "calendar", "tasks", "classes", "profile", "notifications" -> currentScreen = "home"
                "addTask", "editTask", "taskDetail" -> currentScreen = "tasks"
                "addClass", "editClass" -> currentScreen = "classes"
                "addSubject", "editSubject", "subjects" -> currentScreen = "home"
                "settings" -> currentScreen = "profile"
                "notificationSettings" -> currentScreen = "settings"
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CardWhite,
                    contentColor = TextDark,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        Triple("calendar", "Calendar", "📅"),
                        Triple("tasks", "Tasks", "📝"),
                        Triple("classes", "Classes", "📚")
                    )

                    navItems.forEach { (screenKey, label, emoji) ->
                        val isSelected = currentScreen == screenKey
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentScreen != screenKey) {
                                    currentScreen = screenKey
                                }
                            },
                            icon = { Text(emoji, fontSize = 20.sp) },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextDark,
                                selectedTextColor = TextDark,
                                indicatorColor = PinkPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoggedIn) {
                when (currentScreen) {
                    "home" -> {
                        HomeScreen(
                            vm = viewModel,
                            onLogoutClick = { viewModel.logoutUser() },
                            onAddTaskClick = { currentScreen = "addTask" },
                            onAddClassClick = { currentScreen = "addClass" },
                            onNavigateToTasks = { currentScreen = "tasks" },
                            onNavigateToClasses = { currentScreen = "classes" },
                            onNavigateToCalendar = { currentScreen = "calendar" },
                            onNavigateToProfile = { currentScreen = "profile" },
                            onNavigateToNotifications = { currentScreen = "notifications" }
                        )
                    }

                    "tasks" -> {
                        TaskListScreen(
                            vm = viewModel,
                            onAddTaskClick = { currentScreen = "addTask" },
                            onTaskClick = { task ->
                                selectedTaskId = task.id
                                currentScreen = "taskDetail"
                            },
                            onBackClick = { currentScreen = "home" }
                        )
                    }

                    "addTask" -> {
                        AddTaskScreen(
                            vm = viewModel,
                            onBackClick = { currentScreen = "tasks" }
                        )
                    }

                    "editTask" -> {
                        taskToEdit?.let { task ->
                            EditTaskScreen(
                                vm = viewModel,
                                taskToEdit = task,
                                onBackClick = { currentScreen = "tasks" }
                            )
                        } ?: run {
                            currentScreen = "tasks"
                        }
                    }

                    "taskDetail" -> {
                        selectedTaskId?.let { id ->
                            TaskDetailScreen(
                                vm = viewModel,
                                taskId = id,
                                onEditClick = { task ->
                                    taskToEdit = task
                                    currentScreen = "editTask"
                                },
                                onBackClick = { currentScreen = "tasks" }
                            )
                        } ?: run {
                            currentScreen = "tasks"
                        }
                    }

                    "classes" -> {
                        ClassListScreen(
                            vm = viewModel,
                            onAddClassClick = { currentScreen = "addClass" },
                            onEditClassClick = { session ->
                                classToEdit = session
                                currentScreen = "editClass"
                            },
                            onBackClick = { currentScreen = "home" }
                        )
                    }

                    "addClass" -> {
                        AddClassScreen(
                            vm = viewModel,
                            onBackClick = { currentScreen = "classes" }
                        )
                    }

                    "editClass" -> {
                        classToEdit?.let { session ->
                            EditClassScreen(
                                vm = viewModel,
                                classSession = session,
                                onBackClick = { currentScreen = "classes" }
                            )
                        } ?: run {
                            currentScreen = "classes"
                        }
                    }

                    "subjects" -> {
                        SubjectListScreen(
                            vm = viewModel,
                            onAddSubjectClick = {
                                subjectToEdit = null
                                currentScreen = "addSubject"
                            },
                            onEditSubjectClick = { subject ->
                                subjectToEdit = subject
                                currentScreen = "editSubject"
                            },
                            onBackClick = { currentScreen = "home" }
                        )
                    }

                    "addSubject" -> {
                        AddEditSubjectScreen(
                            vm = viewModel,
                            subjectToEdit = null,
                            onBackClick = { currentScreen = "subjects" }
                        )
                    }

                    "editSubject" -> {
                        AddEditSubjectScreen(
                            vm = viewModel,
                            subjectToEdit = subjectToEdit,
                            onBackClick = { currentScreen = "subjects" }
                        )
                    }

                    "calendar" -> {
                        CalendarScreen(
                            vm = viewModel,
                            onBackClick = { currentScreen = "home" }
                        )
                    }

                    "profile" -> {
                        ProfileScreen(
                            vm = viewModel,
                            onLogoutClick = { viewModel.logoutUser() },
                            onSettingsClick = { currentScreen = "settings" },
                            onBackClick = { currentScreen = "home" }
                        )
                    }

                    "settings" -> {
                        SettingsScreen(
                            vm = viewModel,
                            onNavigateToNotificationSettings = { currentScreen = "notificationSettings" },
                            onBackClick = { currentScreen = "profile" }
                        )
                    }

                    "notificationSettings" -> {
                        NotificationSettings(
                            onBackClick = { currentScreen = "settings" }
                        )
                    }

                    "notifications" -> {
                        NotificationScreen(
                            vm = viewModel,
                            onBackClick = { currentScreen = "home" }
                        )
                    }

                    else -> {
                        HomeScreen(
                            vm = viewModel,
                            onLogoutClick = { viewModel.logoutUser() },
                            onAddTaskClick = { currentScreen = "addTask" },
                            onAddClassClick = { currentScreen = "addClass" },
                            onNavigateToTasks = { currentScreen = "tasks" },
                            onNavigateToClasses = { currentScreen = "classes" },
                            onNavigateToCalendar = { currentScreen = "calendar" },
                            onNavigateToProfile = { currentScreen = "profile" },
                            onNavigateToNotifications = { currentScreen = "notifications" }
                        )
                    }
                }
            } else {
                when (currentScreen) {
                    "signUp" -> {
                        SignUpScreen(
                            onSignInClick = { currentScreen = "signIn" },
                            vm = viewModel
                        )
                    }

                    "forgotPassword" -> {
                        ForgotPasswordScreen(
                            vm = viewModel,
                            onBackToSignInClick = { currentScreen = "signIn" }
                        )
                    }

                    "emailVerification" -> {
                        EmailVerificationScreen(
                            onContinueClick = { currentScreen = "signIn" }
                        )
                    }

                    else -> {
                        SignInScreen(
                            onSignUpClick = { currentScreen = "signUp" },
                            onForgotPasswordClick = { currentScreen = "forgotPassword" },
                            vm = viewModel
                        )
                    }
                }
            }
        }
    }
}
