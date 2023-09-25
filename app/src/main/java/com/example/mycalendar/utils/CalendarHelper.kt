package com.example.mycalendar.utils

import com.example.mycalendar.ListFetch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

object CalendarHelper {
    private val list = mutableListOf<MyDate>()
    suspend fun getPagedMonthList(listFetch: ListFetch, currentYear: Int): List<MyDate> {
        when (listFetch) {
            ListFetch.CurrentYear -> {
                list.addAll(getAllMonthsList(Clock.System.todayIn(TimeZone.currentSystemDefault()).year))
            }
            ListFetch.PreviousYear -> {
                val previousYearList = getAllMonthsList(currentYear - 1)
                logd("previousYearList ${previousYearList[11].year}")
                list.addAll(0, list)
            }
            ListFetch.NextYear -> {
                list.addAll(getAllMonthsList(currentYear + 1))
            }
        }
        return list
    }

    suspend fun getAllMonthsList(currentYear: Int): List<MyDate> {
        val list = mutableListOf<MyDate>()
        withContext(Dispatchers.Default) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            (1..12).forEach {
                val localDate = LocalDate(currentYear, it, 1)
                val firstDateOfMonthDay = localDate.dayOfWeek
                val myDate = MyDate(
                    firstDateOfMonthDay.value,
                    localDate.dayOfMonth,
                    localDate.year,
                    localDate.monthNumber,
                    localDate.month.length(isLeap(localDate.year)),
                    today.dayOfMonth
                )
                withContext(Dispatchers.Main) {
                    list.add(myDate)
                }
            }
        }
        return list
    }

    fun getCurrentMonthNumber(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).monthNumber

    fun getTodayDayNumberInMonth(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).dayOfMonth

    fun currentYear(): Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year

    fun isLeap(year: Int): Boolean {
        return if (year % 4 == 0) {
            if (year % 100 == 0) {
                year % 400 == 0
            } else
                true
        } else
            false
    }
}

data class MyDate(
    val firstDayNumber: Int,
    val date: Int,
    val year: Int,
    val month: Int,
    val monthLength: Int,
    val todayDayNumber: Int
)