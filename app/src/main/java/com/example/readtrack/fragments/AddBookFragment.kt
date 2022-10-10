package com.example.readtrack.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.readtrack.R
import com.example.readtrack.databinding.FragmentAddBookBinding
import com.example.readtrack.types.Status
import com.example.readtrack.viewmodels.AddBookViewModel
import com.example.readtrack.viewmodels.BookShelfViewModel
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "AddBookFragment"

class AddBookFragment : Fragment() {
    private val addBookViewModel by activityViewModels<AddBookViewModel>()
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAddBookBinding.inflate(inflater, container, false)
            .apply {
                vm = addBookViewModel

                startedToFinishedDateTextField.setOnClickListener {
                    createDateRangePicker(startedToFinishedDateTextField)
                }

                startedDateTextField.setOnClickListener {
                    createDatePicker(startedDateTextField)
                }

                // Chip once checked cannot be unchecked (app:selectionRequired="true")
                // thus everytime new selection is made make sure warning text does not appear
                statusChipGroup.setOnCheckedStateChangeListener { _, _ ->
                    statusChipWarningText.visibility = View.GONE
                }

                readChip.setOnCheckedChangeListener { _, _ ->
                    addBookViewModel.newBook.value!!.status = Status.READ
                    setViewVisibilityForStatus(this, Status.READ)
                }

                readingChip.setOnCheckedChangeListener { _, _ ->
                    addBookViewModel.newBook.value!!.status = Status.READING
                    setViewVisibilityForStatus(this, Status.READING)
                }

                wtrChip.setOnCheckedChangeListener { _, _ ->
                    addBookViewModel.newBook.value!!.status = Status.WANT_TO_READ
                    setViewVisibilityForStatus(this, Status.WANT_TO_READ)
                }

                bookNameTextField.doOnTextChanged { _, _, _, _ ->
                    setTextLayoutErrorIfEmpty(bookNameLayout)
                }

                bookNameTextField.setOnFocusChangeListener { _, focused ->
                    if (!focused) {
                        bookNameLayout.error = null
                    }
                }

                bookAuthorTextField.doOnTextChanged { _, _, _, _ ->
                    setTextLayoutErrorIfEmpty(bookAuthorLayout)
                }

                bookAuthorTextField.setOnFocusChangeListener { _, focused ->
                    if (!focused) {
                        bookAuthorLayout.error = null
                    }
                }

                bookGenreTextField.doOnTextChanged { _, _, _, _ ->
                    setTextLayoutErrorIfEmpty(bookGenreLayout)
                }

                bookGenreTextField.setOnFocusChangeListener { _, focused ->
                    if (!focused) {
                        bookGenreLayout.error = null
                    }
                }

                startedToFinishedDateTextField.doOnTextChanged { _, _, _, _ ->
                    setTextLayoutErrorIfEmpty(startedToFinishedDateLayout)
                }

                startedDateTextField.doOnTextChanged { _, _, _, _ ->
                    setTextLayoutErrorIfEmpty(startedDateLayout)
                }

                ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                    Log.d(TAG, "Rating has changed to: $rating")
                    ratingBarWarningText.visibility =
                        if (rating <= 0)
                            View.VISIBLE
                        else
                            View.GONE

                    // Two-way data binding on this property does not work as intended, hence only one-way in xml
                    // Issue: default binding adapter:
                    /* https://android.googlesource.com/platform/frameworks/data-binding/+/refs/heads/studio-master-dev/
                    extensions/baseAdapters/src/main/java/androidx/databinding/adapters/ RatingBarBindingAdapter.java
                    */
                    addBookViewModel.newBook.value!!.rating = rating
                }

                addNewBookBtn.setOnClickListener {
                    val newBook = addBookViewModel.newBook.value!!

                    if (isAddBookFormValid(this)) {
                        val newStoredBook = newBook.toStoredBook()

                        Log.d(TAG, "New StoredBook instance: $newStoredBook")
                        // addBook contains database modifying operation
                        // and thus a suspend function
                        // and thus has to be run inside a coroutine block
                        // which at here tied to the lifecycle of AddBookFragment
                        viewLifecycleOwner.lifecycleScope.launch {
                            bookShelfViewModel.addBook(newStoredBook)
                            findNavController().navigate(R.id.bookShelfFragment)
                        }

                    } else {
                        Log.d(TAG, "Form incomplete")
                        showWarningMsgWhenFormInputMissing(this)
                    }
                }

