/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import berlin.mfn.naturblick.backend.Coordinates
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.DoubleArraySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object CoordinatesSerializer : KSerializer<Coordinates> {
    override val descriptor = DoubleArraySerializer().descriptor

    override fun deserialize(decoder: Decoder): Coordinates {
        val coords = decoder.decodeSerializableValue(DoubleArraySerializer())
        if (coords.size == 2) {
            return Coordinates(coords[1], coords[0])
        } else {
            error("Expected array with latitude and longitude")
        }
    }

    override fun serialize(encoder: Encoder, value: Coordinates) {
        val coords = doubleArrayOf(value.lon, value.lat)
        encoder.encodeSerializableValue(DoubleArraySerializer(), coords)
    }
}
