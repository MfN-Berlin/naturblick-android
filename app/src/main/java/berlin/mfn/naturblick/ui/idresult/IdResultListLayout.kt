package berlin.mfn.naturblick.ui.idresult

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
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
            binding.root.setSingleClickListener {
                idResult.agreeClick()
            }
            addView(binding.root)
        }
    }
}
