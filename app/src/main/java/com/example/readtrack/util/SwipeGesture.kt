package com.example.readtrack.util

import android.content.Context
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

abstract class SwipeGesture(context: Context) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    private val deleteColor = ContextCompat.getColor(context, com.google.android.material.R.color.m3_sys_color_light_error)
    private val whiteTint = ContextCompat.getColor(context, com.google.android.material.R.color.m3_ref_palette_white)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
            .addSwipeLeftLabel("Delete")
            .addSwipeLeftBackgroundColor(deleteColor)
            .setSwipeLeftActionIconTint(whiteTint)
            .setSwipeLeftLabelColor(whiteTint)
            .create()
            .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}