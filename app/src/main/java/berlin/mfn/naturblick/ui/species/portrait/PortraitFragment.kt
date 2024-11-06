/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.portrait

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentPortraitBinding
import berlin.mfn.naturblick.databinding.IncludeMiniportraitBinding
import berlin.mfn.naturblick.databinding.IncludePortraitBinding
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.fieldbook.CreateManualObservation
import berlin.mfn.naturblick.utils.AnalyticsTracker
import berlin.mfn.naturblick.utils.SingleTrackPlayer
import berlin.mfn.naturblick.utils.Wikipedia
import berlin.mfn.naturblick.utils.setSingleClickListener
import berlin.mfn.naturblick.utils.showCcInfo
import kotlinx.parcelize.Parcelize

@Parcelize
class SpeciesId(val value: Int) : Parcelable

class PortraitFragment : Fragment() {
    private lateinit var audioPlayer: SingleTrackPlayer

    private fun navToGroupOverview() {
        findNavController().navigate(PortraitFragmentDirections.actionNavPortraitToNavGroups())
    }

    private fun speciesIdPathFragment(activity: FragmentActivity?): String? {
        try {
            return activity?.intent?.data?.pathSegments?.last()
        } catch (_: NoSuchElementException) {
            navToGroupOverview()
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.audioPlayer = SingleTrackPlayer(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args: PortraitFragmentArgs by navArgs()
        val speciesId = args.speciesId
        val speciesIdPathFragment = speciesIdPathFragment(activity)

        if (speciesId == null && speciesIdPathFragment == null) {
            navToGroupOverview()
        }

        val db = StrapiDb.getDb(requireContext())

        val portraitViewModel: PortraitViewModel by viewModels {
            PortraitViewModelFactory(
                db,
                speciesId,
                speciesIdPathFragment
            )
        }
        val binding = FragmentPortraitBinding.inflate(inflater, container, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.portraitContent.setOnScrollChangeListener { _, _, _, _, _ ->
                if (binding.portraitContent.scrollY < 1 && !binding.createObservationAction.isExtended) {
                    binding.createObservationAction.extend()
                } else if (binding.portraitContent.scrollY > 1 && binding.createObservationAction.isExtended) {
                    binding.createObservationAction.shrink()
                }
            }
        }

        binding.createObservationAction.isExtended = true

        portraitViewModel.speciesAndPortrait.observe(viewLifecycleOwner) { (species, portrait) ->
            (requireActivity() as PortraitActivity).supportActionBar?.title = species.name

            AnalyticsTracker.trackPortrait(
                requireActivity().application as NaturblickApplication,
                species
            )

            if (portrait != null) {
                val portraitBinding =
                    IncludePortraitBinding.inflate(inflater, container, false)
                audioPlayer.setOnIsPlayingChangedListener { isPlaying ->
                    if (isPlaying) {
                        portraitBinding.buttonToggleAudio.setImageResource(
                            R.drawable.ic_baseline_pause_circle_outline_24
                        )
                    } else {
                        portraitBinding.buttonToggleAudio.setImageResource(
                            R.drawable.ic_baseline_play_circle_outline_24
                        )
                    }
                }
                portraitBinding
                    .portraitSimilarSpecies
                    .setNavigateToSpecies { speciesId ->
                        findNavController().navigate(
                            PortraitFragmentDirections.actionNavPortraitToNavPortrait(
                                speciesId,
                                args.allowSelection
                            )
                        )
                    }
                portraitBinding.species = species
                portraitBinding.portrait = portrait
                binding.portraitContent.removeAllViews()
                binding.portraitContent.addView(portraitBinding.root)
                portrait.portrait.audioUrl?.let { url ->
                    portraitBinding.hasAudio = true
                    portraitBinding.buttonToggleAudio.setOnClickListener {
                        toggleAudio(url, species)
                    }
                }
                portraitBinding.buttonDescriptionCc.setSingleClickListener {
                    portrait.description?.image?.let {
                        showCcInfo(inflater, it, requireContext())
                    }
                }
                portrait.inTheCity?.let { iws ->
                    portraitBinding.includePortraitImageInTheCity.buttonCc.setSingleClickListener {
                        showCcInfo(inflater, iws.image, requireContext())
                    }
                }
                portrait.goodToKnow?.let { gtk ->
                    portraitBinding.includePortraitImageGoodToKnow.buttonCc.setSingleClickListener {
                        showCcInfo(inflater, gtk.image, requireContext())
                    }
                }
            } else {
                val miniPortraitBinding =
                    IncludeMiniportraitBinding
                        .inflate(inflater, container, false)
                miniPortraitBinding.buttonWikipedia.setSingleClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, species.wikipediaUri))
                }
                miniPortraitBinding.species = species
                binding.portraitContent.removeAllViews()
                binding.portraitContent.addView(miniPortraitBinding.root)
            }
            if (args.allowSelection) {
                binding.createObservationAction.visibility = View.VISIBLE
                binding.createObservationAction.setSingleClickListener { _ ->
                    findNavController().navigate(
                        PortraitFragmentDirections.navPortraitToNavFieldbookObservation(
                            CreateManualObservation(species.id)
                        )
                    )
                }
            } else {
                binding.createObservationAction.visibility = View.GONE
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun toggleAudio(url: String, species: Species) {
        AnalyticsTracker.trackSpeciesPortraitSound(
            requireActivity().application as NaturblickApplication,
            url,
            species
        )
        audioPlayer.toggle(Uri.parse("${BuildConfig.STRAPI_URL}$url"))
    }

    override fun onStop() {
        super.onStop()
        audioPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer.release()
    }
}
