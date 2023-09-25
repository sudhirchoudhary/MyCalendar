package com.example.mycalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mycalendar.data.api.ApiService
import com.example.mycalendar.ui.ScreenRoute
import com.example.mycalendar.ui.features.calendar.CalendarScreen
import com.example.mycalendar.ui.features.taskdetail.TaskDetailScreen
import com.example.mycalendar.ui.features.tasklist.TaskListScreen
import com.example.mycalendar.ui.theme.MyCalendarTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var apiService: ApiService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyCalendarTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val taskViewModel: TaskListViewModel by viewModels()

                    val taskListState = taskViewModel.taskListUiState.collectAsState()
                    val calendarScreenUiState = taskViewModel.calendarScreenUiState.collectAsState()
                    val taskDetailScreenUiState = taskViewModel.taskDetailScreenUiState.collectAsState()

                    NavHost(navController = navController, startDestination = ScreenRoute.TASK_LIST_SCREEN) {
                        composable(route = ScreenRoute.CALENDAR_SCREEN) {
                            CalendarScreen(navController, calendarScreenUiState.value, onEvent = taskViewModel::onEvent)
                        }

                        composable(route = ScreenRoute.TASK_LIST_SCREEN) {
                            TaskListScreen(navController, taskListState.value, onEvent = taskViewModel::onEvent)
                        }

                        composable(route = ScreenRoute.TASK_DETAIL_SCREEN) {
                            TaskDetailScreen(taskDetailScreenUiState.value, navController, onEvent = taskViewModel::onEvent)
                        }
                    }
                }
            }
        }
    }
}