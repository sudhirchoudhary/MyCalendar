package com.example.mycalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mycalendar.data.Repository
import com.example.mycalendar.data.Response
import com.example.mycalendar.data.model.CreateTaskRequest
import com.example.mycalendar.data.model.DeleteTaskRequest
import com.example.mycalendar.data.model.GetAllTasksRequest
import com.example.mycalendar.data.model.TaskMetaData
import com.example.mycalendar.utils.CalendarHelper
import com.example.mycalendar.utils.Constants
import com.example.mycalendar.utils.Constants.USER_ID
import com.example.mycalendar.utils.MyDate
import com.example.mycalendar.utils.logd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private lateinit var job: Job
    private val _taskListUiState = MutableStateFlow(TaskListUiState())
    val taskListUiState: StateFlow<TaskListUiState>
        get() = _taskListUiState.asStateFlow()

    private val _calendarScreenUiState = MutableStateFlow(CalendarScreenUiState(isLoading = true))
    val calendarScreenUiState: StateFlow<CalendarScreenUiState>
        get() = _calendarScreenUiState.asStateFlow()

    private val _currentTaskDetailState = MutableStateFlow(TaskDetailScreenUiState())
    val taskDetailScreenUiState: StateFlow<TaskDetailScreenUiState>
        get() = _currentTaskDetailState.asStateFlow()

    private fun getCalendarList(listFetch: ListFetch = ListFetch.CurrentYear, currentYear: Int = -1) {
        if(this::job.isInitialized)
            job.cancel()
        job = viewModelScope.launch {
            _calendarScreenUiState.value = calendarScreenUiState.value.copy(
                isLoading = false,
                currentMonthNumber = CalendarHelper.getCurrentMonthNumber(),
                currentYear = CalendarHelper.currentYear(),
                todayDayNumber = CalendarHelper.getTodayDayNumberInMonth(),
                myDateList = CalendarHelper.getPagedMonthList(listFetch, currentYear),
                userMessage = ""
            )
            logd("calendarUiState.list.size = ${calendarScreenUiState.value.myDateList.size}")
        }
    }

    private fun getAllTasks() {
        viewModelScope.launch(
            CoroutineExceptionHandler { _, _ ->
                _taskListUiState.value = taskListUiState.value.copy(
                    isLoading = false,
                    taskList = emptyList(),
                    userMessage = ""
                )
            }
        ) {
            _taskListUiState.value = taskListUiState.value.copy(isLoading = true)
            val getAllTasksRequest = GetAllTasksRequest(USER_ID)
            repository.getAllTasks(getAllTasksRequest).collect {
                when (it) {
                    is Response.Success -> {
                        _taskListUiState.value =
                            taskListUiState.value.copy(
                                isLoading = false,
                                taskList = it.data.tasks,
                                userMessage = ""
                            )
                    }

                    is Response.Error -> {
                        _taskListUiState.value =
                            taskListUiState.value.copy(
                                isLoading = false,
                                taskList = emptyList(),
                                userMessage = it.error
                            )
                    }

                    else -> {}
                }
            }
        }
    }

    private fun onDateSelected(year: Int, month: Int, day: Int) {
        _calendarScreenUiState.value = calendarScreenUiState.value.copy(
            selectedDate = SelectedDate(year = year, month = month, day = day).apply {
                logd("$this")
            }
        )
    }

    private fun createTask(createTaskRequest: CreateTaskRequest) {
        viewModelScope.launch(CoroutineExceptionHandler { _, _ ->  }) {
            if(createTaskRequest.validateRequest()) {
                repository.createTask(createTaskRequest).collect {
                    if(it is Response.Success) {
                        logd("Task created successfully")
                        _calendarScreenUiState.value = calendarScreenUiState.value.copy(
                            selectedDate = SelectedDate(),
                            userMessage = "Task created successfully",
                            shouldNavigateUp = true
                        )
                    }
                }

            } else {
                _calendarScreenUiState.value = calendarScreenUiState.value.copy(
                    userMessage = "Please fill all fields"
                )
            }
        }
    }

    private fun getCurrentTaskDetail(taskMetaData: TaskMetaData?) {
        taskMetaData?.let {
            _currentTaskDetailState.value = taskDetailScreenUiState.value.copy(
                isLoading = false,
                taskMetaData = it
            )
        }
    }

    private fun deleteCurrentTask() {
        taskDetailScreenUiState.value.taskMetaData?.let {
            viewModelScope.launch {
                _currentTaskDetailState.value = taskDetailScreenUiState.value.copy(
                    isLoading = true,
                    shouldNavigateUp = false
                )
                val deleteTaskRequest = DeleteTaskRequest(
                    user_id = Constants.USER_ID,
                    task_id = it.task_id
                )
                repository.deleteTask(deleteTaskRequest).collect {
                    when(it) {
                        is Response.Success -> {
                            _currentTaskDetailState.value = taskDetailScreenUiState.value.copy(
                                isLoading = false,
                                userMessage = "Task Deleted",
                                shouldNavigateUp = true
                            )
                        }
                        is Response.Error -> {
                            _currentTaskDetailState.value = taskDetailScreenUiState.value.copy(
                                isLoading = false,
                                userMessage = "Something went wrong",
                                shouldNavigateUp = false
                            )
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun clearCalendarScreenUiState() {
        _calendarScreenUiState.value = CalendarScreenUiState()
    }

    private fun clearTaskDetailScreenUiState() {
        _currentTaskDetailState.value = TaskDetailScreenUiState()
    }



    fun onEvent(uiEvent: UiEvent) {
        when (uiEvent) {
            UiEvent.GetAllTasks -> getAllTasks()
            is UiEvent.GetAllMonthsList -> getCalendarList(uiEvent.listFetch, uiEvent.currentYear)
            is UiEvent.OnDateSelected -> onDateSelected(
                uiEvent.year,
                uiEvent.month,
                uiEvent.day
            )
            is UiEvent.CreateTask -> createTask(uiEvent.createTaskRequest)
            is UiEvent.GetCurrentTaskDetail -> getCurrentTaskDetail(uiEvent.taskMetaData)
            UiEvent.DeleteCurrentTask -> deleteCurrentTask()
            UiEvent.ClearCalendarScreenUiState -> clearCalendarScreenUiState()
            UiEvent.ClearTaskDetailUiState -> clearTaskDetailScreenUiState()
        }
    }
}

sealed interface UiEvent {
    data object GetAllTasks : UiEvent
    data class GetAllMonthsList(
        val listFetch: ListFetch = ListFetch.CurrentYear,
        val currentYear: Int = -1
    ) : UiEvent
    data class OnDateSelected(
        val year: Int,
        val month: Int,
        val day: Int
    ) : UiEvent

    data class CreateTask(
        val createTaskRequest: CreateTaskRequest
    ): UiEvent

    data class GetCurrentTaskDetail(
        val taskMetaData: TaskMetaData?
    ): UiEvent

    data object DeleteCurrentTask: UiEvent

    data object ClearTaskDetailUiState: UiEvent
    data object ClearCalendarScreenUiState: UiEvent
}

data class TaskListUiState(
    val isLoading: Boolean = false,
    val taskList: List<TaskMetaData> = emptyList(),
    val userMessage: String = ""
)

data class TaskDetailScreenUiState(
    val isLoading: Boolean = false,
    val taskMetaData: TaskMetaData? = null,
    val userMessage: String = "",
    val shouldNavigateUp: Boolean = false
)

data class CalendarScreenUiState(
    val isLoading: Boolean = false,
    val currentMonthNumber: Int = 1,
    val currentYear: Int = 0,
    val todayDayNumber: Int = 1,
    val myDateList: List<MyDate> = emptyList(),
    val userMessage: String = "",
    val selectedDate: SelectedDate = SelectedDate(),
    val shouldNavigateUp: Boolean = false
)

data class SelectedDate(
    val year: Int = 0,
    val day: Int = 0,
    val month: Int = 0
)

enum class ListFetch {
    CurrentYear,
    NextYear,
    PreviousYear
}

fun CreateTaskRequest.validateRequest(): Boolean {
    if(task.title.isEmpty() || task.description.isEmpty() || task.created_date.isEmpty())
        return false
    return true
}