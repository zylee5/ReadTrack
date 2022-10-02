package com.example.readtrack.fragments

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.readtrack.databinding.FragmentAddBookBinding
import com.example.readtrack.types.Status
import com.example.readtrack.viewmodels.AddBookViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "AddBookFragment"

class AddBookFragment : Fragment() {
    private val addBookViewModel by activityViewModels<AddBookViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAddBookBinding.inflate(inflater, container, false)
        binding.vm = addBookViewModel

        binding.startedToFinishedDateTextField.setOnClickListener {
            createDateRangePicker(binding.startedToFinishedDateTextField)
        }

        binding.startedDateTextField.setOnClickListener {
            createDatePicker(binding.startedDateTextField)
        }

//        binding.finishedDateTextField.setOnClickListener {
//            createDatePicker("Finished Date", binding.finishedDateTextField)
//        }

        binding.readChip.setOnCheckedChangeListener { _, _ ->
            addBookViewModel.newBook.value!!.status = Status.READ.string
            binding.startedToFinishedDateLayout.visibility = View.VISIBLE
            binding.startedDateLayout.visibility = View.GONE
            binding.ratingText.isVisible = true
            binding.ratingBar.isVisible = true
        }

        binding.readingChip.setOnCheckedChangeListener { _, _ ->
            addBookViewModel.newBook.value!!.status = Status.READING.string
            binding.startedToFinishedDateLayout.visibility = View.GONE
            binding.startedDateLayout.visibility = View.VISIBLE
            binding.ratingText.isVisible = true
            binding.ratingBar.isVisible = true
        }

        binding.wtrChip.setOnCheckedChangeListener { _, _ ->
            addBookViewModel.newBook.value!!.status = Status.WANT_TO_READ.string
            binding.startedToFinishedDateLayout.visibility = View.GONE
            binding.startedDateLayout.visibility = View.GONE
            binding.ratingText.isVisible = false
            binding.ratingBar.isVisible = false
        }

        binding.lifecycleOwner = viewLifecycleOwner
        Log.d(TAG, addBookViewModel.newBook.value.toString())

        return binding.root
    }

    private fun createDatePicker(textField: TextInputEditText) {
        val title = "Started Date"

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
        picker.show(childFragmentManager, picker.toString())

        // Set the action for when save button is clicked
        picker.addOnPositiveButtonClickListener {
            // it = selected date in Long!
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            // DateTimeFormatter converts LocalTime and not milliseconds
            val localDate: LocalDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()

            val formattedDate = localDate.format(dateFormatter)
            textField.setText(formattedDate)
        }

        // Set the action for when the cancel button is clicked
        picker.addOnNegativeButtonClickListener {
            Toast.makeText(requireContext(), "You have not selected a date", Toast.LENGTH_SHORT).show()
        }

        // Set the action for when the picker is canceled (back button, etc.)
        picker.addOnCancelListener {
            Toast.makeText(requireContext(), "You have not selected a date", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createDateRangePicker(textField: TextInputEditText) {
        val title = "Started Date - Finished Date"

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
        picker.show(childFragmentManager, picker.toString())

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

        // Set the action for when the cancel button is clicked
        picker.addOnNegativeButtonClickListener {
            Toast.makeText(requireContext(), "You have not selected a date range", Toast.LENGTH_SHORT).show()
        }

        // Set the action for when the picker is canceled (back button, etc.)
        picker.addOnCancelListener {
            Toast.makeText(requireContext(), "You have not selected a date range", Toast.LENGTH_SHORT).show()
        }
    }

}

// Drop down menu with an item selected lost other options after fragment transition/rotation
// https://github.com/material-components/material-components-android/issues/1464
// not working
class ExposedDropdownMenu(context: Context, attributeSet: AttributeSet?) :
    MaterialAutoCompleteTextView(context, attributeSet) {

    override fun getFreezesText(): Boolean {
        return false
    }
}


