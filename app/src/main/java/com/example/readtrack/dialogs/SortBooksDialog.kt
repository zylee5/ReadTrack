package com.example.readtrack.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.example.readtrack.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.ClassCastException

class SortBooksDialog : DialogFragment() {
    interface SortBooksDialogListener {
        fun onPositiveBtnClicked(view: View)
    }

    private lateinit var listener: SortBooksDialogListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as SortBooksDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$parentFragment did not implement SortBooksDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity as Context
        val view = View.inflate(context, R.layout.book_sort_dialog, null)

        return MaterialAlertDialogBuilder(context)
            .setTitle("Sort books by")
            .setView(view)
            .setPositiveButton("Ok") {
                _, _ -> listener.onPositiveBtnClicked(view)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}