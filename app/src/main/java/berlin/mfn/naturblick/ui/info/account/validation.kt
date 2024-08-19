/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.ui.info.account

import android.text.TextUtils
import android.util.Patterns
import berlin.mfn.naturblick.R

fun validatePassword(password: String): Pair<Boolean?, Int?> {
    val passwordIsEmpty = TextUtils.isEmpty(password)
    val passwordIsTooShort = password.length < 9
    val passwordContainsNoLowerCaseLetters = password.find { it.isLowerCase() } == null
    val passwordContainsNoUpperCaseLetters = password.find { it.isUpperCase() } == null
    val passwordContainsNoDigits = password.find { it.isDigit() } == null
    return if (passwordIsEmpty) {
        Pair(null, null)
    } else if (passwordIsTooShort) {
        Pair(false, R.string.password_too_short)
    } else if (passwordContainsNoLowerCaseLetters) {
        Pair(false, R.string.password_no_lower_case)
    } else if (passwordContainsNoUpperCaseLetters) {
        Pair(false, R.string.password_no_upper_case)
    } else if (passwordContainsNoDigits) {
        Pair(false, R.string.password_no_digits)
    } else {
        Pair(true, null)
    }
}

fun validateEmail(email: String): Boolean? {
    val emailIsEmpty = TextUtils.isEmpty(email)
    val emailIsInvalid = !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    return if (emailIsEmpty) {
        null
    } else !emailIsInvalid
}
