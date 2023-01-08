package com.example.readtrack.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import com.example.readtrack.databinding.BookListItemBinding
import com.example.readtrack.types.StoredBook

class BookListAdapter : ListAdapter<StoredBook, BookListAdapter.BookListViewHolder>(
    BookListDiffCallback()
) {
    inner class BookListViewHolder(
        private val binding: BookListItemBinding
    ): RecyclerView.ViewHolder(binding.root) {

        // Assign 'item' as the 'book' instance in BookListItem view
        fun bind(item: StoredBook) {
            binding.apply {
                book = item
            }
        }
    }

    // Initialize a view holder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    : BookListViewHolder =
        BookListViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.book_list_item,
                parent,
                false
            )
        )

    // Get a StoredBook instance from the list at 'position'
    // and assign it as the 'book' instance in each BookListItem view
    override fun onBindViewHolder(holder: BookListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

private class BookListDiffCallback: DiffUtil.ItemCallback<StoredBook>() {
    override fun areItemsTheSame(oldItem: StoredBook, newItem: StoredBook)
    : Boolean = oldItem.bookId == newItem.bookId

    override fun areContentsTheSame(oldItem: StoredBook, newItem: StoredBook)
    : Boolean = oldItem == newItem
}