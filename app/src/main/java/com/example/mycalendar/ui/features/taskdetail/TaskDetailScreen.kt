package com.example.mycalendar.ui.features.taskdetail

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycalendar.TaskDetailScreenUiState
import com.example.mycalendar.UiEvent

@Composable
fun TaskDetailScreen(
    taskDetailScreenUiState: TaskDetailScreenUiState,
    navController: NavController,
    modifier: Modifier = Modifier,
    onEvent: (UiEvent) -> Unit
) {
    LaunchedEffect(key1 = taskDetailScreenUiState.shouldNavigateUp) {
        if (taskDetailScreenUiState.shouldNavigateUp) {
            navController.navigateUp()
        }
    }

    val context = LocalContext.current
    LaunchedEffect(key1 = taskDetailScreenUiState.userMessage) {
        if(taskDetailScreenUiState.userMessage.isNotEmpty())
            Toast.makeText(context, taskDetailScreenUiState.userMessage, Toast.LENGTH_SHORT).show()
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = taskDetailScreenUiState.taskMetaData?.task_detail?.title ?: "",
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = taskDetailScreenUiState.taskMetaData?.task_detail?.description ?: "",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = taskDetailScreenUiState.taskMetaData?.task_detail?.created_date ?: "",
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(60.dp))

            Button(
                onClick = {
                    onEvent(UiEvent.DeleteCurrentTask)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Delete Task")
            }
        }


    }

    DisposableEffect(key1 = true) {
        onDispose {
            onEvent(UiEvent.ClearTaskDetailUiState)
        }
    }
}