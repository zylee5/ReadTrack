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

class BookShelfListAdapter(
    private val onItemClickedListener: OnStoredBookClickedListener
) : ListAdapter<StoredBook, BookShelfListAdapter.BookShelfListViewHolder>(
    BookShelfListDiffCallback()
) {
    // The ViewHolder is the BookListItem view
    inner class BookShelfListViewHolder(
        private val binding: BookListItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            // binding.root is the whole item view
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                // Prevent deleted item gets clicked before animation completed
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickedListener.onItemClicked(getItem(position))
                }
            }
        }

        // Assign 'item' as the 'book' instance in BookListItem view
        fun bind(item: StoredBook) {
            binding.apply {
                book = item
                executePendingBindings()
            }
        }
    }

    // Initialize a view holder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    : BookShelfListViewHolder =
        BookShelfListViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.book_list_item,
                parent,
                false
            )
        )

    // Get a StoredBook instance from the list at 'position'
    // and assign it as the 'book' instance in each BookListItem view
    override fun onBindViewHolder(holder: BookShelfListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

interface OnStoredBookClickedListener {
    fun onItemClicked(bookClicked: StoredBook)
}

private class BookShelfListDiffCallback: DiffUtil.ItemCallback<StoredBook>() {
    override fun areItemsTheSame(oldItem: StoredBook, newItem: StoredBook)
    : Boolean = oldItem.bookId == newItem.bookId

    override fun areContentsTheSame(oldItem: StoredBook, newItem: StoredBook)
    : Boolean = oldItem == newItem
}