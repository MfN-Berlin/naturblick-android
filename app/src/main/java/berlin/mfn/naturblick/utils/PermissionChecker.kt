package berlin.mfn.naturblick.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

data class Permission(val permission: String, val missing: Int)

interface RequiredPermissionCallback {
    fun requiredPermissionGranted(): Unit
}

sealed interface RequiredPermissionChecker {

    fun requirePermission(context: Context, success: RequiredPermissionCallback)

    companion object {
        fun register(
            fragment: Fragment,
            permission: String,
            missing: Int,
            success: RequiredPermissionCallback
        ): RequiredPermissionChecker {
            val launcher =
                fragment.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        success.requiredPermissionGranted()
                    } else {
                        fragment.requireActivity().setResult(Activity.RESULT_CANCELED)
                        fragment.requireActivity().finish()
                        Toast.makeText(
                            fragment.requireContext(),
                            missing,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            return SingleRequiredPermissionChecker(permission, launcher)
        }

        fun register(
            fragment: Fragment,
            permissions: List<Permission>,
            success: RequiredPermissionCallback
        ): RequiredPermissionChecker {
            val launcher = fragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { granted ->
                val notGranted = permissions.filter {
                    !granted.getOrDefault(it.permission, false)
                }

                if (notGranted.isEmpty()) {
                    success.requiredPermissionGranted()
                } else {
                    val toastString = notGranted.joinToString("\n") {
                        fragment.requireContext().resources.getString(it.missing)
                    }
                    fragment.requireActivity().setResult(Activity.RESULT_CANCELED)
                    fragment.requireActivity().finish()
                    Toast.makeText(
                        fragment.requireContext(),
                        toastString,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            return MultiplePermissionsCheckerRequired(permissions, launcher)
        }
    }
}

object IdentityRequiredPermissionChecker : RequiredPermissionChecker {
    override fun requirePermission(context: Context, success: RequiredPermissionCallback) {
        success.requiredPermissionGranted()
    }
}

class SingleRequiredPermissionChecker internal constructor(
    private val permission: String,
    private val launcher: ActivityResultLauncher<String>
) : RequiredPermissionChecker {
    override fun requirePermission(context: Context, success: RequiredPermissionCallback) {
        when (
            ContextCompat.checkSelfPermission(
                context,
                permission
            )
        ) {
            PackageManager.PERMISSION_DENIED ->
                launcher.launch(permission)
            else -> success.requiredPermissionGranted()
        }
    }
}

class MultiplePermissionsCheckerRequired internal constructor(
    private val permissions: List<Permission>,
    private val launcher: ActivityResultLauncher<Array<String>>
) : RequiredPermissionChecker {
    override fun requirePermission(context: Context, success: RequiredPermissionCallback) {
        if (permissions.fold(true) { acc, permission ->
            acc && (
                ActivityCompat.checkSelfPermission(
                        context,
                        permission.permission
                    ) == PackageManager.PERMISSION_GRANTED
                )
        }
        ) {
            success.requiredPermissionGranted()
        } else {
            launcher.launch(
                permissions.map {
                    it.permission
                }.toTypedArray()
            )
        }
    }
}

fun Fragment.registerRequiredPermission(
    permission: String,
    missing: Int,
    success: RequiredPermissionCallback
): RequiredPermissionChecker =
    RequiredPermissionChecker.register(this, permission, missing, success)

fun Fragment.registerRequiredPermission(
    permissions: List<Permission>,
    success: RequiredPermissionCallback
): RequiredPermissionChecker =
    RequiredPermissionChecker.register(this, permissions, success)

interface RequestedPermissionsCallback {
    fun permissionResult(granted: Boolean, permissions: List<String>): Unit
}

interface RequestedPermissionCallback : RequestedPermissionsCallback {
    override fun permissionResult(granted: Boolean, permissions: List<String>) {
        permissionResult(granted)
    }
    fun permissionResult(granted: Boolean): Unit
}

class PermissionChecker internal constructor(
    private val permission: String,
    private val launcher: ActivityResultLauncher<String>
) {

    fun checkPermission(context: Context, result: RequestedPermissionsCallback) {
        when (
            ContextCompat.checkSelfPermission(
                context,
                permission
            )
        ) {
            PackageManager.PERMISSION_DENIED ->
                launcher.launch(permission)
            else -> result.permissionResult(true, listOf(permission))
        }
    }

    companion object {
        fun register(
            fragment: Fragment,
            permission: String,
            result: RequestedPermissionsCallback
        ): PermissionChecker {
            val launcher =
                fragment.registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    result.permissionResult(isGranted, listOf(permission))
                }
            return PermissionChecker(permission, launcher)
        }
    }
}

fun Fragment.registerRequestedPermission(
    permission: String,
    result: RequestedPermissionsCallback
) =
    PermissionChecker.register(this, permission, result)

class OneOfPermissionChecker internal constructor(
    private val permissions: List<String>,
    private val launcher: ActivityResultLauncher<Array<String>>
) {
    fun checkPermission(context: Context, success: RequestedPermissionsCallback) {
        val granted = permissions.filter { permission ->
            ActivityCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        if (granted.isNotEmpty()) {
            success.permissionResult(true, granted)
        } else {
            launcher.launch(
                permissions.toTypedArray()
            )
        }
    }

    companion object {
        fun register(
            fragment: Fragment,
            permissions: List<String>,
            result: RequestedPermissionsCallback
        ): OneOfPermissionChecker {
            val launcher = fragment.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { granted ->
                val grantedList = permissions.filter {
                    granted.getOrDefault(it, false)
                }

                result.permissionResult(grantedList.isNotEmpty(), grantedList)
            }
            return OneOfPermissionChecker(permissions, launcher)
        }
    }
}

fun Fragment.registerRequestedOneOfPermission(
    permissions: List<String>,
    result: RequestedPermissionsCallback
) =
    OneOfPermissionChecker.register(this, permissions, result)
