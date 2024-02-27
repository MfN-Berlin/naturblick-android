package berlin.mfn.naturblick.ui.character

import berlin.mfn.naturblick.room.CharacterWithValues
import berlin.mfn.naturblick.ui.species.CharacterQuery

data class CharacterValuesState(
    val characters: List<CharacterWithValues>,
    val selected: Set<Int>
) {
    val query: CharacterQuery get() {
        val selectedCharacters = characters.filter {
            it.values.any { selected.contains(it.id) }
        }
        val query = selectedCharacters.flatMap { character ->
            val selectedValues = character.values.filter { selected.contains(it.id) }.size
            character.values.map {
                if (selected.contains(it.id)) {
                    Pair(it.id, 100.0f / selectedValues.toFloat())
                } else {
                    Pair(it.id, 0f)
                }
            }
        }
        return CharacterQuery(selectedCharacters.size, query)
    }
}
