package com.example.readtrack.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import com.example.readtrack.adapters.BookShelfListAdapter
import com.example.readtrack.adapters.OnStoredBookClickedListener
import com.example.readtrack.databinding.FragmentBookShelfBinding
import com.example.readtrack.dialogs.SortBooksDialog
import com.example.readtrack.types.StoredBook
import com.example.readtrack.util.SwipeGesture
import com.example.readtrack.viewmodels.BookShelfViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.*


private const val TAG = "BookShelfFragment"

class BookShelfFragment : Fragment(), SortBooksDialog.SortBooksDialogListener, OnStoredBookClickedListener {
    private lateinit var binding: FragmentBookShelfBinding
    private lateinit var preferences: SharedPreferences
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()
    private val bookListAdapter = BookShelfListAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        preferences = requireActivity().getSharedPreferences("pref", Context.MODE_PRIVATE)
        binding = FragmentBookShelfBinding.inflate(inflater, container, false)
            .apply {
                with(bookSearchResultList) {
                    adapter = bookListAdapter
                    addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
                    createSwipeAction(requireContext(), this)
                }

                // Open the sort book dialog
                sortOptionBtn.setOnClickListener {
                    val newSortBookFragment = SortBooksDialog()
                    newSortBookFragment.show(childFragmentManager, "sortBookDialog")
                }

                // Search query only when searchView is focused
                // so that when fragment is replaced and then resumed (where searchView is unfocused)
                // no query search will be made
                var shouldSearch = false
                searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
                    // Log.d(TAG, "Focus? $hasFocus")
                    shouldSearch = hasFocus
                }

                // One exception is when clear button of searchView is pressed
                // searchView remains unfocused if it was already unfocused
                // but we still want to trigger search for empty query
                searchView.findViewById<ImageView>(
                    searchView.context.resources
                        .getIdentifier("android:id/search_close_btn", null, null)
                ).setOnClickListener {
                    // Log.d(TAG, "Clear button clicked")
                    shouldSearch = true
                    searchView.setQuery("", false)
                }

                // Filter the list with search view
                searchView.setOnQueryTextListener (
                    object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(text: String?): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(text: String?): Boolean {
                            // Log.d(TAG, "Should search? $shouldSearch")
                            if (!shouldSearch) return false

                            val query = "%$text%"

                            // To set the MutableLiveData to the latest query text
                            bookShelfViewModel.searchByTitleOrAuthor(query)
                            // To scroll the RecyclerView to the top
                            bookSearchResultList.postDelayed( {
                                bookSearchResultList.smoothScrollToPosition(0)
                            }, 100)
                            // Log.d(TAG, "Query text is changed")
                            return true
                        }

                    }

                )
            }

        // storedBooks has to be observed for any changes later on
        // so that the list adapter can update the recycler view continuously
        bookShelfViewModel.storedBooks.observe(viewLifecycleOwner) { storedBooks ->
            // Sort the list according to user settings
            val order = preferences.getInt("Order", R.id.ascending)
            val sortAttribute = preferences.getInt("Attribute", R.id.bookName)
            val comparator = getComparator(order, sortAttribute)
            if (comparator != null) {
                if (sortAttribute == R.id.startedDate || sortAttribute == R.id.finishedDate) {
                    bookShelfViewModel.sortBooks(StoredBook.StatusComparator()) // Sort by status first
                }
                bookShelfViewModel.sortBooks(comparator)
            }
            bookListAdapter.submitList(storedBooks)
        }

        return binding.root
    }
    
    // When the view item in RecyclerView is clicked
    override fun onItemClicked(bookClicked: StoredBook) {
        // Open AddBookFragment as a dialog
        val editBookRecordDialog = AddBookFragment()

        val bookId = bookClicked.bookId
        val args = Bundle()
        args.putLong("Id", bookId)

        editBookRecordDialog.arguments = args
        editBookRecordDialog.show(childFragmentManager, "editBookDialog")
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
                                        // To allow undo delete
                                        Snackbar.make(recyclerView, "'${selectedBook.title}' is deleted", Snackbar.LENGTH_LONG)
                                            .setAction("Undo") {
                                                viewLifecycleOwner.lifecycleScope.launch {
                                                    bookShelfViewModel.addBook(selectedBook)
                                                }
                                            }
                                            .setAnchorView(activity?.findViewById(R.id.bottom_nav))
                                            .show()
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

        // Both order and book attribute are selected
        if (selectedOrder != -1 && selectedAttribute != -1) {
            val comparator = getComparator(selectedOrder, selectedAttribute)

            if (comparator != null) {
                // Sort the list based on the order and attribute selected
                if (selectedAttribute == R.id.startedDate || selectedAttribute == R.id.finishedDate) {
                    bookShelfViewModel.sortBooks(StoredBook.StatusComparator()) // Sort by status first
                }
                bookShelfViewModel.sortBooks(comparator)
                bookListAdapter.notifyDataSetChanged()
                // To scroll the RecyclerView to the top
                binding.apply {
                    bookSearchResultList.postDelayed( {
                        bookSearchResultList.smoothScrollToPosition(0)
                    }, 100)
                }
            }

            // Update user setting
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
