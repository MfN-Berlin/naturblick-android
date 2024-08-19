/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.content.Context
import android.widget.*
import androidx.databinding.BindingAdapter
import berlin.mfn.naturblick.R

enum class Behavior(val isPlant: Boolean?, val value: String, val res: Int) {
    PLANT_LOSING_LEAFS(true, "Blätter abwerfend", R.string.plant_behavior_losing_leafs),
    PLANT_BLOOMING(true, "blühend", R.string.plant_behavior_blooming),
    PLANT_CARRYING_FRUITS(true, "Früchte tragend", R.string.plant_behavior_carrying_fruits),
    PLANT_WITH_BUDS(true, "knospend", R.string.plant_behavior_with_buds),
    PLANT_NEW_SHOOTS(true, "neue Triebe", R.string.plant_behavior_new_shoots),
    PLANT_WITHERED(true, "verblüht", R.string.plant_behavior_withered),
    ANIMAL_ON_A_FLOWER(false, "an einer Blüte", R.string.animal_behavior_on_a_flower),
    ANIMAL_NEST(false, "Bau/Nest", R.string.animal_behavior_nest),
    ANIMAL_BITE_MARKS(false, "Fraßspuren", R.string.animal_behavior_bite_marks),
    ANIMAL_SHELL(false, "Gehäuse/Schale", R.string.animal_behavior_shell),
    ANIMAL_OR_PLANT_INSIDE(null, "in Gebäude", R.string.behavior_inside),
    ANIMAL_CADAVER(false, "Kadaver (Totes Tier)", R.string.animal_behavior_carcass),
    ANIMAL_COCOON(false, "Kokon", R.string.animal_behavior_cocoon),
    ANIMAL_FECES(false, "Kot", R.string.animal_behavior_feces),
    ANIMAL_EGGS(false, "Laich/Eier", R.string.animal_behavior_eggs),
    ANIMAL_CALL(false, "Laut/Gesang/Ruf", R.string.animal_behavior_call),
    ANIMAL_TRACK(false, "Spur", R.string.animal_behavior_track);

    companion object {
        fun create(value: String): Behavior? =
            values().findLast {
                it.value.lowercase() == value.lowercase().trim('"')
            }

        fun parse(context: Context, value: String): Behavior? {
            val res = context.resources
            return values().findLast {
                value == res.getString(it.res)
            }
        }
    }
}

class BehaviorAdapter(context: Context, layout: Int, var values: Array<String>) :
    ArrayAdapter<String>(context, layout, values) {
    private val nothing = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            results.values = values
            results.count = values.size
            return results
        }
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return nothing
    }
}

@BindingAdapter("setAdapter")
fun setAdapter(textView: AutoCompleteTextView, adapter: BehaviorAdapter?) {
    adapter?.let {
        textView.setAdapter(it)
    }
}
