/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.imprint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentSourcesImprintBinding
import berlin.mfn.naturblick.room.SourcesImprint
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.species.portrait.sourcesBinding
import berlin.mfn.naturblick.utils.isGerman
import berlin.mfn.naturblick.utils.setupBottomInset
import kotlinx.coroutines.launch

class SourcesAdapter(
    private val sourcesImprint: List<SourcesImprint>
) :
    RecyclerView.Adapter<SourcesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView

        init {
            textView = view.findViewById(R.id.mTextView)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.sources_item, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        sourcesImprint[position].let {
            val licencse = it.licence?.let { l -> " ($l)" } ?: ""
            val imgSource = it.imageSource?.let { imgS -> "\n$imgS" } ?: ""
            val scieName = if (isGerman()) it.scieName else it.scieNameEng

            sourcesBinding(viewHolder.textView, "${it.author}$licencse\n$scieName$imgSource")
        }
    }

    override fun getItemCount() = sourcesImprint.size
}

class SourcesImprintFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSourcesImprintBinding.inflate(inflater, container, false)
        binding.rvSources.setupBottomInset()
        viewLifecycleOwner.lifecycleScope.launch {
            val dbSources =
                StrapiDb.getDb(requireContext()).sourcesImprintDao().getSourcesImprint()
            binding.rvSources.adapter = SourcesAdapter(dbSources)
        }

        return binding.root
    }
}
