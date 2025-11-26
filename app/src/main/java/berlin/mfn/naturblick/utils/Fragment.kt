/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.DialogCcInfoBinding
import berlin.mfn.naturblick.room.PortraitImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.cketti.mailto.EmailIntentBuilder

fun Fragment.cancel() {
    requireActivity().setResult(Activity.RESULT_CANCELED)
    requireActivity().finish()
}

data class ErrorDialog(
    val items: Int,
    val onClick: (Int, Boolean) -> Unit
) {
    companion object {
        fun forIdFlow(
            existingObservation: Boolean,
            onClick: (Int, Boolean) -> Unit
        ): ErrorDialog {
            val errorChoices = if (existingObservation) {
                R.array.autoid_not_new_error_options
            } else {
                R.array.autoid_error_options
            }
            return ErrorDialog(errorChoices, onClick)
        }
    }
}

fun <T : Any> Fragment.resolveWithErrorDialog(
    result: NetworkResult<T>,
    dialog: ErrorDialog,
    success: (T) -> Unit
) {
    result.fold(success) { error ->
        MaterialAlertDialogBuilder(
            requireContext(),
            R.style.Naturblick_MaterialComponents_Dialog_Alert
        )
            .setTitle(error.error)
            .setOnCancelListener {
                cancel()
            }
            .apply {
                setItems(dialog.items) { _, chosen ->
                    dialog.onClick(chosen, error.isSignedOut)
                }
            }
            .show()
    }
}

private fun ownerLinkToLink(context: Context, ownerLink: String?): String {
    return ownerLink
        ?.takeIf { it.isNotEmpty() }
        ?.let {
            "<br><a href='${it}'>${context.getString(R.string.ownerpage)}</a>"
        } ?: ""
}

private fun licenceToLink(licence: String): String {

    fun sa(licence: String): String {
        return if (licence.contains("sa"))
            "-sa"
        else
            ""
    }

    fun version(licence: String): String {
        return if (licence.contains("1.0"))
            "1.0"
        else if (licence.contains("2.0"))
            "2.0"
        else if (licence.contains("2.5"))
            "2.5"
        else if (licence.contains("3.0"))
            "3.0"
        else if (licence.contains("4.0"))
            "4.0"
        else
            ""
    }

    val l = licence.lowercase()
    return if (l.contains("cc0") || l.contains("cc 0"))
        "<a href='https://creativecommons.org/publicdomain/zero/1.0'>CC BY</a>"
    else if (l.contains("cc") && l.contains("by"))
        "<a href='https://creativecommons.org/licenses/by${sa(l)}/${version(l)}'>" +
                "CC BY</a>"
    else
        "CC BY"
}

private fun textAndSourceAsLink(source: String, context: Context): String {
    return "<a href='$source'>${context.getString(R.string.photo)}</a>"
}


fun Fragment.showCcInfo(
    layoutInflater: LayoutInflater,
    image: PortraitImage,
    context: Context
): AlertDialog =
    showCcInfo(layoutInflater, image.owner, image.source, image.license, image.ownerLink, context)

/**
 * https://wiki.creativecommons.org/wiki/License_Versions#Detailed_attribution_comparison_chart
 *
 *      https://creativecommons.org/publicdomain/zero/1.0/deed.de
 *
 *      https://creativecommons.org/licenses/by/2.0/
 *      https://creativecommons.org/licenses/by-sa/2.0/
 *      https://creativecommons.org/licenses/by/2.5/
 *      https://creativecommons.org/licenses/by-sa/2.5/
 *      https://creativecommons.org/licenses/by/3.0/
 *      https://creativecommons.org/licenses/by-sa/3.0/
 *      https://creativecommons.org/licenses/by/4.0/
 *      https://creativecommons.org/licenses/by-sa/4.0/
 *
 */
fun Fragment.showCcInfo(
    layoutInflater: LayoutInflater,
    owner: String,
    source: String,
    license: String,
    ownerLink: String?,
    context: Context
): AlertDialog {
    val dialogBuilder = MaterialAlertDialogBuilder(
        requireContext(),
        R.style.Naturblick_MaterialComponents_Dialog_Alert
    )
    val binding = DialogCcInfoBinding.inflate(layoutInflater)

    val text = "${
        textAndSourceAsLink(
            source,
            context
        )
    } $owner/ ${licenceToLink(license.trim())}${ownerLinkToLink(context, ownerLink)}"

    binding.ccInfoText.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
    binding.ccInfoText.movementMethod = LinkMovementMethod.getInstance()

    dialogBuilder
        .setNegativeButton(R.string.close) { d, _ ->
            d.cancel()
        }
        .setView(binding.root)
    return dialogBuilder.show()
}

fun Fragment.sendFeedback() {
    EmailIntentBuilder.from(requireActivity())
        .to("naturblick@mfn.berlin")
        .subject(
            requireContext().resources.getString(
                R.string.feedback_subject,
                BuildConfig.VERSION_NAME
            )
        )
        .body(
            requireContext().resources.getString(
                R.string.feedback_body,
                Build.MODEL,
                Build.VERSION.RELEASE,
                BuildConfig.VERSION_NAME
            )
        )
        .start()
}

fun Fragment.sendAccessibilityFeedback() {
    EmailIntentBuilder.from(requireActivity())
        .to("naturblick@mfn.berlin")
        .start()
}