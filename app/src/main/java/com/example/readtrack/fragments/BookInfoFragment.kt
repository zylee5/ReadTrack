package com.example.readtrack.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.readtrack.R
import com.example.readtrack.databinding.FragmentBookInfoBinding

class BookInfoFragment : Fragment() {
    private lateinit var binding: FragmentBookInfoBinding
    private val args: BookInfoFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookInfoBinding.inflate(inflater, container, false)
        val clickedBook = args.bookFromService
        println(clickedBook.toString())

        return binding.root
    }
}