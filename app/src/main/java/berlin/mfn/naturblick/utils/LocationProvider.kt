package berlin.mfn.naturblick.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context.LOCATION_SERVICE
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource

interface LocationProvider {
    fun getCurrentLocation(
        activity: Activity,
        success: (location: Location) -> Unit,
        canceled: () -> Unit,
        error: () -> Unit
    )

    companion object {
        fun getProvider(activity: Activity): LocationProvider {
            return if (
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) ==
                ConnectionResult.SUCCESS
            ) {
                GoogleLocationProvider(LocationServices.getFusedLocationProviderClient(activity))
            } else {
                StandardLocationProvider(
                    (activity.getSystemService(LOCATION_SERVICE) as? LocationManager)!!
                )
            }
        }
    }
}

class GoogleLocationProvider(private val fusedLocationClient: FusedLocationProviderClient) :
    LocationProvider {

    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(
        activity: Activity,
        success: (location: Location) -> Unit,
        canceled: () -> Unit,
        error: () -> Unit
    ) {
        val locationTask = fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        )
        locationTask.addOnCompleteListener(activity) { task ->
            val location = task.result
            if (location != null) {
                success(location)
            } else {
                error()
            }
        }
        locationTask.addOnCanceledListener {
            canceled()
        }
        locationTask.addOnFailureListener {
            error()
        }
    }
}

class StandardLocationProvider(val service: LocationManager) : LocationProvider {

    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(
        activity: Activity,
        success: (location: Location) -> Unit,
        canceled: () -> Unit,
        error: () -> Unit
    ) {
        val provider = service.getBestProvider(
            Criteria().apply {
                accuracy = Criteria.ACCURACY_FINE
            },
            true
        )
        if (provider != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                service.getCurrentLocation(
                    provider,
                    CancellationSignal().apply {
                        setOnCancelListener(canceled)
                    },
                    activity.mainExecutor
                ) {
                    if (it != null) {
                        success(it)
                    } else {
                        error()
                    }
                }
            } else {
                service.requestSingleUpdate(
                    provider,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            success(location)
                        }

                        /* These methods needs to be overriden since they have no
                         * default implementation in older versions of android
                         */
                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                        override fun onStatusChanged(
                            provider: String?,
                            status: Int,
                            extras: Bundle?
                        ) {
                        }
                    },
                    activity.mainLooper
                )
            }
        } else {
            error()
        }
    }
}
