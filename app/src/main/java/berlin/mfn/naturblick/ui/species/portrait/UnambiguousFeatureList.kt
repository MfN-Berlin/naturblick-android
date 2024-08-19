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
import berlin.mfn.naturblick.databinding.ViewFeatureItemBinding
import berlin.mfn.naturblick.room.UnambiguousFeature

class UnambiguousFeatureList(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {

    fun setFeatures(features: List<String>) {
        removeAllViews()
        val inflater = LayoutInflater.from(context)
        for (feature in features) {
            val binding =
                ViewFeatureItemBinding.inflate(
                    inflater,
                    this,
                    false
                )
            binding.featureText.text = feature
            addView(binding.root)
        }
    }
}

@BindingAdapter("features")
fun setFeatures(featureList: UnambiguousFeatureList, features: List<UnambiguousFeature>) {
    featureList.setFeatures(
        features.map {
            it.description
        }
    )
}
