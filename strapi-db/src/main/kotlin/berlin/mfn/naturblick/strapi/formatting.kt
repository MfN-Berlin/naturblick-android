/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.strapi

fun String.allowBreakOnHyphen(): String =
    replace("-", "\u200b-\u200b")