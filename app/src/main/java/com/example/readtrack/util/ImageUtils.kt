package com.example.readtrack.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

private const val TAG = "ImageUtils"
object ImageUtils {
    /***
     * Read an image from a Uri stream as Bitmap object
     */
    fun decodeUriStreamToBitmap(
        uri: String,
        width: Int,
        height: Int,
        context: Context
    ): Bitmap? {
        var uriStream: InputStream? = null
        try {
            // Open an input stream for uri
            uriStream = context.contentResolver.openInputStream(Uri.parse(uri))

            uriStream?.let { stream ->
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true // Get the image size only (set outXXX fields)

                // Decode the uri stream to get the original image size
                BitmapFactory.decodeStream(stream, null, options)

                // Important: Reopen the stream
                // as the stream position has reached its end
                stream.close()
                uriStream = context.contentResolver.openInputStream(Uri.parse(uri))
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

                        newStream.close()
                        return resizedBitmap
                    }
                }
            }
            return null
        } catch (e: Exception) {
            return null
        } finally {
            // Important: Close the stream under whatever circumstances
            uriStream?.close()
        }
    }

    /***
     * Create a file with current timestamp as its name
     */
    @Throws(IOException::class)
    fun createUniqueImgFile(context: Context): File {
        val formatter = DateTimeFormatter.ofPattern("d_MM_yyyy")
        val timeStamp = LocalDateTime.now().format(formatter)
        val fileName = "ReadTrack_${timeStamp}"
        val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // Can throw exception
        return File.createTempFile(fileName, ".jpg", fileDir)
    }

    /***
     * Read an image from a File
     */
    fun decodeFileToBitmap(
        path: String,
        width: Int,
        height: Int
    ): Bitmap {
        val options = BitmapFactory.Options()
            .apply {
                inJustDecodeBounds = true
            }
        BitmapFactory.decodeFile(path, options)

        options.apply {
            inSampleSize = calculateInSampleSize(
                options.outWidth,
                options.outHeight,
                width,
                height
            )
            inJustDecodeBounds = false
        }

        return BitmapFactory.decodeFile(path, options)
    }

    /***
     * Return the scale value of BitmapFactory.options.inSampleSize
     * in the power of 2 based on the required width and height
     * https://developer.android.com/reference/android/graphics/BitmapFactory.Options#inSampleSize
     */
    fun calculateInSampleSize(
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

    fun isValidURL(url: String): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: MalformedURLException) {
            false
        } catch (e: URISyntaxException) {
            false
        }
    }
}