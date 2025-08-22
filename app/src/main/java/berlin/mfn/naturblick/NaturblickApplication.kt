/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick

import android.app.Application
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import berlin.mfn.naturblick.backend.RegisterDeviceWorker
import berlin.mfn.naturblick.ui.data.GroupRepo
import berlin.mfn.naturblick.utils.AnalyticsTracker
import com.snowplowanalytics.snowplow.controller.TrackerController

class NaturblickApplication : Application() {

    private lateinit var _tracker: TrackerController

    val tracker: TrackerController get() = _tracker

    override fun onCreate() {
        super.onCreate()
        registerDevice()
        _tracker = AnalyticsTracker.createTracker(applicationContext)
        AnalyticsTracker.trackInitEvent(applicationContext, tracker)
    }

    private fun registerDevice() {
        val registerWorkRequest = OneTimeWorkRequestBuilder<RegisterDeviceWorker>()
            .setConstraints(RegisterDeviceWorker.JOB_CONSTRAINTS)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            RegisterDeviceWorker.JOB_ID,
            ExistingWorkPolicy.REPLACE,
            registerWorkRequest
        )
    }
}
