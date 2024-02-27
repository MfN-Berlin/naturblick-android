package berlin.mfn.naturblick.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ZonedDateTimeSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val dateTimeJson = "\"2007-12-03T10:15:30+01:00[Europe/Paris]\""
    private val dateTime = ZonedDateTime.of(
        2007,
        12,
        3,
        10,
        15,
        30,
        0,
        ZoneId.of("Europe/Paris")
    )

    @Test
    fun decodeZonedDateTime_isCorrect() {
        assertEquals(json.decodeFromString(ZonedDateTimeSerializer, dateTimeJson), dateTime)
    }

    @Test
    fun encodeZonedDateTime_isCorrect() {
        assertEquals(json.encodeToString(ZonedDateTimeSerializer, dateTime), dateTimeJson)
    }
}
