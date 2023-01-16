package com.example.readtrack.adapters

import android.view.View
import android.widget.ImageView
import androidx.databinding.*
import com.bumptech.glide.Glide
import com.example.readtrack.R
import com.example.readtrack.types.Status
import com.google.android.material.chip.ChipGroup

@BindingAdapter("imageUri")
fun loadImage(view: ImageView, uri: String?) {
    if (!uri.isNullOrEmpty()) {
        Glide
            .with(view.context)
            .load(uri)
            .into(view)
    }
}

object StatusBindingAdapters {
    @BindingAdapter("status")
    @JvmStatic
    fun ChipGroup.statusToChip(status: Status?) =
        when (status) {
            Status.READ -> check(R.id.readChip)
            Status.READING -> check(R.id.readingChip)
            Status.WANT_TO_READ -> check(R.id.wtrChip)
            else -> check(View.NO_ID)
        }

    @InverseBindingAdapter(attribute = "status")
    @JvmStatic
    fun ChipGroup.chipToStatus(): Status =
        when (checkedChipId) {
            R.id.readChip -> Status.READ
            R.id.readingChip -> Status.READING
            R.id.wtrChip -> Status.WANT_TO_READ
            else -> Status.NONE
        }

    @BindingAdapter("statusAttrChanged")
    @JvmStatic
    fun ChipGroup.setListeners(attrChange: InverseBindingListener?) =
        setOnCheckedStateChangeListener { _, _ -> attrChange?.onChange() }
}
