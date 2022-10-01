package com.example.readtrack.fragments

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*


class AddBookFragment : Fragment() {
    private val addBookViewModel by activityViewModels<AddBookViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAddBookBinding.inflate(inflater, container, false)
        binding.vm = addBookViewModel

        binding.startingDateTextField.setOnClickListener {
            createDatePicker("Starting Date", binding.startingDateTextField)
        }

        binding.finishingDateTextField.setOnClickListener {
            createDatePicker("Finishing Date", binding.finishingDateTextField)
        }

        binding.readChip.setOnCheckedChangeListener { _, _ ->
            binding.startingDateLayout.isVisible = true
            binding.finishingDateLayout.isVisible = true
            binding.ratingText.isVisible = true
            binding.ratingBar.isVisible = true
        }

        binding.readingChip.setOnCheckedChangeListener { _, _ ->
            binding.startingDateLayout.isVisible = true
            binding.finishingDateLayout.visibility = View.GONE
            binding.ratingText.isVisible = true
            binding.ratingBar.isVisible = true
        }

        binding.wtrChip.setOnCheckedChangeListener { _, _ ->
            binding.startingDateLayout.isVisible = false
            binding.finishingDateLayout.isVisible = false
            binding.ratingText.isVisible = false
            binding.ratingBar.isVisible = false
        }

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun createDatePicker(title: String, textField: TextInputEditText) {
        // Initialize date range picker from material design library
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
        val picker = builder.build()

        // Inside this fragment host another child fragment
        picker.show(childFragmentManager, picker.toString())

        // Set the action for when save button is clicked
        picker.addOnPositiveButtonClickListener {
            // it = selected date in Long!
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
            val formattedDate = dateFormatter.format(Date(it))
//                val finishingDate = dateFormatter.format(Date(it.second))
            textField.setText("$formattedDate")
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


