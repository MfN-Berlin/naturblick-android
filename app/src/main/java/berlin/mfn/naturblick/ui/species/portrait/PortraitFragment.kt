/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.species.portrait

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentPortraitBinding
import berlin.mfn.naturblick.databinding.IncludeMiniportraitBinding
import berlin.mfn.naturblick.databinding.IncludePortraitBinding
import berlin.mfn.naturblick.room.ImageWithSizes
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.ui.fieldbook.CreateManualObservation
import berlin.mfn.naturblick.ui.species.ConfirmSpecies
import berlin.mfn.naturblick.ui.species.PickSpecies.SELECTED_SPECIES
import berlin.mfn.naturblick.ui.species.PickSpeciesResult
import berlin.mfn.naturblick.utils.AnalyticsTracker
import berlin.mfn.naturblick.utils.SingleTrackPlayer
import berlin.mfn.naturblick.utils.cancel
import berlin.mfn.naturblick.utils.setSingleClickListener
import berlin.mfn.naturblick.utils.setupBottomInset
import berlin.mfn.naturblick.utils.setupBottomInsetMargin
import berlin.mfn.naturblick.utils.showCcInfo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.parcelize.Parcelize

@Parcelize
class SpeciesId(val value: Int) : Parcelable

class PortraitFragment : Fragment() {
    private lateinit var audioPlayer: SingleTrackPlayer

    private val confirmSpeciesResult = registerForActivityResult(ConfirmSpecies) { confirmed ->
        if(confirmed != null) {
            finish(confirmed)
        }
    }

    private fun finish(confirmed: PickSpeciesResult) {
        val intent = Intent()
        intent.putExtra(SELECTED_SPECIES, confirmed)
        requireActivity().setResult(Activity.RESULT_OK, intent)
        requireActivity().finish()
    }

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


    private fun wireImageCC(ccFab: FloatingActionButton, image: ImageWithSizes) {
        ccFab.setSingleClickListener {
            showCcInfo(layoutInflater, image.image, requireContext())
        }
    }
    private fun wireImageButtons(ccFab: FloatingActionButton, fullscreenFab: FloatingActionButton, image: ImageWithSizes) {
        wireImageCC(ccFab, image)
        image.largest?.let { fullscreen ->
            val intent =
                Intent(Intent.ACTION_VIEW).setDataAndType(fullscreen.fullUrl.toUri (), "image/jpeg")
            if(intent.resolveActivity(requireActivity().packageManager) != null) {
                fullscreenFab.setSingleClickListener {
                    startActivity(intent)
                }
                fullscreenFab.visibility = View.VISIBLE
            }
        }
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
        val speciesIdPathFragment = speciesIdPathFragment(activity)
        val portraitViewModel: PortraitViewModel by viewModels(extrasProducer = {
            MutableCreationExtras(defaultViewModelCreationExtras).apply {
                set(PortraitViewModel.SPECIES_ID_PATH_FRAGMENT_KEY, speciesIdPathFragment)
            }
        }) {
            PortraitViewModel.Factory
        }

        if (portraitViewModel.speciesId == null && speciesIdPathFragment == null) {
            navToGroupOverview()
        }

        val binding = FragmentPortraitBinding.inflate(inflater, container, false)

        binding.createObservationAction.isExtended = true
        binding.createObservationAction.setupBottomInsetMargin()
        binding.portraitContent.setupBottomInset()
        binding.buttonSheet.setupBottomInset()
        portraitViewModel.speciesAndPortrait.observe(viewLifecycleOwner) { (species, portrait) ->
            (requireActivity() as PortraitActivity).supportActionBar?.title = species.name

            AnalyticsTracker.trackPortrait(
                requireActivity().application as NaturblickApplication,
                species
            )

            if (portrait != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.portraitContent.setOnScrollChangeListener { _, _, _, _, _ ->
                        if (binding.portraitContent.scrollY < 1 && !binding.createObservationAction.isExtended) {
                            binding.createObservationAction.extend()
                        } else if (binding.portraitContent.scrollY > 1 && binding.createObservationAction.isExtended) {
                            binding.createObservationAction.shrink()
                        }
                    }
                }
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
                        if (portraitViewModel.selectable) {
                            confirmSpeciesResult.launch(speciesId)
                        } else {
                            findNavController().navigate(
                                PortraitFragmentDirections.actionNavPortraitToNavPortrait(
                                    speciesId,
                                    false
                                )
                            )
                        }
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
                portrait.description?.let {
                    wireImageButtons(
                        portraitBinding.buttonDescriptionCc,
                        portraitBinding.buttonDescriptionFullscreen,
                        it
                    )
                }
                portrait.inTheCity?.let {
                    wireImageButtons(portraitBinding.includePortraitImageInTheCity.buttonCc, portraitBinding.includePortraitImageInTheCity.buttonFullscreen, it)
                }
                portrait.goodToKnow?.let {
                    wireImageButtons(portraitBinding.includePortraitImageGoodToKnow.buttonCc, portraitBinding.includePortraitImageGoodToKnow.buttonFullscreen, it)
                }
            } else {
                val miniPortraitBinding =
                    IncludeMiniportraitBinding
                        .inflate(inflater, container, false)

                miniPortraitBinding.buttonWikipedia.setSingleClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, species.wikipediaUri))
                }
                species.imageUrlOwner?.let { owner ->
                    species.imageUrlSource?.let { source ->
                        species.imageUrlLicense?.let { license ->
                            miniPortraitBinding.buttonSpeciesImageCc.setSingleClickListener {
                                showCcInfo(inflater, owner, source, license, requireContext())
                            }
                            miniPortraitBinding.buttonSpeciesImageCc.visibility = View.VISIBLE
                        }
                    }
                }
                miniPortraitBinding.species = species
                binding.portraitContent.removeAllViews()
                binding.portraitContent.addView(miniPortraitBinding.root)
                species.avatarOrigUrl?.let {
                    val intent = Intent(Intent.ACTION_VIEW).setDataAndType( it.toUri(), "image/jpeg")
                    if(intent.resolveActivity(requireActivity().packageManager) != null) {
                        miniPortraitBinding.buttonFullscreen.setSingleClickListener {
                            startActivity(intent)
                        }
                        miniPortraitBinding.buttonFullscreen.visibility = View.VISIBLE
                    }
                }
            }
            if (portraitViewModel.selectable) {
                binding.buttonSheet.visibility = View.VISIBLE
                binding.buttonConfirm.setSingleClickListener { _ ->
                    finish(PickSpeciesResult(species.id))
                }
                binding.buttonBack.setSingleClickListener {
                    cancel()
                }
            } else {
                binding.createObservationAction.visibility = View.VISIBLE
                binding.createObservationAction.setSingleClickListener { _ ->
                    findNavController().navigate(
                        PortraitFragmentDirections.navPortraitToNavFieldbookObservation(
                            CreateManualObservation(species.id)
                        )
                    )
                }
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
        audioPlayer.toggle(Uri.parse("${BuildConfig.DJANGO_URL}$url"))
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
