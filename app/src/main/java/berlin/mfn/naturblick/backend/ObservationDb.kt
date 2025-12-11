/*
 * Copyright © 2024 Museum für Naturkunde Berlin.
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package berlin.mfn.naturblick.backend

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import berlin.mfn.naturblick.room.CustomTypeConverters

@TypeConverters(CustomTypeConverters::class)
@Database(
    version = 8,
    entities = [
        ObservationOperationEntry::class,
        PatchOperation::class,
        CreateOperation::class,
        DeleteOperation::class,
        UploadMediaOperation::class,
        UploadThumbnailMediaOperation::class,
        Observation::class,
        BackendObservation::class,
        Sync::class,
        ViewFieldbookOperation::class,
        ViewPortraitOperation::class
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = ObservationDb.DeleteIdResultColumns::class),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        // 6 to 7: see MIGRATION_6_7
        AutoMigration(from = 7, to = 8)
    ]
)
abstract class ObservationDb : RoomDatabase() {
    abstract fun operationDao(): OperationDao
    abstract fun syncDao(): SyncDao

    @DeleteColumn(tableName = "patch_operation", columnName = "id_result_species1")
    @DeleteColumn(tableName = "patch_operation", columnName = "id_result_species2")
    @DeleteColumn(tableName = "patch_operation", columnName = "id_result_species3")
    @DeleteColumn(tableName = "patch_operation", columnName = "id_result_score1")
    @DeleteColumn(tableName = "patch_operation", columnName = "id_result_score2")
    @DeleteColumn(tableName = "patch_operation", columnName = "id_result_score3")
    class DeleteIdResultColumns : AutoMigrationSpec

    companion object {
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DELETE FROM sync")
            }
        }

        @Volatile
        private var INSTANCE: ObservationDb? = null
        fun getDb(applicationContext: Context): ObservationDb =
            INSTANCE ?: synchronized(this) {
                val it = Room.databaseBuilder(
                    applicationContext,
                    ObservationDb::class.java,
                    "observations"
                )
                    .addMigrations(MIGRATION_6_7)
                    .build()
                INSTANCE = it
                it
            }
    }
}
