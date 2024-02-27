package berlin.mfn.naturblick.utils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager
import berlin.mfn.naturblick.BuildConfig
import berlin.mfn.naturblick.NaturblickApplication
import berlin.mfn.naturblick.room.Species
import berlin.mfn.naturblick.ui.data.Group
import berlin.mfn.naturblick.ui.species.CharacterQuery
import com.snowplowanalytics.snowplow.Snowplow
import com.snowplowanalytics.snowplow.configuration.EmitterConfiguration
import com.snowplowanalytics.snowplow.configuration.NetworkConfiguration
import com.snowplowanalytics.snowplow.configuration.SessionConfiguration
import com.snowplowanalytics.snowplow.configuration.TrackerConfiguration
import com.snowplowanalytics.snowplow.controller.TrackerController
import com.snowplowanalytics.snowplow.emitter.BufferOption
import com.snowplowanalytics.snowplow.event.Structured
import com.snowplowanalytics.snowplow.network.HttpMethod
import com.snowplowanalytics.snowplow.util.TimeMeasure
import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Display(val width: Int, val height: Int)

object AnalyticsTracker {
    fun createTracker(context: Context): TrackerController {
        val deviceId = AndroidDeviceId.deviceId(context.contentResolver)
        val networkConfig = NetworkConfiguration(BuildConfig.ANALYTICS_URL, HttpMethod.POST)
        val emitterConfiguration = EmitterConfiguration().apply {
            // Buffer up to 25 events to reduce traffic
            bufferOption = BufferOption.HeavyGroup
        }
        val trackerConfig: TrackerConfiguration = TrackerConfiguration(deviceId)
            .sessionContext(true)
            .platformContext(true)
            .lifecycleAutotracking(false)
            .screenViewAutotracking(true)
            .screenContext(true)
            .applicationContext(true)
            .exceptionAutotracking(true)
            .installAutotracking(true)
            .userAnonymisation(false)

        val sessionConfig = SessionConfiguration(
            TimeMeasure(15, TimeUnit.MINUTES),
            TimeMeasure(15, TimeUnit.MINUTES)
        )

        return Snowplow.createTracker(
            context,
            "androidTracker",
            networkConfig,
            trackerConfig,
            sessionConfig,
            emitterConfiguration
        )
    }

    fun trackInitEvent(context: Context, tracker: TrackerController) {
        val metrics = DisplayMetrics()

        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay.getMetrics(metrics)

        val thisDisplay = Display(width = metrics.widthPixels, height = metrics.heightPixels)

        val event = Structured("Init", "startNaturblick")
            .label(BuildConfig.VERSION_NAME)
            .property(Json.encodeToString(thisDisplay))

        tracker.track(event)
    }

    fun trackSpeciesGroup(app: NaturblickApplication, group: String) {
        val event = Structured("UI", "groupSpeciesList")
            .label("speciesGroup")
            .property(group)
        app.tracker.track(event)
    }

    fun trackMKey(app: NaturblickApplication, chars: CharacterQuery) {
        val selection: List<Int> = chars.query
            .filter { it.second > 0.0 }
            .map { it.first }

        val event = Structured("UI", "resultSpeciesList")
            .label("MKey")
            .property(Json.encodeToString(selection))
        app.tracker.track(event)
    }

    fun trackWhichSpeciesMkey(app: NaturblickApplication, group: Group) {
        val event = Structured("UI", "pickSpeciesMKey")
            .label(group.id)
            .property(group.name)
        app.tracker.track(event)
    }

    fun trackWhichSpeciesGroup(app: NaturblickApplication, group: Group) {
        val event = Structured("UI", "pickSpeciesGroup")
            .label(group.id)
            .property(group.name)
        app.tracker.track(event)
    }

    fun trackPortrait(app: NaturblickApplication, species: Species) {
        val event = Structured("UI", "speciesPortrait")
            .label(species.id.toString())
            .property(species.sciname)
            .value(species.id.toDouble())
        app.tracker.track(event)
    }

    fun trackSpeciesPortraitSound(app: NaturblickApplication, url: String, species: Species) {
        val event = Structured("UI", "playSpeciesPortraitSound")
            .property(url)
            .value(species.id.toDouble())
        app.tracker.track(event)
    }
}
