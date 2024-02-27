package berlin.mfn.naturblick.utils

import android.text.method.LinkMovementMethod
import androidx.databinding.BindingAdapter
import com.google.android.material.textview.MaterialTextView

@BindingAdapter("autoLink")
fun autoLink(textView: MaterialTextView, active: Boolean?) {
    if (active != null && active) {
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}
