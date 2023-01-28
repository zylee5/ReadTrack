package com.example.readtrack.adapters

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bumptech.glide.Glide
import com.example.readtrack.R
import com.example.readtrack.types.Status
import com.example.readtrack.util.findParentViewWithType
import com.example.readtrack.util.scrollToBottom
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*
import kotlin.concurrent.schedule
import kotlin.reflect.KClass


@BindingAdapter("imageUri")
fun loadImage(view: ImageView, uri: String?) {
    if (!uri.isNullOrEmpty()) {
        Glide
            .with(view.context)
            .load(uri)
            .into(view)
    } else {
        view.setImageResource(R.drawable.default_book_cover)
    }
}

@BindingAdapter("error")
fun setError(tInputLayout: TextInputLayout, errorMsg: String) {
    if (errorMsg.isNotEmpty()) {
        tInputLayout.error = errorMsg
    } else {
        tInputLayout.error = null
    }
}

@BindingAdapter("validation")
fun setValidation(tInputLayout: TextInputLayout, str: String?) {
    if (str?.trim()?.isEmpty() == true && tInputLayout.editText?.isFocused == true) {
        tInputLayout.error = "You must fill in this field"
    } else {
        tInputLayout.error = null
    }
}

@BindingAdapter("status")
fun ChipGroup.statusToChip(status: Status?) =
    when (status) {
        Status.READ -> check(R.id.readChip)
        Status.READING -> check(R.id.readingChip)
        Status.WANT_TO_READ -> check(R.id.wtrChip)
        Status.NONE -> clearCheck()
        else -> check(View.NO_ID)
    }

@InverseBindingAdapter(attribute = "status")
fun ChipGroup.chipToStatus(): Status =
    when (checkedChipId) {
        R.id.readChip -> Status.READ
        R.id.readingChip -> Status.READING
        R.id.wtrChip -> Status.WANT_TO_READ
        else -> Status.NONE
    }

@BindingAdapter("statusAttrChanged")
fun ChipGroup.setListeners(attrChange: InverseBindingListener?) =
    setOnCheckedStateChangeListener { _, _ ->
        attrChange?.onChange()
        // Scroll to bottom of scrollView
        val scrollView = findParentViewWithType(this, ScrollView::class)
        if (scrollView != null) {
            Timer().schedule(100) {
                scrollView.scrollToBottom()
            }
        }
    }