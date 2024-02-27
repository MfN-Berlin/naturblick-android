package berlin.mfn.naturblick.ui.character

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.Space
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.ViewCharacterValueBinding
import berlin.mfn.naturblick.room.CharacterValue
import kotlin.math.roundToInt

class CharacterValues(context: Context, attributeSet: AttributeSet) :
    GridLayout(context, attributeSet) {

    private var bindings: List<ViewCharacterValueBinding> = emptyList()

    fun select(selected: Boolean, binding: ViewCharacterValueBinding) {
        if (selected) {
            binding.characterCard.strokeWidth =
                resources.getDimension(R.dimen.selected_width).roundToInt()
            binding.characterCardCheck.visibility = VISIBLE
        } else {
            binding.characterCard.strokeWidth =
                resources.getDimension(R.dimen.not_selected_width).roundToInt()
            binding.characterCardCheck.visibility = GONE
        }
    }

    fun setCharacterValues(
        characterValues: List<CharacterValue>,
        selected: Set<Int>,
        toggle: (Int) -> Unit
    ) {
        if (bindings.isEmpty()) {
            removeAllViews()
            val inflater = LayoutInflater.from(context)
            bindings = characterValues.map { value ->
                val valueBinding = ViewCharacterValueBinding.inflate(
                    inflater,
                    this,
                    false
                )
                valueBinding.value = value
                select(selected.contains(value.id), valueBinding)
                valueBinding.valueView.setOnClickListener {
                    toggle(value.id)
                }
                if (value.hasImage) {
                    valueBinding.statesExamples.setImageResource(
                        context.resources.getIdentifier(
                            "character_${value.id}",
                            "drawable",
                            context.packageName
                        )
                    )
                }
                addView(valueBinding.root)
                valueBinding
            }

            // Fill up empty columns if only first row, otherwise elements will grow to fill row
            for (i in 2 downTo characterValues.size) {
                val space = Space(context)
                val params = LayoutParams(spec(UNDEFINED, 0f), spec(UNDEFINED, 1f))
                space.layoutParams = params
                addView(space)
            }
        } else {
            for (binding in bindings) {
                binding.value?.let {
                    select(selected.contains(it.id), binding)
                }
            }
        }
    }
}
