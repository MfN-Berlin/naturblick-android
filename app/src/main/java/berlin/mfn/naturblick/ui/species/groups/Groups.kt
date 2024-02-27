package berlin.mfn.naturblick.ui.species.groups

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import berlin.mfn.naturblick.databinding.FragmentGroupItemBinding
import berlin.mfn.naturblick.ui.data.Group
import berlin.mfn.naturblick.utils.setSingleClickListener

class Groups(context: Context, attributeSet: AttributeSet) :
    GridLayout(context, attributeSet) {

    fun setGroups(
        groups: List<Group>,
        click: (Group) -> Unit
    ) {
        removeAllViews()
        val inflater = LayoutInflater.from(context)
        groups.map { group ->
            val valueBinding = FragmentGroupItemBinding.inflate(
                inflater,
                this,
                false
            )
            valueBinding.group = group
            valueBinding.groupView.setSingleClickListener {
                click(group)
            }
            valueBinding.root
        }.forEach(::addView)
    }
}
