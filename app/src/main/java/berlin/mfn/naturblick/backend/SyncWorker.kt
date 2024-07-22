package berlin.mfn.naturblick.backend

import android.content.Context
import android.util.Log
import androidx.work.*
import berlin.mfn.naturblick.ui.info.settings.Settings
import okio.IOException
import retrofit2.HttpException

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            Log.i(TAG, "Start observation sync")
            PublicBackendApi.service.triggerSync(applicationContext)
            Log.i(TAG, "Successfully synced observations")
            Result.success()
        } catch (e: HttpException) {
            val code = e.code()
            if (code in 500..599) {
                Log.e(TAG, "Failed to sync observations. 5xx error code will ask for retry", e)
                Result.retry()
            } else {
                Log.e(
                    TAG,
                    "Failed to sync observations. Non 5xx error code will NOT ask for retry",
                    e
                )
                if (e.code() == 401) {
                    Settings.setSignedOut(applicationContext)
                }
                throw e
            }
        } catch (e: IOException) {
            Log.e(SyncWorker.TAG, "Failed to sync observations, will ask for retry", e)
            Result.retry()
        }
    }

    companion object {
        private const val JOB_ID = "berlin.mfn.naturblick.backend.SyncWorker"
        private val JOB_CONSTRAINTS = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        const val TAG = "SyncWorker"

        fun triggerBackgroundSync(context: Context, onSignedOut: (() -> Unit)? = null) {
            if (!Settings.canSync(context)) {
                onSignedOut?.let {
                    it()
                }
            } else {
                val registerWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(JOB_CONSTRAINTS)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    JOB_ID,
                    ExistingWorkPolicy.REPLACE,
                    registerWorkRequest
                )
            }
        }
    }
}
