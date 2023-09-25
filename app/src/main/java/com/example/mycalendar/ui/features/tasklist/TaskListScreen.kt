package com.example.mycalendar.ui.features.tasklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycalendar.TaskListUiState
import com.example.mycalendar.UiEvent
import com.example.mycalendar.data.model.TaskDetail
import com.example.mycalendar.ui.ScreenRoute

@Composable
fun TaskListScreen(
    navController: NavController,
    taskListUiState: TaskListUiState,
    modifier: Modifier = Modifier,
    onEvent: (UiEvent) -> Unit
) {
    LaunchedEffect(key1 = true) {
        onEvent(UiEvent.GetAllTasks)
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (taskListUiState.isLoading) {
            CircularProgressIndicator()
        } else {
            if (taskListUiState.taskList.isEmpty())
                Text(text = "No task.\n click on + add one.", textAlign = TextAlign.Center)
            else
                LazyColumn(modifier = modifier.fillMaxSize()) {
                    items(taskListUiState.taskList) {
                        TaskRow(taskDetail = it.task_detail) {
                            onEvent(UiEvent.GetCurrentTaskDetail(it))
                            navController.navigate(ScreenRoute.TASK_DETAIL_SCREEN)
                        }
                    }
                }
        }

        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            IconButton(
                onClick = { navController.navigate(route = ScreenRoute.CALENDAR_SCREEN) },
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 16.dp)
                    .size(60.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun TaskList(taskListUiState: TaskListUiState, modifier: Modifier = Modifier) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRow(
    taskDetail: TaskDetail,
    modifier: Modifier = Modifier,
    onClicked: () -> Unit
) {
    Card(modifier = modifier
        .padding(4.dp)
        .fillMaxWidth(), onClick = { onClicked() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = taskDetail.title, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = taskDetail.created_date, fontSize = 16.sp)
        }
    }
}
