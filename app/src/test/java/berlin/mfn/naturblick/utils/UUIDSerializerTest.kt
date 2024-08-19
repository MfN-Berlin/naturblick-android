/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import java.util.*
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class UUIDSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val uuidString = "123e4567-e89b-12d3-a456-426614174000"
    private val uuidJson = "\"${uuidString}\""
    private val uuid = UUID.fromString(uuidString)

    @Test
    fun decodeUUID_isCorrect() {
        assertEquals(json.decodeFromString(UUIDSerializer, uuidJson), uuid)
    }

    @Test
    fun encodeUUID_isCorrect() {
        assertEquals(json.encodeToString(UUIDSerializer, uuid), uuidJson)
    }
}
