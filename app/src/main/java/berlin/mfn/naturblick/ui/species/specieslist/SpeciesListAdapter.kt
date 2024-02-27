package berlin.mfn.naturblick.ui.species.specieslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import berlin.mfn.naturblick.databinding.FragmentSpeciesItemBinding
import berlin.mfn.naturblick.room.SpeciesWithGenus
import berlin.mfn.naturblick.utils.setSingleClickListener

class SpeciesListAdapter(
    private val onClick: (SpeciesWithGenus) -> Unit
) :
    PagingDataAdapter<SpeciesWithGenus, SpeciesListAdapter.SpeciesViewHolder>(SpeciesDiffCallback) {

    class SpeciesViewHolder(
        private val item: FragmentSpeciesItemBinding,
        private val onClick: (SpeciesWithGenus) -> Unit
    ) :
        RecyclerView.ViewHolder(item.root) {
        private var currentSpecies: SpeciesWithGenus? = null

        init {
            itemView.setSingleClickListener {
                currentSpecies?.let {
                    onClick(it)
                }
            }
        }

        fun bind(species: SpeciesWithGenus) {
            currentSpecies = species
            item.species = species
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpeciesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = FragmentSpeciesItemBinding.inflate(inflater, parent, false)
        return SpeciesViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: SpeciesViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }
}

object SpeciesDiffCallback : DiffUtil.ItemCallback<SpeciesWithGenus>() {
    override fun areItemsTheSame(oldItem: SpeciesWithGenus, newItem: SpeciesWithGenus): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SpeciesWithGenus, newItem: SpeciesWithGenus): Boolean {
        return oldItem.species.id == newItem.species.id && oldItem.female == newItem.female
    }
}
