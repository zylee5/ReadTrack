package com.example.readtrack.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.readtrack.R
import com.example.readtrack.databinding.BookRecordLayoutBinding
import com.example.readtrack.databinding.DialogEditBookRecordBinding
import com.example.readtrack.databinding.FragmentAddBookBinding
import com.example.readtrack.dialogs.AddBookCoverDialog
import com.example.readtrack.types.NewBook
import com.example.readtrack.types.Status
import com.example.readtrack.util.ImageUtils
import com.example.readtrack.util.findParentViewWithType
import com.example.readtrack.viewmodels.AddBookViewModel
import com.example.readtrack.viewmodels.BookShelfViewModel
import com.google.android.material.chip.Chip
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
import java.util.*


private const val TAG = "AddBookFragment"

class AddBookFragment : DialogFragment(), AddBookCoverDialog.AddBookCoverDialogListener {
    private var bookId: Long? = null
    private lateinit var liveDataObserved: MutableLiveData<NewBook>
    private lateinit var bookRecordLayoutBinding: BookRecordLayoutBinding
    private lateinit var addBookBinding: FragmentAddBookBinding
    private lateinit var editBookRecordBinding: DialogEditBookRecordBinding
    private val addBookViewModel by activityViewModels<AddBookViewModel>()
    private val bookShelfViewModel by activityViewModels<BookShelfViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myArgs = arguments
        if (myArgs != null) {
            bookId = myArgs.getLong("Id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Add book
        if (bookId == null) {
            addBookBinding = FragmentAddBookBinding.inflate(inflater, container, false)
            liveDataObserved = addBookViewModel.newBook
            addBookBinding
                .apply {
                    vm = addBookViewModel
                    lifecycleOwner = viewLifecycleOwner
                    addNewBookBtn.setOnClickListener {
                        liveDataObserved.value?.let { newBook ->
                            val newStoredBook = newBook.toStoredBook()

                            Log.d(TAG, "New StoredBook instance: $newStoredBook")
                            // addBook contains database modifying operation
                            // and thus a suspend function
                            // and thus has to be run inside a coroutine block
                            // which at here tied to the lifecycle of AddBookFragment
                            viewLifecycleOwner.lifecycleScope.launch {
                                bookShelfViewModel.addBook(newStoredBook)
                                findNavController().navigate(R.id.action_addBookFragment_to_bookShelfFragment)
                                // To clear the input in the view
                                liveDataObserved.value = NewBook()
                            }
                        }
                    }
                }
            bookRecordLayoutBinding = addBookBinding.bookRecordLayout
//            addBookViewModel.isAddBookFormValid.observe(viewLifecycleOwner) {
//                Log.d(TAG, "isAddBookFormValid mediator observed changes - value: $it")
//            }
        }
        // Edit book
        else {
            editBookRecordBinding = DialogEditBookRecordBinding.inflate(inflater, container, false)
            liveDataObserved = addBookViewModel.editBook
            editBookRecordBinding
                .apply {
                    vm = addBookViewModel
                    lifecycleOwner = viewLifecycleOwner
                    bookId?.let{
                        // Get StoredBook with bookId from db, convert it into NewBook
                        // and assign it to the value of liveDataObserved
                        viewLifecycleOwner.lifecycleScope.launch {
                            liveDataObserved.value = bookShelfViewModel.getBookById(it).toNewBook()
                        }

                        editDialogClose.setOnClickListener { dismiss() }
                        editDialogAction.setOnClickListener {
                            liveDataObserved.value?.let { editBook ->
                                viewLifecycleOwner.lifecycleScope.launch {
                                    bookId?.let {
                                        bookShelfViewModel.updateBook(editBook.toStoredBook(it))
                                    }
                                    dismiss()
                                }
                            }
                        }
                    }
                }
            bookRecordLayoutBinding = editBookRecordBinding.bookRecordLayout
//            addBookViewModel.isEditBookFormValid.observe(viewLifecycleOwner) {
//                Log.d(TAG, "isEditBookFormValid mediator observed changes - value: $it")
//            }
        }

        bookRecordLayoutBinding
            .apply {
                startedToFinishedDateTextField.setOnClickListener {
                    createDateRangePicker(startedToFinishedDateTextField)
                }

                startedDateTextField.setOnClickListener {
                    createDatePicker(startedDateTextField)
                }

                // When error has been set, and then focus on the text field moved to another text field
                // error should be cleared
                bookNameTextField.setOnFocusChangeListener { _, focused ->
                    if (!focused) {
                        bookNameLayout.error = null
                    }
                }

                bookAuthorTextField.setOnFocusChangeListener { _, focused ->
                    if (!focused) {
                        bookAuthorLayout.error = null
                    }
                }

                bookGenreTextField.setOnFocusChangeListener { _, focused ->
                    if (!focused) {
                        bookGenreLayout.error = null
                    }
                }

                addBookCoverBtn.setOnClickListener {
                    // open a dialog fragment to add a book cover photo
                    // by either capturing through camera
                    // or choosing from gallery
                    val newDialogFragment = AddBookCoverDialog.getInstance(requireContext())
                    newDialogFragment?.show(childFragmentManager, "addBookCoverDialog")
                }
            }
        return if (bookId == null) addBookBinding.root else editBookRecordBinding.root
    }

    override fun onStart() {
        super.onStart()
        // Make the dialog fullscreen
        if (bookId != null) {
            dialog?.let {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                it.window?.setLayout(width, height)
                it.window?.setBackgroundDrawable(context?.getDrawable(R.color.md_theme_light_surface) ?: ColorDrawable(
                    Color.WHITE)
                )
            }
        }
    }

    override fun onPickOptionClicked() {
        val pickIntent = Intent(
            Intent.ACTION_OPEN_DOCUMENT, // Allow persistent access to documents owned by a document provider
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply {
            type = "image/*" // Only image files can be selected
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false) // Only one image can be selected
            putExtra(Intent.EXTRA_LOCAL_ONLY, true) // No Google Drive etc.
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Grant access (read) permission
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION) // Uri should be granted persistent (access) permission
                                                                   // for future access when retrieved from database
        }

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
            result.data?.let { intent ->
                val flags = intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION

                activity?.let { activity ->
                    val resolver = activity.contentResolver
                    intent.data?.let { uri ->
                        // Important: Actually persist the permissions set by 'flags' on 'uri'
                        resolver.takePersistableUriPermission(uri, flags)

                        liveDataObserved.value?.let { newBook ->
                            newBook.coverUri = uri.toString()
                            newBook.hasCoverImg.set(true) // even though coverUri has changed, hasCoverImg will not change according to that
                                                          // manual updating is required
                            newBook.hasCoverImg.notifyChange()
                        }
                    }
                }
            }
        }
    }

    override fun onCaptureOptionClicked() {
        // Create a temporary file and add to the list
        try {
            val img = ImageUtils.createUniqueImgFile(requireContext())
            liveDataObserved.value?.coverImgFiles?.add(img)
        } catch (e: IOException) {
            // If no file has been created, activity will not launch
            return
        }

        // If the list is not found, return
        liveDataObserved.value?.coverImgFiles?.let { imgFiles ->
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
                // The captured full-sized image will be written in the uri
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
        liveDataObserved.value?.coverImgFiles?.let { imgFiles ->
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
                    liveDataObserved.value?.let {
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
    }
}

/***
 * Set warning TextView from 'binding' visible
 * when 'view' such as RatingBar and ChipGroup has no values selected
 */
//    private fun setWarningTextVisibleIfNoSelectionFor(view: View) {
//        bookRecordLayoutBinding.apply {
//            when (view) {
//                is RatingBar -> {
//                    if (view.rating <= 0)
//                        ratingBarWarningText.visibility = View.VISIBLE
//                }
//                is ChipGroup -> {
//                    if (view.checkedChipId == -1)
//                        statusChipWarningText.visibility = View.VISIBLE
//                }
//            }
//        }
//    }

/***
 * Show warning messages based on non-valid parts of the add book form
 * Criteria:
 * (same as isAddBookFormValid)
 */
//    private fun showWarningMsgWhenFormInputMissing() {
//        bookRecordLayoutBinding.apply {
//            val selectedStatus = statusChipGroup.checkedChipId
//
//            setTextLayoutErrorIfEmpty(bookNameLayout)
//            setTextLayoutErrorIfEmpty(bookAuthorLayout)
//            setTextLayoutErrorIfEmpty(bookGenreLayout)
//
//            when (selectedStatus) {
//                R.id.readChip -> {
//                    setTextLayoutErrorIfEmpty(startedToFinishedDateLayout)
//                    // setWarningTextVisibleIfNoSelectionFor(ratingBar)
//                }
//                R.id.readingChip -> {
//                    setTextLayoutErrorIfEmpty(startedDateLayout)
//                    // setWarningTextVisibleIfNoSelectionFor(ratingBar)
//                }
//                R.id.wtrChip -> {
//                    // nothing
//                }
//                else -> {
//                    // setWarningTextVisibleIfNoSelectionFor(statusChipGroup)
//                }
//            }
//        }
//    }

/***
 * Set TextInputLayout with error message
 * if its associated TextInputEditText is empty
 */
//    private fun setTextLayoutErrorIfEmpty(layout: TextInputLayout) {
//        val errorMsg = getString(R.string.must_fill)
//        layout.error =
//            if (isTxtFieldEmpty(layout.editText as TextInputEditText))
//                errorMsg
//            else
//                null
//    }
