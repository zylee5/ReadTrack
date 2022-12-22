package com.example.readtrack.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import com.example.readtrack.adapters.BookListAdapter
import com.example.readtrack.databinding.FragmentBookShelfBinding
import com.example.readtrack.dialogs.SortBooksDialog
import com.example.readtrack.types.StoredBook
import com.example.readtrack.util.SwipeGesture
import com.example.readtrack.viewmodels.BookShelfViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kotlin.Comparator

class BookShelfFragment : Fragment(), SortBooksDialog.SortBooksDialogListener {
    private lateinit var binding: FragmentBookShelfBinding
    private lateinit var preferences: SharedPreferences
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()
    private val bookListAdapter = BookListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        preferences = requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)

        binding = FragmentBookShelfBinding.inflate(inflater, container, false)
            .apply {
                with(bookList) {
                    adapter = bookListAdapter
                    addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                    createSwipeAction(requireContext(), this)
                }

                // open the sort book dialog
                sortOptionBtn.setOnClickListener {
                    val newSortBookFragment = SortBooksDialog()
                    newSortBookFragment.show(childFragmentManager, "sortBookDialog")
                }
            }

        // storedBooks only gets initialized once from dao
        // it has to be observed for any changes later on
        // so that the list adapter can update the recycler view continuously
        bookShelfViewModel.storedBooks.observe(viewLifecycleOwner) { storedBooks ->

            // sort the list according to user settings
            val order = preferences.getInt("Order", R.id.ascending)
            val sortAttribute = preferences.getInt("Attribute", R.id.bookName)
            val comparator = getComparator(order, sortAttribute)
            if (comparator != null) {
                Log.d("Bookshelf", "Order: ${resources.getResourceEntryName(order)}, Attribute: ${resources.getResourceEntryName(sortAttribute)}")
                bookShelfViewModel.sortBooks(comparator)
            }
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onPositiveBtnClicked(view: View) {
        val orderRadioGrp = view.findViewById<RadioGroup>(R.id.orderRadioGrp)
        val attrRadioGrp = view.findViewById<RadioGroup>(R.id.attrRadioGrp)

        val selectedOrder = orderRadioGrp.checkedRadioButtonId
        val selectedAttribute = attrRadioGrp.checkedRadioButtonId

        // both order and book attribute are selected
        if (selectedOrder != -1 && selectedAttribute != -1) {
            val comparator = getComparator(selectedOrder, selectedAttribute)

            if (comparator != null) {
                bookShelfViewModel.sortBooks(comparator)
                bookListAdapter.notifyDataSetChanged()
            }

            // update user setting
            val editor = preferences.edit()
            editor.apply {
                putInt("Order", selectedOrder)
                putInt("Attribute", selectedAttribute)
                apply()
            }
        }
    }

    private fun getComparator(
        order: Int, attribute: Int
    ): Comparator<StoredBook>? {
        var comparator: Comparator<StoredBook>? = when (attribute) {
            R.id.bookName -> StoredBook.BookNameComparator()
            R.id.authorName -> StoredBook.AuthorNameComparator()
            R.id.genre -> StoredBook.GenreComparator()
            R.id.startedDate -> StoredBook.StartDateComparator()
            R.id.finishedDate -> StoredBook.FinishedDateComparator()
            R.id.status -> StoredBook.StatusComparator()
            R.id.rating -> StoredBook.RatingComparator()
            else -> null
        }

        if (order == R.id.descending && comparator != null) {
            comparator = comparator.reversed()
        }

        return comparator
    }
}