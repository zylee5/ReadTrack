package com.example.readtrack.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.readtrack.R
import com.example.readtrack.databinding.BookSearchListItemBinding
import com.example.readtrack.types.BookFromService
import com.example.readtrack.types.StoredBook

class BookSearchResultListAdapter(
    private val onItemClickedListener: OnBookFromServiceClickedListener
): PagingDataAdapter<BookFromService, BookSearchResultListAdapter.BookSearchResultListViewHolder>(
    BookSearchResultListDiffCallback()
) {
    inner class BookSearchResultListViewHolder(
        private val binding: BookSearchListItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                // Prevent deleted item gets clicked before animation completed
                if (position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let { item -> onItemClickedListener.onItemClicked(item) }
                }
            }
        }

        fun bind(item: BookFromService) {
            binding.apply {
                bookFromService = item
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookSearchResultListViewHolder {
        return BookSearchResultListViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.book_search_list_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BookSearchResultListViewHolder, position: Int) {
        this.getItem(position)?.let { holder.bind(it) }
    }
}

interface OnBookFromServiceClickedListener {
    fun onItemClicked(bookClicked: BookFromService)
}

private class BookSearchResultListDiffCallback: DiffUtil.ItemCallback<BookFromService>() {
    override fun areItemsTheSame(oldItem: BookFromService, newItem: BookFromService): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BookFromService, newItem: BookFromService): Boolean {
        return oldItem == newItem
    }
}