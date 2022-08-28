package ru.netology.nerecipe.util

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import java.util.*


object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, /* flags = */0)
    }
}

fun View.focusAndShowKeyboard() {
    /**
     * https://stackoverflow.com/a/71587766
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (isFocused) {
            post {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        // We need to wait until the window gets focus.
        viewTreeObserver.addOnWindowFocusChangeListener(
            object : ViewTreeObserver.OnWindowFocusChangeListener {
                override fun onWindowFocusChanged(hasFocus: Boolean) {
                    // This notification will arrive just before the InputMethodManager gets set up.
                    if (hasFocus) {
                        this@focusAndShowKeyboard.showTheKeyboardNow()
                        // It’s very important to remove this listener once we are done.
                        viewTreeObserver.removeOnWindowFocusChangeListener(this)
                    }
                }
            })
    }
}

fun getNextId(): UUID = UUID.randomUUID()

val EMPTY_UUID = UUID.nameUUIDFromBytes("SOMETHING_STRANGE_HERE".toByteArray())
val ALL_UUID = UUID.nameUUIDFromBytes("SHOW_ALL".toByteArray())
val IMAGE_DIR = "images"


const val THUMB_IMAGE_POSTFIX = "_thumb"
const val SHARE_DIVIDER = " - "


fun RecyclerView.smoothScrollToTop() {
    layoutManager?.startSmoothScroll(object :
        LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }.apply { targetPosition = 0 })
}

fun RecyclerView.smoothSnapToPosition(
    position: Int,
    snapMode: Int = LinearSmoothScroller.SNAP_TO_START
) {
    val smoothScroller = object : LinearSmoothScroller(this.context) {
        override fun getVerticalSnapPreference(): Int = snapMode
        override fun getHorizontalSnapPreference(): Int = snapMode
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

fun Context.getLocalizedNameByResName(context: Context, resName: String) =
    try {
        context.resources?.getIdentifier(
            resName,
            "string",
            context.applicationContext.packageName
        )?.let {
            context.resources?.getString(it)
        }
    } catch (e: Resources.NotFoundException) {
        resName
    }

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}