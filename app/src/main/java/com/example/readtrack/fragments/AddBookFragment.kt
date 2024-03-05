package com.example.readtrack.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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
import com.example.readtrack.util.DateUtils
import com.example.readtrack.util.ImageUtils
import com.example.readtrack.util.ImageUtils.isValidURL
import com.example.readtrack.viewmodels.AddBookViewModel
import com.example.readtrack.viewmodels.BookShelfViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.net.URL
import kotlin.math.sqrt


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
        }

        // If cover uri is url, fetch the image from network using coroutine
        val coverUri = liveDataObserved.value?.coverUri ?: ""
        if (isValidURL(coverUri)) {
            updateCoverImg(coverUri)
        }

        bookRecordLayoutBinding
            .apply {
                startedToFinishedDateTextField.setOnClickListener {
                    DateUtils.createDateRangePicker(startedToFinishedDateTextField, "Started Date - Finished Date", childFragmentManager)
                }

                startedDateTextField.setOnClickListener {
                    DateUtils.createDatePicker(startedDateTextField, "Started Date", childFragmentManager)
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

    private fun updateCoverImg(coverUri: String) {
        val width = requireContext().resources.getDimensionPixelSize(R.dimen.bk_cover_img_width)
        val height = requireContext().resources.getDimensionPixelSize(R.dimen.bk_cover_img_height)

        var uriStream: InputStream? = null
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                uriStream = URL(coverUri).openStream()
                uriStream?.let { stream ->
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true // Get the image size only (set outXXX fields)

                    // Decode the uri stream to get the original image size
                    BitmapFactory.decodeStream(stream, null, options)

                    // Important: Reopen the stream
                    // as the stream position has reached its end
                    stream.close()

                    uriStream = URL(coverUri).openStream()

                    uriStream?.let { newStream ->
                        // E.g. inSampleSize = 2 -->
                        // 1/2 width & height (1/4 num. of pixels)
                        options.inSampleSize = ImageUtils.calculateInSampleSize(
                            options.outWidth, // original width and height
                            options.outHeight,
                            width,
                            height
                        )

                        // Output the down-sampled image
                        options.inJustDecodeBounds = false
                        val roughBitmap = BitmapFactory.decodeStream(
                            newStream,
                            null,
                            options
                        )

                        roughBitmap?.let { bitmap ->
                            val dstHeight = sqrt(width * height / (bitmap.width.toDouble() / bitmap.height))
                            val dstWidth = (dstHeight / bitmap.height) * bitmap.width

                            val resizedBitmap = Bitmap.createScaledBitmap(
                                bitmap,
                                dstWidth.toInt(),
                                dstHeight.toInt(),
                                true
                            )

                            withContext(Dispatchers.Main) {
                                bookRecordLayoutBinding.addBookCoverBtn.background = BitmapDrawable(requireContext().resources, resizedBitmap)
                            }

                            newStream.close()
                        }
                    }
                }
            }
        }
    }
}