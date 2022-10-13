package com.example.readtrack.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream
import java.lang.Exception

object ImageUtils {
    /***
     * Read an image from a Uri stream as Bitmap object
     */
    fun decodeUriStreamToBitmap(
        uri: Uri,
        width: Int,
        height: Int,
        context: Context
    ): Bitmap? {
        var uriStream: InputStream? = null
        try {
            // Open a uri stream
            uriStream = context.contentResolver.openInputStream(uri)

            uriStream?.let { stream ->
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true // Get the image size only (set outXXX fields)

                // Decode the uri stream to get the original image size
                BitmapFactory.decodeStream(stream, null, options)

                // Important: reopen the stream
                // as the stream position has reached its end
                stream.close()
                uriStream = context.contentResolver.openInputStream(uri)

                uriStream?.let { newStream ->
                    // E.g. inSampleSize = 2 -->
                    // 1/2 width & height (1/4 num. of pixels)
                    options.inSampleSize = calculateInSampleSize(
                        options.outWidth, // original width and height
                        options.outHeight,
                        width,
                        height
                    )

                    // Output the down-sampled image
                    options.inJustDecodeBounds = false
                    val bitmap = BitmapFactory.decodeStream(
                        newStream,
                        null,
                        options
                    )

                    newStream.close()
                    return bitmap
                }

            } ?: return null

        } catch (e: Exception) {
            return null
        } finally {
            // Close the stream under whatever circumstances
            uriStream?.close()
        }
    }

    /***
     * Return the scale value of BitmapFactory.options.inSampleSize
     * in the power of 2 based on the required width and height
     * https://developer.android.com/reference/android/graphics/BitmapFactory.Options#inSampleSize
     */
    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1 // Original size if <= 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2 // BitmapFactory.options.inSampleSize takes values of power of 2
            }
        }
        return inSampleSize
    }
}