package berlin.mfn.naturblick.strapi

fun String.allowBreakOnHyphen(): String =
    replace("-", "\u200b-\u200b")