package berlin.mfn.naturblick.utils

import berlin.mfn.naturblick.backend.CreateOperation
import berlin.mfn.naturblick.backend.DeleteOperation
import berlin.mfn.naturblick.backend.ObsType
import berlin.mfn.naturblick.backend.PatchOperation
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ObservationOperationSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val uuidString = "123e4567-e89b-12d3-a456-426614174000"
    private val uuidJson = "\"${uuidString}\""
    private val uuid = UUID.fromString(uuidString)
    private val dateTimeJson = "\"2007-12-03T10:15:30+01:00[Europe/Paris]\""
    private val dateTime = ZonedDateTime.of(2007, 12, 3, 10, 15, 30, 0, ZoneId.of("Europe/Paris"))

    @Test
    fun encode_PatchOperation_isCorrect() {
        val patchOperationJson = """{"operation":"patch","data":{
            |"occurenceId":$uuidJson,
            |"newSpeciesId":1}
            |}""".trimMargin().replace("\n", "")
        assertEquals(
            json.encodeToString(
                ObservationOperationSerializer,
                PatchOperation(
                    uuid, 1, null, null, null,
                    null, null
                )
            ),
            patchOperationJson
        )
    }

    @Test
    fun encode_DeleteOperation_isCorrect() {
        val deleteOperationJson = """{"operation":"delete","data":{"occurenceId":$uuidJson}}"""
        assertEquals(
            json.encodeToString(
                ObservationOperationSerializer,
                DeleteOperation(uuid, -1)
            ),
            deleteOperationJson
        )
    }

    @Test
    fun encode_CreateOperation_isCorrect() {
        val createOperationJson =
            """{"operation":"create","data":{
                |"occurenceId":$uuidJson,"created":$dateTimeJson,"speciesId":null,
                |"ccByName":"","obsType":"manual",
                |"deviceIdentifier":"someDeviceIdentifier","appVersion":"1"
                |,"imported":false}
                |}""".trimMargin().replace("\n", "")
        assertEquals(
            json.encodeToString(
                ObservationOperationSerializer,
                CreateOperation(
                    uuid, dateTime, null, "", ObsType.MANUAL,
                    null, appVersion = "1", deviceIdentifier = "someDeviceIdentifier",
                    imported = false
                )
            ),
            createOperationJson
        )
    }
}
