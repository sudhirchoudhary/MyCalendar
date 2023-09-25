package com.example.mycalendar.ui.features.calendar

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mycalendar.data.model.CreateTaskRequest
import com.example.mycalendar.data.model.TaskDetail
import com.example.mycalendar.CalendarScreenUiState
import com.example.mycalendar.ListFetch
import com.example.mycalendar.SelectedDate
import com.example.mycalendar.UiEvent
import com.example.mycalendar.utils.Constants
import com.example.mycalendar.utils.MyDate
import com.example.mycalendar.utils.logd
import kotlinx.datetime.Month

val dayList = listOf(
    "M",
    "T",
    "W",
    "T",
    "F",
    "S",
    "S"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    calendarScreenUiState: CalendarScreenUiState,
    modifier: Modifier = Modifier,
    onEvent: (UiEvent) -> Unit
) {

    var currentMonth by remember {
        mutableStateOf(calendarScreenUiState.currentMonthNumber)
    }

    var currentYear by remember {
        mutableStateOf(calendarScreenUiState.currentYear)
    }

    val selectedDate by rememberUpdatedState(newValue = calendarScreenUiState.selectedDate)

    LaunchedEffect(key1 = calendarScreenUiState.selectedDate) {
        logd("inside launched ${calendarScreenUiState.selectedDate}")
    }
    val context = LocalContext.current

    LaunchedEffect(key1 = calendarScreenUiState.shouldNavigateUp) {
        if(calendarScreenUiState.shouldNavigateUp)
            navController.navigateUp()
    }

    LaunchedEffect(key1 = calendarScreenUiState.userMessage) {
        if(calendarScreenUiState.userMessage.isNotEmpty())
            Toast.makeText(context, calendarScreenUiState.userMessage, Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(key1 = true) {
        logd("current month number is ${calendarScreenUiState.currentMonthNumber}")
        if (calendarScreenUiState.myDateList.isEmpty())
            onEvent(UiEvent.GetAllMonthsList())
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (calendarScreenUiState.myDateList.isNotEmpty()) {
            val pagerState =
                rememberPagerState(initialPage = (calendarScreenUiState.currentMonthNumber - 1) + (calendarScreenUiState.currentYear - calendarScreenUiState.myDateList[0].year) * 12) {
                    calendarScreenUiState.myDateList.size
                }

            LaunchedEffect(pagerState) {
                logd("current Month is $currentMonth")
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    currentMonth = (page % 12 + 1)
                    currentYear = calendarScreenUiState.myDateList[page].year
                    if (page == pagerState.pageCount - 1) {
                        logd("fetching next year")
                        onEvent(
                            UiEvent.GetAllMonthsList(
                                ListFetch.NextYear,
                                currentYear
                            )
                        )
                    } else if (page == 0) {
                        logd("fetching previous year")
                        onEvent(
                            UiEvent.GetAllMonthsList(
                                ListFetch.PreviousYear,
                                currentYear
                            )
                        )
                    }
                }
            }

            Column(modifier = modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = "${Month(currentMonth)} $currentYear",
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalPager(state = pagerState) {
                    CalendarView(
                        calendarScreenUiState.currentYear,
                        calendarScreenUiState.currentMonthNumber,
                        calendarScreenUiState.myDateList[it],
                        calendarScreenUiState.selectedDate
                    ) {
                        onEvent(
                            UiEvent.OnDateSelected(
                                calendarScreenUiState.currentYear,
                                currentMonth,
                                it
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                AnimatedVisibility(visible = selectedDate.year != 0) {
                    CreateTaskComposable(selectedDate = selectedDate) { _, title, desc ->
                        val selectedDateString =
                            "${calendarScreenUiState.selectedDate.day}-${calendarScreenUiState.selectedDate.month}-${calendarScreenUiState.selectedDate.year}"
                        logd("selctedDateString = $selectedDateString")
                        val taskDetail = TaskDetail(
                            title,
                            desc,
                            selectedDateString
                        )
                        val createTaskRequest = CreateTaskRequest(
                            Constants.USER_ID,
                            taskDetail
                        )
                        onEvent(UiEvent.CreateTask(createTaskRequest))
                    }
                }
            }
        }

        AnimatedVisibility(visible = calendarScreenUiState.isLoading) {
            CircularProgressIndicator()
        }
    }

    DisposableEffect(key1 = true) {
        onDispose {
            onEvent(UiEvent.ClearCalendarScreenUiState)
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    currentYear: Int,
    currentMonthNumber: Int,
    myDate: MyDate,
    selectedDate: SelectedDate,
    onDayClicked: (Int) -> Unit
) {

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 16.dp,
            end = 12.dp,
            bottom = 16.dp
        ),
    ) {
        items(dayList) {
            Text(text = it, textAlign = TextAlign.Center)
        }
        items(myDate.firstDayNumber - 1) {

        }
        items(myDate.monthLength) {
            Card(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (!ifDaySelected(
                            selectedDate,
                            myDate,
                            it + 1
                        )
                    ) {

                        MaterialTheme.colorScheme.surfaceVariant
                    } else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                border = if (myDate.todayDayNumber == it + 1 && myDate.month == currentMonthNumber && myDate.year == currentYear)
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                else BorderStroke(0.dp, Color.Transparent),
                onClick = { onDayClicked(it + 1) }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "${it + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateTaskComposable(
    selectedDate: SelectedDate,
    modifier: Modifier = Modifier,
    onCreateBtnClick: (SelectedDate, String, String) -> Unit
) {
    var titleText by remember {
        mutableStateOf("")
    }
    var descText by remember {
        mutableStateOf("")
    }

    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(text = "Create Task for ${selectedDate.day}")
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = titleText,
            onValueChange = { titleText = it },
            modifier.fillMaxWidth(),
            label = { Text(text = "Title") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = descText,
            onValueChange = { descText = it },
            modifier.fillMaxWidth(),
            label = { Text(text = "Description") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                onCreateBtnClick(selectedDate, titleText, descText)
            },
            modifier.fillMaxWidth()
        ) {
            Text(text = "Create Task")
        }
    }
}

fun ifDaySelected(selectedDate: SelectedDate, myDate: MyDate, currentDay: Int): Boolean {
    if (selectedDate.year != myDate.year)
        return false

    if (selectedDate.month != myDate.month)
        return false

    if (selectedDate.day != currentDay)
        return false

    return true
}