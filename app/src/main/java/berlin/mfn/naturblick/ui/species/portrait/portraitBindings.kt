package berlin.mfn.naturblick.ui.species.portrait

import android.content.Context
import android.widget.TextView
import androidx.databinding.BindingAdapter
import berlin.mfn.naturblick.room.StrapiDb
import berlin.mfn.naturblick.utils.languageId
import kotlinx.coroutines.runBlocking

@BindingAdapter("sources")
fun sourcesBinding(view: TextView, sources: String?) = view.apply {
    sources?.let {
        val sourcePlaceholders = sourcePlaceholders(context)
        text = sourcePlaceholders.fold(it) { acc, e ->
            acc.replace(e.first, e.second)
        }
    }
}

fun sourcePlaceholders(context: Context): List<Pair<String, String>> {
    return runBlocking {
        StrapiDb.getDb(context).sourcesTranslationsDao().getSourcesTranslations(languageId())
            .map {
                Pair(it.key, it.value)
            }
    }
}
