package com.example.readtrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.readtrack.databinding.FragmentAddBookBinding
import com.example.readtrack.viewmodels.AddBookViewModel


class AddBookFragment : Fragment() {
    private val addBookViewModel by activityViewModels<AddBookViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAddBookBinding.inflate(inflater, container, false)
        binding.vm = addBookViewModel

        return binding.root
    }

}
