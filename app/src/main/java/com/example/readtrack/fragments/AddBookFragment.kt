package com.example.readtrack.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.readtrack.R
import com.example.readtrack.databinding.FragmentAddBookBinding
import com.example.readtrack.dialogs.AddBookCoverDialog
import com.example.readtrack.types.Status
import com.example.readtrack.util.ImageUtils
import com.example.readtrack.viewmodels.AddBookViewModel
import com.example.readtrack.viewmodels.BookShelfViewModel
import com.google.android.material.chip.ChipGroup
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "AddBookFragment"

class AddBookFragment : Fragment(), AddBookCoverDialog.AddBookCoverDialogListener {
    private lateinit var binding: FragmentAddBookBinding
    private val addBookViewModel by activityViewModels<AddBookViewModel>()
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBookBinding.inflate(inflater, container, false)
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
                    addBookViewModel.newBook.value?.let {
                        it.status = Status.READ
                    }
                    setViewVisibilityForStatus(Status.READ)
                }

                readingChip.setOnCheckedChangeListener { _, _ ->
                    addBookViewModel.newBook.value?.let {
                        it.status = Status.READING
                    }
                    setViewVisibilityForStatus(Status.READING)
                }

                wtrChip.setOnCheckedChangeListener { _, _ ->
                    addBookViewModel.newBook.value?.let {
                        it.status = Status.WANT_TO_READ
                    }
                    setViewVisibilityForStatus(Status.WANT_TO_READ)
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
                    addBookViewModel.newBook.value?.let {
                        it.rating = rating
                    }
                }

                addBookCoverBtn.setOnClickListener {
                    // open a dialog fragment to add a book cover photo
                    // by either capturing through camera
                    // or choosing from gallery
                    val newDialogFragment = AddBookCoverDialog.getInstance(requireContext())
                    newDialogFragment?.show(childFragmentManager, "addBookCoverDialog")
                }

                addNewBookBtn.setOnClickListener {
                    addBookViewModel.newBook.value?.let {
                        if (isAddBookFormValid()) {
                            val newStoredBook = it.toStoredBook()

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
                            showWarningMsgWhenFormInputMissing()
                        }
                    }
                }

                lifecycleOwner = viewLifecycleOwner
            }

        return binding.root
    }

    override fun onPickOptionClicked() {
        val pickIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launch the pick image activity for result
        // (startActivityForResult has been deprecated)
        pickImgResultLauncher.launch(pickIntent)
    }
    private val pickImgResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() // a generic contract taking any Intent input
    ) { result ->
        // --- ActivityResultCallback (onActivityResult()) ---
        // after returning from the activity
        // handle the activity result
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                addBookViewModel.newBook.value?.let {
                    it.coverUri = uri.toString()
                    it.hasCoverImg.set(true) // even though coverUri has changed, hasCoverImg will not change according to that
                                             // manual updating is required
                    it.hasCoverImg.notifyChange()
                }
            }
        }
    }

    override fun onCaptureOptionClicked() {
        // Create a temporary file and add to the list
        try {
            val img = ImageUtils.createUniqueImgFile(requireContext())
            addBookViewModel.newBook.value?.coverImgFiles?.add(img)
        } catch (e: IOException) {
            // If no file has been created, activity will not launch
            return
        }

        // If the list is not found, return
        addBookViewModel.newBook.value?.coverImgFiles?.let { imgFiles ->
            val lastImg = imgFiles[imgFiles.lastIndex]

            // If the latest added image is not found on the list, return
            lastImg?.let { currentImg ->
                /**
                 * Use a FileProvider to generate the uri for the temp file
                 * The uri is content:// and not file://
                 * content:// allows us to grant temp access to the uri
                 * as opposed to file:// that requires modification of file system permission, which is unsafe
                 */
                val imgUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.readtrack.fileprovider",
                    currentImg
                )
                // Add uri to the created file as an extra to the capture intent
                val captureIntent = Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
                ).apply {
                    putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        imgUri
                    )
                }
                /** Give temporary write permission on the uri
                 * to the camera package
                 */
                // Retrieve all activities that can be performed by captureIntent
                val intentActivitiesList = requireContext().packageManager.queryIntentActivities(
                    captureIntent,
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                intentActivitiesList.map {
                    it.activityInfo.packageName
                }.forEach {
                    requireContext().grantUriPermission(
                        it,
                        imgUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                // Launch the image capture activity
                captureImgResultLauncher.launch(captureIntent)
            }
        }
    }
    /***
     * Problems:
     * 1) Temporary files of 0B that are created whenever the capture image option is selected are not deleted - solved
     * when no images are captured (option selected but quit the camera activity)
     * 2) Images captured that are unused in the end are remained in the directory
     * e.g., user first took an image then later select another from gallery
     * 3) Images captured are in their original size, which is probably not needed
     * 4) Bitmap image set as the button background does not survive configuration change - solved
     */
    private val captureImgResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // If the image list is not found, return
        addBookViewModel.newBook.value?.coverImgFiles?.let { imgFiles ->
            val lastImg = imgFiles[imgFiles.lastIndex]

            // If the latest image is not found on the list, return
            lastImg?.let { currentImg ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Get the image uri
                    val imgUri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.example.readtrack.fileprovider",
                        currentImg
                    )
                    // Update the uri property of newBook instance
                    addBookViewModel.newBook.value?.let {
                        it.coverUri = imgUri.toString()
                        it.hasCoverImg.set(true)
                        it.hasCoverImg.notifyChange()
                    }
                    // Revoke the temporary write permission
                    requireContext().revokeUriPermission(
                        imgUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                    /*
                    // Generate bitmap from currentImg
                    val bitmap = ImageUtils.decodeFileToBitmap(
                        currentImg.absolutePath,
                        resources.getDimensionPixelSize(R.dimen.bk_cover_img_width),
                        resources.getDimensionPixelSize(R.dimen.bk_cover_img_height)
                    )
                    */

                    // Delete all images taken previously (including empty temp files)
                    for (i in imgFiles.size - 2 downTo 0) {
                        val img = imgFiles[i]
                        imgFiles.removeAt(i)
                        img?.delete()
                    }

                } else {
                    // When the camera activity is cancelled
                    // delete the empty temp file
                    imgFiles.remove(currentImg)
                    currentImg.delete()
                }
            }
        }
    }

    private fun setViewVisibilityForStatus(status: Status) {
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
    private fun isAddBookFormValid(): Boolean {
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
    private fun showWarningMsgWhenFormInputMissing() {
        binding.apply {
            val selectedStatus = statusChipGroup.checkedChipId

            setTextLayoutErrorIfEmpty(bookNameLayout)
            setTextLayoutErrorIfEmpty(bookAuthorLayout)
            setTextLayoutErrorIfEmpty(bookGenreLayout)

            when (selectedStatus) {
                R.id.readChip -> {
                    setTextLayoutErrorIfEmpty(startedToFinishedDateLayout)
                    setWarningTextVisibleIfNoSelectionFor(ratingBar)
                }
                R.id.readingChip -> {
                    setTextLayoutErrorIfEmpty(startedDateLayout)
                    setWarningTextVisibleIfNoSelectionFor(ratingBar)
                }
                R.id.wtrChip -> {
                    // nothing
                }
                else -> {
                    setWarningTextVisibleIfNoSelectionFor(statusChipGroup)
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
    private fun setWarningTextVisibleIfNoSelectionFor(view: View) {
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
    ): Boolean = textField.text?.trim()?.isEmpty() ?: false

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