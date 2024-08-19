/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.portrait

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import berlin.mfn.naturblick.databinding.FragmentSpeciesCardBinding
import berlin.mfn.naturblick.room.FullSimilarSpecies
import berlin.mfn.naturblick.utils.setSingleClickListener

class SimilarSpeciesList(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {
    private var navigateToSpeciesCallback: ((SpeciesId) -> Unit)? = null
    fun setNavigateToSpecies(navigateToSpecies: (SpeciesId) -> Unit) {
        this.navigateToSpeciesCallback = navigateToSpecies
    }

    fun setSimilarSpecies(similarSpecies: List<FullSimilarSpecies>?) {
        removeAllViews()
        similarSpecies?.let {
            val inflater = LayoutInflater.from(context)
            for (similar in it) {
                val speciesBinding =
                    FragmentSpeciesCardBinding.inflate(
                        inflater,
                        this,
                        false
                    )
                speciesBinding.similar = similar
                speciesBinding.speciesCardView.setSingleClickListener { _ ->
                    navigateToSpeciesCallback?.let {
                        it(SpeciesId(similar.species.id))
                    }
                }
                addView(speciesBinding.root)
            }
        }
    }
}

@BindingAdapter("similarSpecies")
fun similarSpeciesBinding(view: SimilarSpeciesList, similarSpecies: List<FullSimilarSpecies>?) =
    view.setSimilarSpecies(similarSpecies)
