package com.example.readtrack.util

import android.app.Application
import android.content.Context
import android.view.View
import android.widget.ScrollView
import com.example.readtrack.R
import retrofit2.HttpException
import java.io.IOException
import kotlin.reflect.KClass

fun ScrollView.scrollToBottom() {
    val lastChild = getChildAt(childCount - 1)
    val bottom = lastChild.bottom + paddingBottom
    val delta = bottom - (scrollY+ height)
    smoothScrollBy(0, delta)
}

@Suppress("UNCHECKED_CAST")
fun <T: View> findParentViewWithType(currentView: View, type: KClass<T>): T? {
    var parent = currentView.parent
    while (parent != null && parent::class != type) {
        parent = parent.parent
    }
    return parent as? T
}

fun Throwable.getApiErrorMessage(application: Application?): String? =
    when (this) {
        is IOException -> application?.resources?.getString(R.string.network_exception_error)
        is HttpException -> application?.resources?.getString(R.string.request_exception_error)
        is NoSuchElementException -> application?.resources?.getString(R.string.no_result_found)
        is Exception -> application?.resources?.getString(R.string.general_exception_error)
        else -> null
    }