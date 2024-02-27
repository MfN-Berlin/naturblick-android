package berlin.mfn.naturblick.ui.shared

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import berlin.mfn.naturblick.R
import kotlin.math.roundToInt

class HRView(context: Context, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    init {
        setBackgroundResource(R.color.hr)
    }
    constructor(context: Context) : this(context, null) {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            resources.getDimension(R.dimen.hr_height).roundToInt()
        )
    }
}
