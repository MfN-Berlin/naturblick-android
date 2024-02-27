package berlin.mfn.naturblick.utils

import java.util.*

const val GERMAN = "de"

const val GERMAN_ID = 1
const val ENGLISH_ID = 2

fun isGerman(): Boolean {
    return Locale.getDefault().language == GERMAN
}

fun languageId(): Int {
    return if (Locale.getDefault().language == GERMAN) {
        GERMAN_ID
    } else {
        ENGLISH_ID
    }
}
