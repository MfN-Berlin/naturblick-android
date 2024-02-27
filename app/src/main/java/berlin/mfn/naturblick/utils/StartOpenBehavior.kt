package berlin.mfn.naturblick.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

class StartOpenBehavior<V : View>(context: Context, attrs: AttributeSet) :
    BottomSheetBehavior<V>(context, attrs) {
    init {
        state = STATE_EXPANDED
        isHideable = false
    }
}
