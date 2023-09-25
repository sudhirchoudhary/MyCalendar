package com.example.mycalendar.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

sealed class Response<T> {
    data class Loading<T>(val data: T? = null): Response<T>()

    data class Success<T>(val data: T): Response<T>()

    data class Error<T>(val error: String): Response<T>()
}

fun <T> retrofit2.Response<T>.mapToResponse() = flow<Response<T>> {
    if(isSuccessful) {
        this@mapToResponse.body()?.let {
            emit(Response.Success(it))
        } ?: emit(Response.Error("Something went wrong! error: ${message()}"))
    } else {
        Log.d("RequestX", "mapToResponse: ${this@mapToResponse.headers()}")
        emit(Response.Error("error: ${message()}"))
    }
}.flowOn(Dispatchers.IO)