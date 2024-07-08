package berlin.mfn.naturblick.ui.idresult

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.LinearLayout
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentIdResultItemBinding
import berlin.mfn.naturblick.utils.setSingleClickListener

class IdResultListLayout(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {

    fun setIdResultList(idResultList: List<IdResultWithSpecies>) {
        removeAllViews()
        val inflater = LayoutInflater.from(context)
        for (idResult in idResultList) {
            val binding =
                FragmentIdResultItemBinding.inflate(
                    inflater,
                    this,
                    false
                )
            binding.idResult = idResult
            val attributeId = if(idResult.score > 50)  R.attr.colorOnSecondarySignalMedium else R.attr.colorOnSecondarySignalLow
            val value = TypedValue()
            context.theme.resolveAttribute(attributeId, value, true)
            binding.score.setTextColor(value.data)
            binding.root.setSingleClickListener {
                idResult.agreeClick()
            }
            addView(binding.root)
        }
    }
}
