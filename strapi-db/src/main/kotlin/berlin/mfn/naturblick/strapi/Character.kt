package berlin.mfn.naturblick.strapi

import kotlinx.serialization.Serializable
import java.sql.PreparedStatement

@Serializable
data class Character(
    val id: Int,
    val gername: String,
    val engname: String,
    val group: String,
    val weight: Int,
    val singleChoice: Boolean?,
    val gerdescription: String? = null,
    val engdescription: String? = null
)