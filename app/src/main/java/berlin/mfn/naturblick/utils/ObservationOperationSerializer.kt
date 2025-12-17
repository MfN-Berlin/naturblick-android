/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.utils

import berlin.mfn.naturblick.backend.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

object ObservationOperationSerializer : KSerializer<ObservationOperation> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("ObservationOperation") {
            element<String>("operation")
            element<Unit>("data")
        }

    override fun serialize(encoder: Encoder, value: ObservationOperation) {
        when (value) {
            is PatchOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "patch")
                encodeSerializableElement(descriptor, 1, PatchOperation.serializer(), value)
            }
            is DeleteOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "delete")
                encodeSerializableElement(descriptor, 1, DeleteOperation.serializer(), value)
            }
            is CreateOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "create")
                encodeSerializableElement(descriptor, 1, CreateOperation.serializer(), value)
            }
            is UploadMediaOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "upload_media")
                encodeSerializableElement(descriptor, 1, UploadMediaOperation.serializer(), value)
            }
            is UploadThumbnailMediaOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "upload_media")
                encodeSerializableElement(
                    descriptor,
                    1,
                    UploadThumbnailMediaOperation.serializer(),
                    value
                )
            }
            is ViewFieldbookOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "view_fieldbook")
                encodeSerializableElement(descriptor, 1, ViewFieldbookOperation.serializer(), value)
            }
            is ViewPortraitOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "view_portrait")
                encodeSerializableElement(descriptor, 1, ViewPortraitOperation.serializer(), value)
            }
            is ViewCharactersOperation -> encoder.encodeStructure(descriptor) {
                encodeStringElement(descriptor, 0, "view_characters")
                encodeSerializableElement(descriptor, 1, ViewCharactersOperation.serializer(), value)
            }
        }
    }

    override fun deserialize(decoder: Decoder): ObservationOperation {
        error("Decoding of operations is not supported")
    }
}
