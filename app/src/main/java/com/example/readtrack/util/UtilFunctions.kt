package com.example.readtrack.util

import android.view.View
import android.widget.ScrollView
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