package berlin.mfn.naturblick.backend

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.utils.AndroidDeviceId
import okio.IOException
import retrofit2.HttpException

class RegisterDeviceWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        Log.i(TAG, "Register device")
        try {
            val deviceId = AndroidDeviceId.deviceId(applicationContext.contentResolver)
            PublicBackendApi.service.register(
                RegisterDevice(
                    deviceId,
                    Build.MODEL,
                    Build.VERSION.RELEASE,
                    BuildConfig.VERSION_NAME
                )
            )
            return Result.success()
        } catch (e: HttpException) {
            Log.e(TAG, "Failed to register device, will ask for retry", e)
            return Result.retry()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to register device, will ask for retry", e)
            return Result.retry()
        }
    }

    companion object {
        val JOB_ID = "berlin.mfn.naturblick.backend.RegisterDeviceWorker"
        val JOB_CONSTRAINTS = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        const val TAG = "RegisterDeviceWorker"
    }
}
