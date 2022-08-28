package ru.netology.nerecipe.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.os.Environment
import android.util.Log
import java.io.*


class ImageStorageManager {
    companion object {
        private const val IMAGE_DIR = "images"
        private const val THUMB_DIR = "thumb"

        private const val THUMB_QUALITY = 50

        private const val EXTERNAL_IMAGE_DIR = "/nerecipe/"
        private const val EXTERNAL_THUMB_DIR = EXTERNAL_IMAGE_DIR.plus("thumb/")

        private const val IMAGE_EXT = "png"
        private const val THUMB_EXT = "jpg"


        fun renameFileInInternalStorage(
            context: Context,
            imageFileNameFrom: String,
            imageFileNameTo: String
        ): String? {
            val directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE)
            val fileFrom = File(directory, imageFileNameFrom)
            val fileTo = File(directory, imageFileNameTo)

            var result = true

            if (fileFrom.exists()) {
                if (fileTo.exists()) result = fileTo.delete()
                if (result) result = fileFrom.renameTo(File(directory, imageFileNameTo))
            }

            return if (result) fileTo.absolutePath else null
        }

        fun saveToInternalStorage(
            context: Context,
            bitmapImage: Bitmap,
            imageFileName: String
        ): String {
            val directory =
                context.getDir(makeInternalImageFileDirectory(imageFileName), Context.MODE_PRIVATE)

            val file = File(directory, makeImageFileName(imageFileName))

            val fos = FileOutputStream(file, false)
            if (imageFileName.contains(THUMB_IMAGE_POSTFIX, true))
                bitmapImage.compress(Bitmap.CompressFormat.JPEG, THUMB_QUALITY, fos)
            else
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)

            fos.flush()
            fos.close()

            startMediaScanner(context, file)

            return file.absolutePath
        }

        fun getImageFromInternalStorage(context: Context, imageFileName: String): Bitmap? {
            return try {
                val directory = context.getDir(
                    makeInternalImageFileDirectory(imageFileName),
                    Context.MODE_PRIVATE
                )
                val file = File(directory, makeImageFileName(imageFileName))

                if (file.exists()) BitmapFactory.decodeStream(FileInputStream(file))
                else null
            } catch (e: FileNotFoundException) {
                null
            }
        }

        fun isImageExistInInternalStorage(
            context: Context,
            imageFileName: String
        ): Boolean {
            val directory =
                context.getDir(makeInternalImageFileDirectory(imageFileName), Context.MODE_PRIVATE)
            val file = File(directory, makeImageFileName(imageFileName))

            return file.exists()
        }

        fun deleteImageFromInternalStorage(
            context: Context,
            imageFileName: String
        ): Boolean? {
            return try {
                val dir = context.getDir(
                    makeInternalImageFileDirectory(imageFileName),
                    Context.MODE_PRIVATE
                )
                val file = File(dir, makeImageFileName(imageFileName))
                return file.delete()
            } catch (e: FileNotFoundException) {
                null
            }
        }

        fun saveToExternalStorage(
            context: Context,
            bitmapImage: Bitmap,
            imageFileName: String
        ): String? {
            val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )

            val directory = File(path, makeExternalImageFileDirectory(imageFileName))

            val file = File(directory, makeImageFileName(imageFileName))

            try {
                if (!directory.isDirectory) {
                    directory.mkdirs()
                }

                val fos = FileOutputStream(file)
                if (imageFileName.contains(THUMB_IMAGE_POSTFIX, true))
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, THUMB_QUALITY, fos)
                else
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos)

                fos.flush()
                fos.close()

                startMediaScanner(context, file)

                return file.absolutePath
            } catch (e: IOException) {
                // Unable to create file, likely because external storage is
                // not currently mounted.
                Log.w("ExternalStorage", "Error writing $file", e)
                print(e.stackTrace)
                return null
            }
        }

        private fun startMediaScanner(context: Context, file: File) {
//            if (!file.toString().contains(THUMB_IMAGE_POSTFIX, true))
            MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null,
                OnScanCompletedListener { path, uri ->
                    Log.i("ExternalStorage", "Scanned $path:")
                    Log.i("ExternalStorage", "-> uri=$uri")
                })
        }

        fun deleteImageFromExternalStorage(imageFileName: String): Boolean? {
            // Create a path where we will place our picture in the user's
            // public pictures directory and delete the file.  If external
            // storage is not currently mounted this will fail.
            return try {
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )

                val directory = File(path, makeExternalImageFileDirectory(imageFileName))
                val file = File(directory, makeImageFileName(imageFileName))

                file.delete()
            } catch (e: FileNotFoundException) {
                null
            }
        }

        fun getImageFromExternalStorage(imageFileName: String): Bitmap? {
            // Create a path where we will place our picture in the user's
            // public pictures directory and delete the file.  If external
            // storage is not currently mounted this will fail.
            return try {
                val path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )

                val directory = File(path, makeExternalImageFileDirectory(imageFileName))
                val file = File(directory, makeImageFileName(imageFileName))

                if (file.exists())
                    BitmapFactory.decodeStream(FileInputStream(file))
                else
                    null
            } catch (e: FileNotFoundException) {
                null
            }
        }

        fun isImageExistInExternalStorage(imageFileName: String): Boolean {
            // Create a path where we will place our picture in the user's
            // public pictures directory and check if the file exists.  If
            // external storage is not currently mounted this will think the
            // picture doesn't exist.
            val path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            )
            val directory = File(path, makeExternalImageFileDirectory(imageFileName))
            val file = File(directory, makeImageFileName(imageFileName))
            return file.exists()
        }

        private fun makeImageFileName(fileName: String): String =
            if (fileName.contains(THUMB_IMAGE_POSTFIX, true))
                fileName.plus(".").plus(THUMB_EXT)
            else
                fileName.plus(".").plus(IMAGE_EXT)

        private fun makeExternalImageFileDirectory(directory: String): String =
            if (directory.contains(THUMB_IMAGE_POSTFIX, true)) EXTERNAL_THUMB_DIR
            else EXTERNAL_IMAGE_DIR

        private fun makeInternalImageFileDirectory(directory: String): String =
            if (directory.contains(THUMB_IMAGE_POSTFIX, true)) THUMB_DIR
            else IMAGE_DIR
    }
}