/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.os.SystemClock
import android.view.View

class SingleClickListener(
    private var defaultInterval: Int = 1500,
    private val onSingleCLick: (View) -> Unit
) : View.OnClickListener {
    private var lastTimeClicked: Long = 0
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval) {
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSingleCLick(v)
    }
}

fun View.setSingleClickListener(onSingleClick: (View) -> Unit) {
    val singleClickListener = SingleClickListener {
        onSingleClick(it)
    }
    setOnClickListener(singleClickListener)
}
