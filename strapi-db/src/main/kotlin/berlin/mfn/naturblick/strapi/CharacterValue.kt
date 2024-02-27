package berlin.mfn.naturblick.strapi

import kotlinx.serialization.Serializable
import java.sql.PreparedStatement

@Serializable
data class CharacterValue(
    val id: Int,
    val character: Character,
    val gername: String,
    val engname: String,
    val colors: String?,
    val image: ImageFile?,
    val dots: String?
)