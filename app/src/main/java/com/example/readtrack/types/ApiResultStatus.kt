package com.example.readtrack.types

import android.app.Application
import com.example.readtrack.R
import retrofit2.HttpException
import java.io.IOException

enum class ApiResultStatus {
    Success,
    Unknown,
    GeneralException,
    NetworkException,
    RequestException
}

fun ApiResultStatus.getErrorMessage(application: Application): String? =
    when (this) {
        ApiResultStatus.NetworkException -> application.resources.getString(R.string.network_exception_error)
        ApiResultStatus.RequestException -> application.resources.getString(R.string.request_exception_error)
        ApiResultStatus.GeneralException -> application.resources.getString(R.string.general_exception_error)
        else -> null
    }