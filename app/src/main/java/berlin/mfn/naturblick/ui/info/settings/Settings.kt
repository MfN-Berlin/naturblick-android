package berlin.mfn.naturblick.ui.info.settings

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openDatabase
import android.database.sqlite.SQLiteException
import android.os.Build
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import androidx.appcompat.app.AlertDialog
import berlin.mfn.naturblick.R
import berlin.mfn.naturblick.databinding.DialogCcbyBinding
import berlin.mfn.naturblick.databinding.DialogPolicyBinding
import berlin.mfn.naturblick.utils.AndroidDeviceId
import java.io.File
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Settings {

    private val jsonDecoder = Json { ignoreUnknownKeys = true }

    private fun importOldUserData(sp: SharedPreferences, activity: Activity, done: () -> Unit) {
        Thread {
            val dbFile = File(activity.filesDir, "user.db").toString()
            val oldUserData = try {
                openDatabase(dbFile, null, SQLiteDatabase.OPEN_READONLY).use { db ->
                    db.rawQuery(
                        """SELECT json FROM "by-sequence" ORDER BY rowid DESC LIMIT 1;""",
                        null
                    ).use { cursor ->
                        if (cursor.moveToFirst())
                            cursor.getString(0)
                        else
                            null
                    }
                }?.let { json ->
                    jsonDecoder.decodeFromString<OldUserData>(json)
                }
            } catch (e: SQLiteException) {
                null
            } catch (e: SerializationException) {
                null
            }

            if (oldUserData != null) {
                if (oldUserData.name != null && oldUserData.name != "MfN Naturblick")
                    setCcBy(sp, oldUserData.name, activity)

                if (oldUserData.policy != null && oldUserData.policy) {
                    approvePolicy(sp)
                }
            }

            activity.runOnUiThread {
                done()
            }
        }.start()
    }

    private fun hasSeenAccountInfo(sp: SharedPreferences): Boolean {
        return sp.getBoolean(accountInfoKey, false)
    }

    private fun seenAccountInfoPolicy(sp: SharedPreferences) {
        with(sp.edit()) {
            putBoolean(accountInfoKey, true)
            apply()
        }
    }

    private fun approvePolicy(sp: SharedPreferences) {
        with(sp.edit()) {
            putBoolean(policyPreferenceKey, true)
            apply()
        }
    }

    private fun setCcBy(sp: SharedPreferences, ccBy: String, activity: Activity) {
        with(sp.edit()) {
            putString(ccByPreferenceKey, ccBy)
            apply()
        }
        activity.runOnUiThread {
            Toast.makeText(
                activity.applicationContext,
                activity.applicationContext.resources.getString(
                    R.string.cc_by_was_set_to_default,
                    ccBy
                ),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isCcBy(sp: SharedPreferences): Boolean {
        return sp.getString(ccByPreferenceKey, null) != null
    }

    private fun isPolicyApproved(sp: SharedPreferences): Boolean {
        return sp.getBoolean(policyPreferenceKey, false)
    }

    fun getEmail(context: Context): String? {
        return getEmail(sharedPreferences(context))
    }

    fun getEmail(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(emailKey, null)
    }

    fun setEmailAndRequireSignIn(context: Context, email: String) {
        sharedPreferences(context).edit().apply {
            putString(emailKey, email)
            putBoolean(neverSignedInKey, true)
            apply()
        }
    }

    fun didNeverSignIn(sp: SharedPreferences): Boolean =
        sp.getBoolean(neverSignedInKey, false)

    fun getToken(context: Context): String? {
        return getToken(sharedPreferences(context))
    }

    fun getToken(sharedPreferences: SharedPreferences): String? {
        return sharedPreferences.getString(tokenKey, null)
    }

    fun setToken(context: Context, token: String, email: String) {
        sharedPreferences(context).edit().apply {
            putString(tokenKey, token)
            putString(emailKey, email)
            remove(neverSignedInKey)
            apply()
        }
        AndroidDeviceId.invalidateToken()
    }

    fun setSignedOut(context: Context) {
        sharedPreferences(context).edit().apply {
            remove(tokenKey)
            apply()
        }
        AndroidDeviceId.invalidateToken()
    }

    fun canSync(context: Context): Boolean {
        val sp = sharedPreferences(context)
        return getToken(sp) != null || getEmail(sp) == null
    }

    fun clearEmail(context: Context) {
        sharedPreferences(context).edit().apply {
            remove(tokenKey)
            remove(emailKey)
            remove(neverSignedInKey)
            apply()
        }
        AndroidDeviceId.invalidateToken()
    }

    fun getAllDeviceIds(context: Context, deviceIdentifier: String): Set<String> {
        val sp = sharedPreferences(context)
        val allExistingIds = sp.getStringSet(allDeviceIdsKey, null)?.toSet()
        val allIds = if (allExistingIds == null) {
            setOf(deviceIdentifier)
        } else if (!allExistingIds.contains(deviceIdentifier)) {
            allExistingIds + deviceIdentifier
        } else {
            allExistingIds
        }
        if (allIds != allExistingIds) {
            sp.edit().apply {
                putStringSet(allDeviceIdsKey, allIds)
                putString(deviceIdNamePrefix + deviceIdentifier, Build.MODEL)
                apply()
            }
        }
        return allIds
    }

    fun getLinkedDevices(context: Context, deviceIdentifier: String): List<String> {
        val sp = sharedPreferences(context)
        val allExistingIds = sp.getStringSet(allDeviceIdsKey, null)?.toSet()
        val currentName = listOf(Build.MODEL)
        return if (allExistingIds != null) {
            val linkedIds = allExistingIds - deviceIdentifier
            currentName + linkedIds.map {
                sp.getString(
                    deviceIdNamePrefix + it,
                    context.resources.getString(R.string.unknown_device)
                )!!
            }
        } else {
            currentName
        }
    }

    fun setCcBy(activity: Activity, ccBy: String) {
        setCcBy(sharedPreferences(activity.applicationContext), ccBy, activity)
    }

    fun checkCcBy(activity: Activity, layoutInflater: LayoutInflater, success: () -> Unit) {
        val sp = sharedPreferences(activity)
        if (isCcBy(sp)) {
            success()
        } else {
            importOldUserData(sp, activity) {
                if (isCcBy(sp)) {
                    success()
                } else {
                    resetCcBy(activity, layoutInflater, sp, success)
                }
            }
        }
    }

    fun getCcBy(context: Context): String {
        return sharedPreferences(context)
            .getString(ccByPreferenceKey, context.getString(R.string.cc_by_default))!!
    }

    private fun checkAccountInfo(context: Context, goToSignUp: () -> Unit) {
        val sp = sharedPreferences(context)
        if (!hasSeenAccountInfo(sp)) {
            showAccountInfo(context, sp, goToSignUp)
        }
    }

    fun sharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
    }

    private fun resetCcBy(
        activity: Activity,
        layoutInflater: LayoutInflater,
        sp: SharedPreferences,
        success: () -> Unit
    ) {
        val ccByDialogBuild = AlertDialog.Builder(activity)

        val binding = DialogCcbyBinding.inflate(layoutInflater)
        val editText = binding.editCcBy

        ccByDialogBuild
            .setTitle(R.string.authorship)
            .setMessage(R.string.cc_by_msg)
            .setPositiveButton(R.string.save) { _, _ ->
                val ccBy = editText.text.toString()
                setCcBy(sp, ccBy, activity)
                success()
            }
            .setOnCancelListener {
                val ccBy = editText.text.toString()
                setCcBy(sp, ccBy, activity)
                success()
            }
            .setView(binding.root)
        ccByDialogBuild.show()
    }

    private fun validatePolicy(
        context: Context,
        layoutInflater: LayoutInflater,
        sp: SharedPreferences,
        failed: () -> Unit,
        success: () -> Unit
    ) {
        val dialogBuilder = AlertDialog.Builder(context)

        val binding = DialogPolicyBinding.inflate(layoutInflater)

        dialogBuilder
            .setPositiveButton(R.string.accept) { _, _ ->
                approvePolicy(sp)
                success()
            }
            .setNegativeButton(R.string.decline) { _, _ ->
                failed()
            }
            .setOnCancelListener {
                failed()
            }
            .setView(binding.root)
        dialogBuilder.show()
    }

    private fun showAccountInfo(
        context: Context,
        sp: SharedPreferences,
        goToSignUp: () -> Unit
    ) {
        seenAccountInfoPolicy(sp)
        AlertDialog.Builder(context).apply {
            setTitle(R.string.set_up_an_account)
            setMessage(R.string.account_info)
            setPositiveButton(R.string.yes) { _, _ ->
                goToSignUp()
            }
            setNegativeButton(R.string.no) { _, _ ->
            }
            setOnCancelListener {
            }
        }.show()
    }

    fun check(
        activity: Activity,
        layoutInflater: LayoutInflater,
        failed: () -> Unit,
        goToSignUp: () -> Unit
    ) {
        val sp = sharedPreferences(activity)
        if (!isPolicyApproved(sp)) {
            importOldUserData(sp, activity) {
                if (!isPolicyApproved(sp)) {
                    validatePolicy(activity, layoutInflater, sp, failed) {
                        checkAccountInfo(activity, goToSignUp)
                    }
                } else {
                    checkAccountInfo(activity, goToSignUp)
                }
            }
        } else {
            checkAccountInfo(activity, goToSignUp)
        }
    }

    fun hasSeenImportInfo(context: Context): Boolean =
        sharedPreferences(context).getBoolean(importInfoKey, false)

    fun didSeeImportInfo(context: Context) {
        sharedPreferences(context).edit().apply {
            putBoolean(importInfoKey, true)
            apply()
        }
    }

    private const val ccByPreferenceKey = "ccBy"
    private const val policyPreferenceKey = "policy"
    private const val accountInfoKey = "account-info"
    private const val allDeviceIdsKey = "all-device-ids"
    private const val deviceIdNamePrefix = "device-id-name-"
    private const val tokenKey = "current-token"
    private const val emailKey = "current-email"
    private const val neverSignedInKey = "never-signed-in"
    private const val importInfoKey = "import-info"
}
