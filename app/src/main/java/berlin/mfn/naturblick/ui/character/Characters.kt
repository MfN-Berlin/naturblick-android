/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.character

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import berlin.mfn.naturblick.databinding.ViewCharacterItemBinding
import berlin.mfn.naturblick.ui.shared.HRView

class Characters(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {

    private var bindings: List<ViewCharacterItemBinding> = emptyList()

    fun setCharacterValues(state: CharacterValuesState, toggle: (Int) -> Unit) {
        if (bindings.isEmpty()) {
            val inflater = LayoutInflater.from(context)
            bindings = state.characters.map { character ->
                val valueBinding =
                    ViewCharacterItemBinding.inflate(
                        inflater,
                        this,
                        false
                    )
                valueBinding.character = character
                valueBinding.characterValues.setCharacterValues(
                    character.values,
                    state.selected,
                    toggle
                )
                valueBinding
            }
            val views: List<View> =
                bindings.flatMap {
                    listOf(it.root, HRView(context))
                }.dropLast(1)
            views.forEach(::addView)
        } else {
            for ((binding, character) in bindings.zip(state.characters)) {
                binding.characterValues.setCharacterValues(character.values, state.selected, toggle)
            }
        }
    }
}
