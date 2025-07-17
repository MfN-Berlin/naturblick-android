/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.sound

import android.Manifest
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.FragmentRecordSoundBinding
import berlin.mfn.naturblick.ui.info.settings.Settings
import berlin.mfn.naturblick.utils.*
import java.io.FileDescriptor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class RecordSoundFragment : Fragment(), RequiredPermissionCallback {

    private lateinit var binding: FragmentRecordSoundBinding
    private lateinit var model: RecordSoundViewModel
    private lateinit var cropModel: CropSoundViewModel
    private var localMedia: EmptyLocalMedia? = null
    private var counterJob: Job? = null
    private var openFD: AssetFileDescriptor? = null

    private val counter = (1..(MAX_LENGTH * 10)).asSequence()
        .asFlow()
        .map { it.toFloat() / 10f }
        .onEach {
            delay(100)
        }

    private var mediaRecorder: MediaRecorder? = null

    private fun stopRecorder(intentional: Boolean): LocalMedia? {
        model.stop()
        cancelJob()
        val recorder = mediaRecorder
        mediaRecorder = null
        if (recorder != null) {
            try {
                recorder.stop()
                recorder.reset()
                recorder.release()
            } catch (e: java.lang.RuntimeException) {
                close()
                localMedia?.externallyFailed(requireContext())
                localMedia = null
                if (intentional)
                    throw e
                else
                    return null
            }
            close()
            return if (intentional) {
                val media = localMedia?.externallySuccessfullyCreated(requireContext())
                localMedia = null
                media
            } else {
                localMedia?.externallyFailed(requireContext())
                localMedia = null
                null
            }
        } else {
            return null
        }
    }

    private fun cancelJob() {
        counterJob?.cancel()
        counterJob = null
    }

    private fun stopRecording() {
        val media = stopRecorder(true)
        media?.let {
            Settings.checkCcBy(requireActivity(), layoutInflater) {
                cropModel.setMedia(media)
                findNavController().navigate(
                    RecordSoundFragmentDirections.actionNavRecordSoundToNavCropSound()
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: RecordSoundViewModel by viewModels()
        model = viewModel
        model.setStart(::start)
        val cropViewModel: CropSoundViewModel by activityViewModels {
            CropSoundViewModelFactory(
                requireActivity().application
            )
        }
        cropModel = cropViewModel
        binding = FragmentRecordSoundBinding.inflate(inflater, container, false)
        binding.buttonSheet.setupBottomInset()
        binding.lifecycleOwner = viewLifecycleOwner
        permissionChecker.requirePermission(requireContext(), this)
        binding.root.keepScreenOn = true
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        stopRecorder(false)
    }

    override fun onResume() {
        super.onResume()
        model.resumed()
    }

    private fun open(localMedia: EmptyLocalMedia): FileDescriptor {
        openFD?.let {
            it.close()
        }
        val newFD = localMedia.openFileDescriptor(requireContext(), "w")
        openFD = newFD
        return newFD.fileDescriptor
    }

    private fun close() {
        openFD?.let {
            it.close()
        }
        openFD = null
    }

    fun start() {
        localMedia = Media.createEmpty(MediaType.MP4, requireContext()).let { localMedia ->
            binding.textViewCounter.text = MediaPlayerService.timeFormat(0f)
            counterJob = lifecycleScope.launchWhenStarted {
                counter.cancellable().collectLatest {
                    binding.textViewCounter.text = MediaPlayerService.timeFormat(it)
                    if (it >= MAX_LENGTH) {
                        stopRecording()
                    }
                }
            }
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(requireContext())
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                        getSystemService(
                                requireContext(),
                                AudioManager::class.java
                            )?.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED)
                        == "true"
                    ) {
                        MediaRecorder.AudioSource.UNPROCESSED
                    } else {
                        MediaRecorder.AudioSource.VOICE_RECOGNITION
                    }
                )
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(open(localMedia))
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(256000)
                setAudioChannels(1)
                setAudioSamplingRate(44100)
                prepare()
                start()
            }
            binding.buttonStopRecording.setSingleClickListener {
                stopRecording()
            }
            localMedia
        }
    }

    override fun requiredPermissionGranted() {
        model.permissionGranted()
    }

    private val permissionChecker = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        registerRequiredPermission(
            Manifest.permission.RECORD_AUDIO, R.string.record_permission_missing,
            this
        )
    } else {
        registerRequiredPermission(
            listOf(
                Permission(Manifest.permission.RECORD_AUDIO, R.string.record_permission_missing),
                Permission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    R.string.storage_permission_missing
                )
            ),
            this
        )
    }

    companion object {
        const val TAG = "RecordSound"
        private const val MAX_LENGTH = 60
    }
}
