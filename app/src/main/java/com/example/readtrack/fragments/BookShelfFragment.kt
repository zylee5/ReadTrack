package com.example.readtrack.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import com.example.readtrack.adapters.BookListAdapter
import com.example.readtrack.viewmodels.BookShelfViewModel

class BookShelfFragment : Fragment() {
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_book_shelf, container, false)
        val bookListAdapter = BookListAdapter()

        if (view is RecyclerView) {
            with(view) {
                adapter = bookListAdapter
                addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
        }

        // storedBooks only gets initialized once from dao
        // it has to be observed for any changes later on
        // so that the list adapter can update the recycler view continuously
        bookShelfViewModel.storedBooks.observe(viewLifecycleOwner) { storedBooks ->
            bookListAdapter.submitList(storedBooks)
        }

        return view
    }
}