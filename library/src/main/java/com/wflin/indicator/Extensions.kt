package com.wflin.indicator

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import androidx.annotation.UiThread

/**
 * @author: wflin
 * @data: 2022/5/16
 * @desc:
 */

/**
 * dp to px
 */
val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

/**
 * dp to px
 */
val Int.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

/**
 * set View's width
 */
@UiThread
fun View.setWidth(width: Int) {
    val lp = layoutParams
    lp?.let {
        lp.width = width
        layoutParams = it
    }
}

/**
 * set View's width
 */
@UiThread
fun View.setHeight(height: Int) {
    val lp = layoutParams
    lp?.let {
        lp.height = height
        layoutParams = it
    }
}