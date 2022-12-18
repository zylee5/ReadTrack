package com.example.readtrack.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import com.example.readtrack.adapters.BookListAdapter
import com.example.readtrack.databinding.FragmentBookShelfBinding
import com.example.readtrack.util.SwipeGesture
import com.example.readtrack.viewmodels.BookShelfViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class BookShelfFragment : Fragment() {
    private lateinit var binding: FragmentBookShelfBinding
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()
    private val bookListAdapter = BookListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookShelfBinding.inflate(inflater, container, false)
            .apply {
                with(bookList) {
                    adapter = bookListAdapter
                    addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                    createSwipeAction(requireContext(), this)
                }
            }

        // storedBooks only gets initialized once from dao
        // it has to be observed for any changes later on
        // so that the list adapter can update the recycler view continuously
        bookShelfViewModel.storedBooks.observe(viewLifecycleOwner) { storedBooks ->
            bookListAdapter.submitList(storedBooks)
        }

        return binding.root
    }

    private fun createSwipeAction(context: Context, recyclerView: RecyclerView) {
        val simpleCallback =
            object : SwipeGesture(context) {

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.layoutPosition
                    val selectedBook = bookShelfViewModel.storedBooks.value?.get(position)
                    val builder = AlertDialog.Builder(context)

                    when (direction) {
                        ItemTouchHelper.LEFT ->
                            if (selectedBook != null) {
                                builder.setMessage("Are you sure you want to delete this record?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        viewLifecycleOwner.lifecycleScope.launch {
                                            bookShelfViewModel.deleteBook(selectedBook)
                                        }
                                        // to allow undo delete
                                        Snackbar.make(recyclerView, "'${selectedBook.name}' is deleted", Snackbar.LENGTH_LONG)
                                            .setAction("Undo") {
                                                viewLifecycleOwner.lifecycleScope.launch {
                                                    bookShelfViewModel.addBook(selectedBook)
                                                }
                                            }.show()
                                    }
                                    .setNegativeButton("No") { dialog, _ ->
                                        bookListAdapter.notifyItemChanged(position)
                                        dialog.dismiss()
                                    }
                                val alertDialog = builder.create()
                                alertDialog.show()
                            }


                    }
                }

            }
        val touchHelper = ItemTouchHelper(simpleCallback)
        touchHelper.attachToRecyclerView(recyclerView)
    }
}