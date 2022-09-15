package com.example.readtrack.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.readtrack.databinding.FragmentAddBookBinding
import com.example.readtrack.viewmodels.AddBookViewModel
import com.google.android.material.datepicker.MaterialDatePicker
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

        binding.pickDateButton.setOnClickListener {
            // Initialize date range picker from material design library
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            val picker = builder.build()

            // Inside this fragment host another child fragment
            picker.show(childFragmentManager, picker.toString())

            // Set the action for when save button is clicked
            picker.addOnPositiveButtonClickListener {
                // it = selected date range in Pair<Long!, Long!>
                // it.first = start date
                // it.second = finishing date
                val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
                val startDate = dateFormatter.format(Date(it.first))
                val finishingDate = dateFormatter.format(Date(it.second))
                Toast.makeText(requireContext(), "Start date: ${startDate} \n Finishing date: ${finishingDate}", Toast.LENGTH_LONG).show()
            }

            // Set the action for when the cancel button is clicked
            picker.addOnNegativeButtonClickListener {
                Toast.makeText(requireContext(), "You have not selected a date range", Toast.LENGTH_LONG).show()
            }

            // Set the action for when the picker is canceled (back button, etc.)
            picker.addOnCancelListener {
                Toast.makeText(requireContext(), "You have not selected a date range", Toast.LENGTH_LONG).show()
            }

        }

        return binding.root
    }

}
