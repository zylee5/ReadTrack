package com.example.readtrack.dialogs

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.get
import androidx.fragment.app.DialogFragment
import com.example.readtrack.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.ClassCastException

class SortBooksDialog : DialogFragment() {
    interface SortBooksDialogListener {
        fun onPositiveBtnClicked(view: View)
    }

    private lateinit var listener: SortBooksDialogListener
    private lateinit var preferences: SharedPreferences
    private lateinit var sortDialogView: View

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

        sortDialogView = View.inflate(context, R.layout.book_sort_dialog, null)
        sortDialogView.apply {
            val orderRadioGrp = findViewById<RadioGroup>(R.id.orderRadioGrp)
            val attrRadioGrp = findViewById<RadioGroup>(R.id.attrRadioGrp)

            // check the radio button according to user settings
            preferences = context.getSharedPreferences("pref", Context.MODE_PRIVATE)
            val order = preferences.getInt("Order", R.id.ascending)
            val sortAttribute = preferences.getInt("Attribute", R.id.bookName)

            orderRadioGrp.check(order)
            attrRadioGrp.check(sortAttribute)
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle("Sort books by")
            .setView(sortDialogView)
            .setPositiveButton("Ok") {
                _, _ -> listener.onPositiveBtnClicked(sortDialogView)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}