                lifecycleOwner = viewLifecycleOwner
            }

        return binding.root
    }

    private fun setViewVisibilityForStatus(
        binding: FragmentAddBookBinding, status: Status
    ) {
        binding.apply {
            startedToFinishedDateLayout.visibility =
                if (status == Status.READ) View.VISIBLE
                else View.GONE
            startedDateLayout.visibility =
                if (status == Status.READING) View.VISIBLE
                else View.GONE
            ratingText.visibility =
                if (status == Status.WANT_TO_READ) View.GONE
                else View.VISIBLE
            ratingBar.visibility =
                if (status == Status.WANT_TO_READ) View.GONE
                else View.VISIBLE
            // To prevent previously set visible rating bar warning message showing up
            if (status == Status.WANT_TO_READ)
                ratingBarWarningText.visibility = View.GONE
        }
    }

    /***
     * Check if the add book form is valid
     * Criteria:
     * (1) Status must be selected
     * (2) Name, author, genre must be filled regardless of status
     * (3) Correct date text field must be filled based on selected status
     *     READ: date range, READING: single date
     * (4) Rating cannot be zero for none WANT_TO_READ status
     *
     */
    private fun isAddBookFormValid(
        binding: FragmentAddBookBinding
    ): Boolean {

        binding.apply {
            val selectedStatus = statusChipGroup.checkedChipId

            return !isTxtFieldEmpty(bookNameTextField)
                    && !isTxtFieldEmpty(bookAuthorTextField)
                    && !isTxtFieldEmpty(bookGenreTextField)
                    && when(selectedStatus) {
                        // Read
                        R.id.readChip -> {
                            !isTxtFieldEmpty(startedToFinishedDateTextField)
                                    && ratingBar.rating > 0
                        }
                        // Reading
                        R.id.readingChip -> {
                            !isTxtFieldEmpty(startedDateTextField)
                                    && ratingBar.rating > 0
                        }
                        // Want to read
                        R.id.wtrChip -> {
                            true
                        }
                        // Unchecked
                        else -> {
                            false
                        }
                    }
        }
    }

    /***
     * Show warning messages based on non-valid parts of the add book form
     * Criteria:
     * (same as isAddBookFormValid)
     */
    private fun showWarningMsgWhenFormInputMissing(
        binding: FragmentAddBookBinding
    ) {
        binding.apply {
            val selectedStatus = statusChipGroup.checkedChipId

            setTextLayoutErrorIfEmpty(bookNameLayout)
            setTextLayoutErrorIfEmpty(bookAuthorLayout)
            setTextLayoutErrorIfEmpty(bookGenreLayout)

            when (selectedStatus) {
                R.id.readChip -> {
                    setTextLayoutErrorIfEmpty(startedToFinishedDateLayout)
                    setWarningTextVisibleIfNoSelectionFor(this, ratingBar)
                }
                R.id.readingChip -> {
                    setTextLayoutErrorIfEmpty(startedDateLayout)
                    setWarningTextVisibleIfNoSelectionFor(this, ratingBar)
                }
                R.id.wtrChip -> {
                    // nothing
                }
                else -> {
                    setWarningTextVisibleIfNoSelectionFor(this, statusChipGroup)
                }
            }
        }

        /*
        views.filterIsInstance<TextInputLayout>().forEach {
            setTextLayoutErrorIfEmpty(it, it.editText!!.text, getString(R.string.cannot_be_empty))
        }

        // TODO: both blocks of code below are supposed to work for multiple instances of ChipGroup & RatingBar
        // but there is only one warning TextView for a single instance
        if (views.filterIsInstance<ChipGroup>()
                .any { it.checkedChipId == -1 }) { // if no chips in chip group(s) selected
            binding.statusChipWarningText.visibility = View.VISIBLE
        }

        if (views.filterIsInstance<RatingBar>()
                .any { it.rating <= 0
                        && binding.statusChipGroup.checkedChipId != R.id.wtrChip}) { // if any rating bar has value <= 0 for READ & READING status
            binding.ratingBarWarningText.visibility = View.VISIBLE
        }
        */
    }

    /***
     * Set TextInputLayout with error message
     * if its associated TextInputEditText is empty
     */
    private fun setTextLayoutErrorIfEmpty(layout: TextInputLayout) {
        val errorMsg = getString(R.string.cannot_be_empty)
        layout.error =
            if (isTxtFieldEmpty(layout.editText as TextInputEditText))
                errorMsg
            else
                null
    }

    /***
     * Set warning TextView from 'binding' visible
     * when 'view' such as RatingBar and ChipGroup has no values selected
     */
    private fun setWarningTextVisibleIfNoSelectionFor(binding: FragmentAddBookBinding, view: View) {
        binding.apply {
            when (view) {
                is RatingBar -> {
                    if (view.rating <= 0)
                        ratingBarWarningText.visibility = View.VISIBLE
                }
                is ChipGroup -> {
                    if (view.checkedChipId == -1)
                        statusChipWarningText.visibility = View.VISIBLE
                }
            }
        }
    }

    /***
     * Texts of TextInputEditText have to be trimmed
     * before being checked if empty
     */
    private fun isTxtFieldEmpty(
        textField: TextInputEditText
    ): Boolean = textField.text!!.trim().isEmpty()

    private fun createDatePicker(
        textField: TextInputEditText
    ) {
        val title = "Started Date"

        // Only date today or before can be selected
        val noLaterThanTodayConstraint =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

        // Initialize date picker from material design library
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setCalendarConstraints(noLaterThanTodayConstraint.build())

        val picker = builder.build()

        // Inside this fragment host another child fragment
        picker.show(childFragmentManager, picker.toString())

        // Set the action for when save button is clicked
        picker.addOnPositiveButtonClickListener {
            // it = selected date in Long!
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            // DateTimeFormatter converts LocalTime and not milliseconds
            val localDate: LocalDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()

            val formattedDate = localDate.format(dateFormatter)
            textField.setText(formattedDate)
        }

        // Set the action for when the cancel button is clicked
        picker.addOnNegativeButtonClickListener {
            setTextLayoutErrorIfEmpty(textField.parent.parent as TextInputLayout)
        }

        // Set the action for when the picker is canceled (back button, etc.)
        picker.addOnCancelListener {
            setTextLayoutErrorIfEmpty(textField.parent.parent as TextInputLayout)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createDateRangePicker(
        textField: TextInputEditText
    ) {
        val title = "Started Date - Finished Date"

        // Only date today or before can be selected
        val noLaterThanTodayConstraint =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

        // Initialize date range picker from material design library
        val builder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(title)
            .setCalendarConstraints(noLaterThanTodayConstraint.build())

        val picker = builder.build()

        // Inside this fragment host another child fragment
        picker.show(childFragmentManager, picker.toString())

        // Set the action for when save button is clicked
        picker.addOnPositiveButtonClickListener {
            /***
             * it = selected date in Pair<Long!, Long!>
             * it.first = started date
             * it.second = finished date
             */
            val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
            // DateTimeFormatter converts LocalTime and not milliseconds
            val localStartedDate: LocalDate = Instant.ofEpochMilli(it.first).atZone(ZoneId.systemDefault()).toLocalDate()
            val localFinishedDate: LocalDate = Instant.ofEpochMilli(it.second).atZone(ZoneId.systemDefault()).toLocalDate()

            val startedDate = localStartedDate.format(dateFormatter)
            val finishedDate = localFinishedDate.format(dateFormatter)
            textField.setText("$startedDate - $finishedDate")
        }

        // Set the action for when the cancel button is clicked
        picker.addOnNegativeButtonClickListener {
            setTextLayoutErrorIfEmpty(textField.parent.parent as TextInputLayout)
        }

        // Set the action for when the picker is canceled (back button, etc.)
        picker.addOnCancelListener {
            setTextLayoutErrorIfEmpty(textField.parent.parent as TextInputLayout)
        }
    }

}

/*
/***
 * Drop down menu with an item selected lost other options after fragment transition/rotation
 * https://github.com/material-components/material-components-android/issues/1464
 * NOT WORKING
 */
class ExposedDropdownMenu(
    context: Context, attributeSet: AttributeSet?
): MaterialAutoCompleteTextView(context, attributeSet) {
    override fun getFreezesText(): Boolean {
        return false
    }
}
*/

/*
    /***
     * Return all child views of 'viewGroup' in an array list
     * Only highest level views relative to 'viewGroup' are returned
     */
    private fun getAllChildViews(viewGroup: ViewGroup): ArrayList<View> =
        arrayListOf<View>().apply {
            (0 until viewGroup.childCount).forEach {
                this.add(viewGroup.getChildAt(it))
            }
        }
*/