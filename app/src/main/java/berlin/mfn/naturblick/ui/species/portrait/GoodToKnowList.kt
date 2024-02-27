package berlin.mfn.naturblick.ui.species.portrait

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.databinding.BindingAdapter
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.ViewGoodToKnowItemBinding
import berlin.mfn.naturblick.room.GoodToKnow
import berlin.mfn.naturblick.room.Species

class GoodToKnowList(context: Context, attributeSet: AttributeSet) :
    LinearLayout(context, attributeSet) {

    fun setFacts(facts: List<String>, species: Species) {
        removeAllViews()
        val inflater = LayoutInflater.from(context)
        for (fact in facts) {
            val binding =
                ViewGoodToKnowItemBinding.inflate(
                    inflater,
                    this,
                    false
                )
            binding.fact.text = fact
            addView(binding.root)
        }

        val redListGermanyStatus = when (species.redListGermany) {
            "gefahrdet" -> R.string.red_list_germany_gefahrdet
            "Vorwarnliste" -> R.string.red_list_germany_Vorwarnliste
            "ausgestorbenOderVerschollen" -> R.string.red_list_germany_ausgestorbenOderVerschollen
            "vomAussterbenBedroht" -> R.string.red_list_germany_vomAussterbenBedroht
            "starkGefahrdet" -> R.string.red_list_germany_starkGefahrdet
            "GefahrdungUnbekanntenAusmasses" ->
                R.string.red_list_germany_GefahrdungUnbekanntenAusmasses
            "extremSelten" -> R.string.red_list_germany_extremSelten
            "DatenUnzureichend" -> R.string.red_list_germany_DatenUnzureichend
            "ungefahrdet" -> R.string.red_list_germany_ungefahrdet
            "nichtBewertet" -> R.string.red_list_germany_nichtBewertet
            "keinNachweis" -> R.string.red_list_germany_keinNachweis
            null -> R.string.red_list_germany_nichtBewertet
            else ->
                throw IllegalStateException(
                    "Undefined endangered status: ${species.redListGermany}"
                )
        }
        val redListGermanyBinding =
            ViewGoodToKnowItemBinding.inflate(
                inflater,
                this,
                false
            )
        redListGermanyBinding.fact.text =
            context.resources.getString(
                R.string.red_list_germany,
                context.resources.getString(redListGermanyStatus)
            )
        addView(redListGermanyBinding.root)

        val iucnStatus = when (species.iucnCategory) {
            "NE" -> R.string.iucn_ne
            "DD" -> R.string.iucn_dd
            "LC" -> R.string.iucn_lc
            "NT" -> R.string.iucn_nt
            "VU" -> R.string.iucn_vu
            "EN" -> R.string.iucn_en
            "CR" -> R.string.iucn_cr
            "EW" -> R.string.iucn_ew
            "EX" -> R.string.iucn_ex
            null -> R.string.iucn_ne
            else -> throw IllegalStateException("Undefined IUCN category: ${species.iucnCategory}")
        }
        val iucnBinding =
            ViewGoodToKnowItemBinding.inflate(
                inflater,
                this,
                false
            )
        iucnBinding.fact.text =
            context.resources.getString(
                R.string.iucn_category,
                context.resources.getString(iucnStatus)
            )
        addView(iucnBinding.root)
    }
}

@BindingAdapter("facts", "species")
fun setFacts(goodToKnowList: GoodToKnowList, features: List<GoodToKnow>, species: Species) {
    goodToKnowList.setFacts(
        features.map {
            it.fact
        },
        species
    )
}
