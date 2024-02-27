package berlin.mfn.naturblick.ui.fieldbook.fieldbook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import berlin.mfn.naturblick.databinding.FragmentFieldbookItemBinding
import berlin.mfn.naturblick.utils.setSingleClickListener

class FieldbookAdapter(private val onClick: (FieldbookObservation) -> Unit) :
    PagingDataAdapter<FieldbookObservation, FieldbookAdapter.ObservationViewHolder>(
        ObservationDiffCallback
    ) {

    class ObservationViewHolder(
        private val item: FragmentFieldbookItemBinding,
        private val onClick: (FieldbookObservation) -> Unit
    ) :
        RecyclerView.ViewHolder(item.root) {
        private var currentObservation: FieldbookObservation? = null

        init {
            itemView.setSingleClickListener {
                currentObservation?.let {
                    onClick(it)
                }
            }
        }

        fun bind(observation: FieldbookObservation) {
            currentObservation = observation
            item.observation = observation
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = FragmentFieldbookItemBinding.inflate(inflater, parent, false)
        return ObservationViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ObservationViewHolder, position: Int) {
        val repoItem = getItem(position)
        repoItem?.let {
            holder.bind(it)
        }
    }
}

object ObservationDiffCallback : DiffUtil.ItemCallback<FieldbookObservation>() {
    override fun areItemsTheSame(
        oldItem: FieldbookObservation,
        newItem: FieldbookObservation
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: FieldbookObservation,
        newItem: FieldbookObservation
    ): Boolean {
        return oldItem.occurenceId == newItem.occurenceId
    }
}
