package com.example.readtrack.services

import com.example.readtrack.types.ApiResultStatus
import retrofit2.HttpException
import java.io.IOException

class ApiResult<T>(
    val result: T? = null,
    val status: ApiResultStatus = ApiResultStatus.Unknown
) {
    val success = status == ApiResultStatus.Success
}

// Run api function in this function
// The returned ApiResult.result is the result from the api function
// ApiResult.status can be used to output error message to the user
// A convenient property ApiResult.success is used to determine whether the request is successful
suspend fun <T> safeApiRequest(
    apiRequest: suspend () -> T
): ApiResult<T> {
    return try {
        val result = apiRequest()
        ApiResult(result, ApiResultStatus.Success)
    } catch (ex: HttpException) {
        ApiResult(status = ApiResultStatus.RequestException)
    } catch (ex: IOException) {
        ApiResult(status = ApiResultStatus.NetworkException)
    } catch (ex: Exception) {
        ApiResult(status = ApiResultStatus.GeneralException)
    }
}