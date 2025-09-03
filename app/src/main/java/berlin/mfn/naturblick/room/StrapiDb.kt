/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import berlin.mfn.naturblick.BuildConfig

@Database(
    version = BuildConfig.VERSION_CODE,
    entities = [
        Species::class,
        Portrait::class,
        PortraitImage::class,
        PortraitImageSize::class,
        SimilarSpecies::class,
        Character::class,
        CharacterValue::class,
        CharacterValueSpecies::class,
        UnambiguousFeature::class,
        GoodToKnow::class,
        SourcesImprint::class,
        SourcesTranslations::class,
        TimeZonePolygon::class,
        TimeZoneVertex::class,
        CurrentVersion::class,
        Group::class
    ]
)
abstract class StrapiDb : RoomDatabase() {
    abstract fun speciesDao(): SpeciesDao
    abstract fun groupDao(): GroupDao
    abstract fun portraitDao(): PortraitDao
    abstract fun characterDao(): CharacterDao
    abstract fun sourcesImprintDao(): SourcesImprintDao
    abstract fun sourcesTranslationsDao(): SourcesTranslationsDao
    abstract fun timeZonePolygonDao(): TimeZonePolygonDao
    abstract fun currenVersionDao(): CurrentVersionDao

    companion object {
        @Volatile
        private var INSTANCE: StrapiDb? = null
        fun getDb(applicationContext: Context): StrapiDb =
            INSTANCE ?: synchronized(this) {
                val it = Room.databaseBuilder(
                    applicationContext,
                    StrapiDb::class.java,
                    "django"
                )
                    .createFromAsset("django-db.sqlite3")
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = it
                it
            }
    }
}
