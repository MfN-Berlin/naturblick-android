/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.strapi

import kotlinx.serialization.Serializable
import java.sql.PreparedStatement

@Serializable
data class CharacterValue(
    val id: Int,
    val character_id: Int,
    val gername: String,
    val engname: String,
    val colors: String?,
    val image: String?,
    val dots: String?
)