package com.example.readtrack.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.readtrack.R
import com.example.readtrack.databinding.FragmentBookInfoBinding
import com.example.readtrack.util.DateUtils
import com.example.readtrack.viewmodels.BookInfoViewModel
import com.example.readtrack.viewmodels.BookShelfViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class BookInfoFragment : Fragment() {
    private lateinit var binding: FragmentBookInfoBinding
    private val bookInfoViewModel by activityViewModels<BookInfoViewModel>()
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()
    private val args: BookInfoFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookInfoBinding.inflate(inflater, container, false)
        val clickedBook = args.bookFromService

        binding.apply {
            vm = bookInfoViewModel
            lifecycleOwner = viewLifecycleOwner
            bookInfoViewModel.bookFromService.value = clickedBook

            // Check if the book was previously added in the local db
            viewLifecycleOwner.lifecycleScope.launch {
                val storedBook = bookShelfViewModel.getBookByIdFromService(clickedBook.id)
                if (storedBook != null) {
                    clickedBook.updateBookWithStatus(storedBook)
                    bookInfoViewModel.isBookLocallyStored.value = true
                } else {
                    bookInfoViewModel.isBookLocallyStored.value = false
                }
            }

            addToBookShelfBtn.setOnClickListener {
                val isBtnClicked = bookInfoViewModel.isAddToShelfBtnClicked.value
                bookInfoViewModel.isAddToShelfBtnClicked.value = isBtnClicked?.let { !it }
            }

            startedToFinishedDateTextField.setOnClickListener {
                DateUtils.createDateRangePicker(startedToFinishedDateTextField, "Started Date - Finished Date", childFragmentManager)
            }

            startedDateTextField.setOnClickListener {
                DateUtils.createDatePicker(startedDateTextField, "Started Date", childFragmentManager)
            }

            confirmAddBtn.setOnClickListener {
                val bookToStore = bookInfoViewModel.bookFromService.value
                viewLifecycleOwner.lifecycleScope.launch {
                    bookToStore?.toStoredBook()?.let {
                        val result = bookShelfViewModel.addBook(it)
                        if (result != -1L) { // Success
                            Snackbar.make(binding.root, "Added to bookshelf", Snackbar.LENGTH_SHORT)
                                .setAnchorView(activity?.findViewById(R.id.bottom_nav)).show()
                            bookInfoViewModel.isBookLocallyStored.value = true
                            bookInfoViewModel.isAddToShelfBtnClicked.value = false
                        } else {
                            Snackbar.make(binding.root, "Something went wrong when adding to bookshelf. Please try again.", Snackbar.LENGTH_SHORT)
                                .setAnchorView(activity?.findViewById(R.id.bottom_nav)).show()
                        }
                    }
                }
            }
        }

        return binding.root
    }
}