/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.imprint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.fragment.app.Fragment
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.room.SourcesImprint
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.ui.composable.NaturblickTheme

data class SourceItem(val text: String, val link: String?)
data class AccordionSection(val title: String, val items: List<SourceItem>)

class SourcesImprintFragment : Fragment() {

    @Composable
    fun translateTitleIdentifier(titleIdentifier: String): String {
        return when (titleIdentifier) {
            "ident_keys" -> {
                stringResource(R.string.ident_keys)
            }

            "sound_recogniotion_sounds" -> {
                stringResource(R.string.sound_recogniotion_sound)
            }

            else -> {
                stringResource(R.string.sound_recogniotion_images)
            }
        }
    }


    @Composable
    fun AccordionItem(
        title: String,
        contentItems: List<SourceItem>,
        isExpanded: Boolean,
        onClick: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .background(
                    NaturblickTheme.colors.secondary,
                    RoundedCornerShape(dimensionResource(R.dimen.half_margin))
                )
                .border(
                    dimensionResource(R.dimen.hr_height),
                    NaturblickTheme.colors.onSecondaryHighEmphasis,
                    RoundedCornerShape(dimensionResource(R.dimen.half_margin))
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(dimensionResource(R.dimen.default_margin)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = translateTitleIdentifier(title),
                    modifier = Modifier.weight(1f),
                    color = NaturblickTheme.colors.onSecondaryHighEmphasis
                )
                Icon(
                    imageVector = if (isExpanded)
                        Icons.Default.KeyboardArrowUp
                    else
                        Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = NaturblickTheme.colors.onSecondaryHighEmphasis
                )
            }

            if (isExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.8f)
                        .verticalScroll(rememberScrollState())
                        .padding(dimensionResource(R.dimen.default_margin))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.half_margin))) {
                        contentItems.forEach { item ->
                            Text(
                                buildAnnotatedString {
                                    append(item.text)
                                    item.link?.let {
                                        withLink(
                                            LinkAnnotation.Url(
                                                item.link,
                                                TextLinkStyles(style = SpanStyle(color = NaturblickTheme.colors.secondaryVariant))
                                            )
                                        ) {
                                            append(" ${item.link}")
                                        }
                                    }
                                },
                                color = NaturblickTheme.colors.onSecondaryHighEmphasis
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun AccordionList(
        sections: List<AccordionSection>
    ) {
        var expandedIndex by remember { mutableStateOf<Int?>(null) }

        Column(modifier = Modifier.padding(dimensionResource(R.dimen.default_margin))) {
            sections.forEachIndexed { index, section ->
                AccordionItem(
                    title = section.title,
                    contentItems = section.items,
                    isExpanded = expandedIndex == index,
                    onClick = {
                        expandedIndex = if (expandedIndex == index) null else index
                    }
                )
            }
        }
    }


    fun SourcesImprint.textForSource(): String {
        return "$author $licence $scieName"
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sourcesDao = StrapiDb.getDb(requireContext()).sourcesImprintDao()
        return ComposeView(requireContext()).apply {
            setContent {
                val sources by sourcesDao.getSourcesImprint().collectAsState(emptyList())
                val sections = sources.groupBy {
                    it.section
                }.map { section ->
                    AccordionSection(
                        title = section.key,
                        items = section.value.map {
                            SourceItem(
                                it.textForSource(),
                                it.imageSource
                            )
                        })
                }

                NaturblickTheme {
                    AccordionList(sections)
                }
            }
        }
    }
}
