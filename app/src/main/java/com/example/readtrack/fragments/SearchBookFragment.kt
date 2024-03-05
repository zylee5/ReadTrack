package com.example.readtrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.readtrack.R
import com.example.readtrack.adapters.BookSearchResultListAdapter
import com.example.readtrack.adapters.OnBookFromServiceClickedListener
import com.example.readtrack.databinding.FragmentSearchBookBinding
import com.example.readtrack.types.BookFromService
import com.example.readtrack.util.getApiErrorMessage
import com.example.readtrack.viewmodels.SearchBookViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "SearchBookFragment"
class SearchBookFragment : Fragment(), OnBookFromServiceClickedListener {
    private lateinit var binding: FragmentSearchBookBinding
    private val searchBookViewModel by activityViewModels<SearchBookViewModel>()
    private val bookSearchResultListAdapter = BookSearchResultListAdapter(this)
    private var queryTextChangeJob: Job? = null
    private var shouldSearch: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBookBinding.inflate(inflater, container, false)
        binding.apply {
            with(bookSearchResultList) {
                adapter = bookSearchResultListAdapter
            }

            // No focus no search
            searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                shouldSearch = hasFocus
            }

            val id =
                searchView.context.resources.getIdentifier("android:id/search_src_text", null, null)
            val searchText = searchView.findViewById(id) as TextView
            val myCustomFont = ResourcesCompat.getFont(requireContext(), R.font.barlow_semi_bold)
            searchText.typeface = myCustomFont

            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(str: String?): Boolean {
                    return searchBooksByQuery(str, 200)
                }

                override fun onQueryTextChange(str: String?): Boolean {
                    return searchBooksByQuery(str, 500)
                }

            })
        }

        viewLifecycleOwner.lifecycleScope.launch {
            searchBookViewModel.booksFromService.collectLatest {
                bookSearchResultListAdapter.submitData(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bookSearchResultListAdapter.loadStateFlow.collect {

                // Show progress bar when refreshing, appending, and prepending
                binding.refreshProgressBar.isVisible = it.source.refresh is LoadState.Loading
                binding.appendProgressBar.isVisible = it.source.append is LoadState.Loading
                binding.prependProgressBar.isVisible = it.source.prepend is LoadState.Loading

                it.source.apply {
                    // Cast LoadStates load to LoadState.Error
                    val error = when {
                        this.refresh is LoadState.Error -> this.refresh as LoadState.Error
                        this.append is LoadState.Error -> this.append as LoadState.Error
                        this.prepend is LoadState.Error -> this.prepend as LoadState.Error
                        else -> null
                    }
                    // Show a Snackbar with customized error message corresponding to the
                    // type of Throwable instance of LoadState.Error
                    if (error != null) {
                        val errorMsg = error.error.getApiErrorMessage(requireActivity().application)
                        if (!errorMsg.isNullOrEmpty()) {
                            Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG)
                                .setAnchorView(activity?.findViewById(R.id.bottom_nav)).show()
                        }
                    }
                }
            }
        }

        return binding.root
    }

    private fun searchBooksByQuery(str: String?, delayMillis: Long): Boolean {
        // When the query changes, cancel previous job
        queryTextChangeJob?.cancel()
        // Do not search blank string
        if (!shouldSearch || str.isNullOrBlank()) return false

        // Delay several milliseconds before calling api function
        // to prevent overloaded requests
        queryTextChangeJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(delayMillis)
            val query = "$str"
            searchBookViewModel.searchBooksFromService(query)
            binding.bookSearchResultList.smoothScrollToPosition(0)
        }
        return true
    }

    override fun onItemClicked(bookClicked: BookFromService) {
        val action = SearchBookFragmentDirections.actionSearchBookFragmentToBookInfoFragment(bookClicked)
        findNavController().navigate(action)
    }
}