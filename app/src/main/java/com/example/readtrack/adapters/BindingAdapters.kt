package com.example.readtrack.adapters

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.readtrack.R

@BindingAdapter("imageUri")
fun loadImage(view: ImageView, uri: String?) {
    if (!uri.isNullOrEmpty()) {
        Glide
            .with(view.context)
            .load(uri)
            .into(view)
    }
}