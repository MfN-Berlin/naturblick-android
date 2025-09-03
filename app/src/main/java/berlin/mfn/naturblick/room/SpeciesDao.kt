/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import androidx.paging.PagingSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import berlin.mfn.naturblick.utils.ENGLISH_ID
import berlin.mfn.naturblick.utils.GERMAN_ID

@Dao
interface SpeciesDao {
    @Query("SELECT count(*) FROM species")
    suspend fun getSpeciesCount(): Long

    @Query("SELECT * FROM species WHERE rowid = :id")
    suspend fun getSpecies(id: Int): Species

    @Query("SELECT * FROM species WHERE rowid = (SELECT COALESCE(s2.rowid, s1.rowid) FROM species AS s1 LEFT JOIN species AS s2 ON s1.accepted = s2.rowid WHERE s1.rowid = :id)")
    suspend fun getAcceptedSpecies(id: Int): Species

    @Query("SELECT * FROM species WHERE rowid = (SELECT COALESCE(s2.rowid, s1.rowid) FROM species AS s1 LEFT JOIN species AS s2 ON s1.accepted = s2.rowid WHERE s1.old_species_id = :oldSpeciesId)")
    suspend fun getSpecies(oldSpeciesId: String): Species

    @Query("SELECT rowid AS id, group_id AS `group` FROM species WHERE rowid in (:ids)")
    suspend fun getGroups(ids: List<Int>): List<SpeciesWithGroup>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """SELECT species.*, NULL as female FROM species INNER JOIN portrait ON portrait.species_id = species.rowid 
        WHERE group_id = :group 
        AND (:query IS NULL 
            OR (:language = $GERMAN_ID AND (gersearchfield LIKE :query)) 
            OR (:language = $ENGLISH_ID AND (engsearchfield LIKE :query)) 
            OR sciname LIKE :query)
        AND portrait.language = :language AND gersearchfield IS NOT NULL
        ORDER BY
            CASE WHEN :language = $GERMAN_ID THEN gername IS NULL ELSE engname IS NULL END, 
            CASE WHEN :language = $GERMAN_ID THEN gername ELSE engname END, 
            CASE WHEN :language = $GERMAN_ID THEN gersynonym IS NULL ELSE engsynonym IS NULL END,
            CASE WHEN :language = $GERMAN_ID THEN gersynonym ELSE engsynonym END,
            sciname
            """
    )
    fun filterSpeciesWithPortrait(
        group: String,
        query: String?,
        language: Int
    ): PagingSource<Int, SpeciesWithGenus>

    @Query(
        """SELECT species.*, NULL as female FROM species 
        WHERE (:query IS NULL 
            OR (:language = $GERMAN_ID AND (gersearchfield LIKE :query)) 
            OR (:language = $ENGLISH_ID AND (engsearchfield LIKE :query)) 
            OR sciname LIKE :query) AND gersearchfield IS NOT NULL
        ORDER BY 
            CASE WHEN :language = $GERMAN_ID THEN gername IS NULL ELSE engname IS NULL END, 
            CASE WHEN :language = $GERMAN_ID THEN gername ELSE engname END, 
            CASE WHEN :language = $GERMAN_ID THEN gersynonym IS NULL ELSE engsynonym IS NULL END,
            CASE WHEN :language = $GERMAN_ID THEN gersynonym ELSE engsynonym END,
            sciname
            """
    )
    fun filterSpecies(query: String?, language: Int): PagingSource<Int, SpeciesWithGenus>

    @Query(
        """SELECT rowid FROM species 
        WHERE ((:query IS NULL 
            OR (:language = $GERMAN_ID AND (gersearchfield LIKE :query))
            OR (:language = $ENGLISH_ID AND (engsearchfield LIKE :query))
            OR sciname LIKE :query) AND gersearchfield IS NOT NULL) 
            AND group_id NOT IN (:groups)
        ORDER BY 
            CASE WHEN :language = $GERMAN_ID THEN gername IS NULL ELSE engname IS NULL END, 
            CASE WHEN :language = $GERMAN_ID THEN gername ELSE engname END, 
            CASE WHEN :language = $GERMAN_ID THEN gersynonym IS NULL ELSE engsynonym IS NULL END,
            CASE WHEN :language = $GERMAN_ID THEN gersynonym ELSE engsynonym END,
            sciname
            """
    )
    suspend fun filterOthersSpeciesIds(query: String?, groups: List<String>, language: Int): List<Int>

    @Query(
        """SELECT rowid FROM species 
        WHERE ((:query IS NULL 
            OR (:language = $GERMAN_ID AND (gersearchfield LIKE :query))
            OR (:language = $ENGLISH_ID AND (engsearchfield LIKE :query))
            OR sciname LIKE :query) AND gersearchfield IS NOT NULL) 
            AND (:group IS NULL OR group_id = :group)
        ORDER BY 
            CASE WHEN :language = $GERMAN_ID THEN gername IS NULL ELSE engname IS NULL END, 
            CASE WHEN :language = $GERMAN_ID THEN gername ELSE engname END, 
            CASE WHEN :language = $GERMAN_ID THEN gersynonym IS NULL ELSE engsynonym IS NULL END,
            CASE WHEN :language = $GERMAN_ID THEN gersynonym ELSE engsynonym END,
            sciname
            """
    )
    suspend fun filterSpeciesIds(query: String?, group: String?, language: Int): List<Int>

    @Query(
        """SELECT s.*, NULL as female FROM species AS s
             JOIN groups AS g ON g.name = s.group_id
        WHERE g.nature = :nature AND (:query IS NULL 
            OR (:language = $GERMAN_ID AND (gersearchfield LIKE :query)) 
            OR (:language = $ENGLISH_ID AND (engsearchfield LIKE :query)) 
            OR sciname LIKE :query) AND gersearchfield IS NOT NULL
        ORDER BY 
            CASE WHEN :language = $GERMAN_ID THEN s.gername IS NULL ELSE s.engname IS NULL END, 
            CASE WHEN :language = $GERMAN_ID THEN s.gername ELSE s.engname END, 
            CASE WHEN :language = $GERMAN_ID THEN s.gersynonym IS NULL ELSE s.engsynonym IS NULL END,
            CASE WHEN :language = $GERMAN_ID THEN s.gersynonym ELSE s.engsynonym END,
            sciname
            """
    )
    fun filterSpeciesByNature(nature: String, query: String?, language: Int
    ): PagingSource<Int, SpeciesWithGenus>

    private fun buildSpeciesByCharactersQuery(
        searchQuery: String?,
        language: Int,
        categories: Int,
        query: List<Pair<Int, Float>>
    ): Pair<String, Array<Any?>> {

        // This is a contrived way of writing "VALUES()" that works on sqlite all the way down to
        // API level 21 (sqlite 3.8)
        val selectedCharacters = query.joinToString(" UNION ALL ") {
            "SELECT ? AS id, ? AS weight"
        }
        val args = listOf(categories.toFloat()) + query.flatMap { it.toList() } + listOf(
            searchQuery,
            language,
            searchQuery,
            searchQuery,
            language,
            searchQuery,
            searchQuery,
            searchQuery
        )
        return Pair(
            """SELECT *, sum(inner_distance) / CAST(? AS REAL) AS distance FROM (SELECT 
                | character_species.species_id AS species_id,
                | female,
                | character.rowid AS character_id,
                | species.*, 
                | (sum(abs(selected.weight - character_species.weight)) / 2.0) * character.weight AS inner_distance
                | FROM character_value_species AS character_species
                | JOIN character_value ON character_value.rowid = character_species.character_value_id
                | JOIN character ON character.rowid = character_value.character_id
                | JOIN ($selectedCharacters) AS selected ON selected.id = character_value.rowid 
                | JOIN species ON character_species.species_id = species.rowid
                | WHERE 
                |   ? IS NULL 
                | OR (
                |   (? = $GERMAN_ID AND (species.gername LIKE ? OR species.gersynonym LIKE ?)) 
                |   OR (? = $ENGLISH_ID AND (species.engname LIKE ? OR species.engsynonym LIKE ?)) 
                |   OR species.sciname LIKE ?
                | )
                | GROUP BY species_id, female, character_id)
                | GROUP BY species_id, female
                | HAVING ROUND(distance) < 75
                | ORDER BY distance""".trimMargin(),
            args.toTypedArray<Any?>()
        )
    }

    @RawQuery(
        observedEntities = [
            Species::class
        ]
    )
    fun filterSpeciesByCharactersInternal(
        query: SupportSQLiteQuery
    ): PagingSource<Int, SpeciesWithGenus>

    fun filterSpeciesByCharacters(
        searchQuery: String?,
        language: Int,
        categories: Int,
        query: List<Pair<Int, Float>>
    ): PagingSource<Int, SpeciesWithGenus> {
        val (queryString, args) = buildSpeciesByCharactersQuery(searchQuery, language, categories, query)
        return filterSpeciesByCharactersInternal(SimpleSQLiteQuery(queryString, args))
    }

    @RawQuery(
        observedEntities = [
            Species::class
        ]
    )
    suspend fun countSpeciesByCharactersInternal(
        query: SupportSQLiteQuery
    ): Int

    suspend fun countSpeciesByCharacters(
        searchQuery: String?,
        language: Int,
        categories: Int,
        query: List<Pair<Int, Float>>
    ): Int {
        if (query.isNotEmpty()) {
            val (queryString, args) = buildSpeciesByCharactersQuery(
                searchQuery,
                language,
                categories,
                query
            )
            return countSpeciesByCharactersInternal(
                SimpleSQLiteQuery(
                    "SELECT COUNT(*) FROM ($queryString)",
                    args
                )
            )
        } else {
            return 0
        }
    }
}