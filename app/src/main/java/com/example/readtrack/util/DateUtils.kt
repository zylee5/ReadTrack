package com.example.readtrack.util

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    fun createDatePicker(
        textField: TextInputEditText,
        title: String,
        fragmentManager: FragmentManager
    ) {
        // Only date today or before can be selected
        val noLaterThanTodayConstraint =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

        // Initialize date picker from material design library
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setCalendarConstraints(noLaterThanTodayConstraint.build())

        val picker = builder.build()

        // Inside this fragment host another child fragment
        picker.show(fragmentManager, picker.toString())

        // Set the action for when save button is clicked
        picker.addOnPositiveButtonClickListener {
            // it = selected date in Long!
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            // DateTimeFormatter converts LocalTime and not milliseconds
            val localDate: LocalDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()

            val formattedDate = localDate.format(dateFormatter)
            textField.setText(formattedDate)
        }
    }

    @SuppressLint("SetTextI18n")
    fun createDateRangePicker(
        textField: TextInputEditText,
        title: String,
        fragmentManager: FragmentManager
    ) {
        // Only date today or before can be selected
        val noLaterThanTodayConstraint =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

        // Initialize date range picker from material design library
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(title)
            .setCalendarConstraints(noLaterThanTodayConstraint.build())

        val picker = builder.build()

        // Inside this fragment host another child fragment
        picker.show(fragmentManager, picker.toString())

        // Set the action for when save button is clicked
        picker.addOnPositiveButtonClickListener {
            /***
             * it = selected date in Pair<Long!, Long!>
             * it.first = started date
             * it.second = finished date
             */
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            // DateTimeFormatter converts LocalTime and not milliseconds
            val localStartedDate: LocalDate = Instant.ofEpochMilli(it.first).atZone(ZoneId.systemDefault()).toLocalDate()
            val localFinishedDate: LocalDate = Instant.ofEpochMilli(it.second).atZone(ZoneId.systemDefault()).toLocalDate()

            val startedDate = localStartedDate.format(dateFormatter)
            val finishedDate = localFinishedDate.format(dateFormatter)
            textField.setText("$startedDate - $finishedDate")
        }
    }
}