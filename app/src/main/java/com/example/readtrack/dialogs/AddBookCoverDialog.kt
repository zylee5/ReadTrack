package com.example.readtrack.dialogs

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.ClassCastException

class AddBookCoverDialog : DialogFragment() {
    // To deliver the onClick events to the host fragment
    interface AddBookCoverDialogListener {
        fun onPickOptionClicked()
        fun onCaptureOptionClicked()
    }

    private lateinit var listener: AddBookCoverDialogListener

    /***
     * To check if the host fragment of this dialog
     * has implemented the listener interface
     */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // context is activity
            // but this dialog fragment is called from its parent fragment
            listener = parentFragment as AddBookCoverDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$parentFragment did not implement AddBookCoverDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity as Context
        val options = ArrayList<String>()
        var captureIdx = -1
        var pickIdx = -1

        // Add the options based on
        // whether the intent can be performed
        if (canCaptureWithCamera(context)) {
            options.add("Take a photo")
            captureIdx = 0
        }

        if (canChooseFromGallery(context)) {
            options.add("Choose from gallery")
            pickIdx = if (captureIdx == 0) 1 else 0
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle("Add a book cover")
            .setItems(options.toTypedArray<CharSequence>()) { _, which ->
                when (which){
                    captureIdx -> listener.onCaptureOptionClicked()
                    pickIdx -> listener.onPickOptionClicked()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    companion object {
        /***
         * Verify whether the device can perform the intent of
         * capturing image using camera
         */
        private fun canCaptureWithCamera (context: Context): Boolean {
            val captureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )
            return captureIntent.resolveActivity(
                context.packageManager
            ) != null
        }

        /***
         * Verify whether the device can perform the intent of
         * choosing images from gallery
         */
        private fun canChooseFromGallery(context: Context): Boolean {
            val pickIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            return pickIntent.resolveActivity(
                context.packageManager
            ) != null
        }

        fun getInstance(context: Context): AddBookCoverDialog? =
            if (canCaptureWithCamera(context) || canChooseFromGallery(context))
                AddBookCoverDialog()
            else
                null
    }
}