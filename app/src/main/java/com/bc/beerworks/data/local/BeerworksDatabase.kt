package com.bc.beerworks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BaStyleFtsEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleFtsEntity
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.local.entity.HopStyleCrossRef
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.local.entity.HopVarietyFtsEntity
import com.bc.beerworks.data.local.entity.MaltStyleCrossRef
import com.bc.beerworks.data.local.entity.MaltVarietyEntity
import com.bc.beerworks.data.local.entity.MaltVarietyFtsEntity
import com.bc.beerworks.data.local.entity.MeadStyleEntity
import com.bc.beerworks.data.local.entity.MeadStyleFtsEntity

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `mead_styles` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `categoryNumber` TEXT NOT NULL,
                `number` TEXT NOT NULL,
                `overallImpression` TEXT NOT NULL,
                `aroma` TEXT NOT NULL,
                `appearance` TEXT NOT NULL,
                `flavor` TEXT NOT NULL,
                `mouthfeel` TEXT NOT NULL,
                `comments` TEXT NOT NULL,
                `history` TEXT NOT NULL,
                `characteristicIngredients` TEXT NOT NULL,
                `styleComparison` TEXT NOT NULL,
                `ogMin` REAL,
                `ogMax` REAL,
                `fgMin` REAL,
                `fgMax` REAL,
                `abvMin` REAL,
                `abvMax` REAL,
                `sweetness` TEXT NOT NULL,
                `carbonation` TEXT NOT NULL,
                `strength` TEXT NOT NULL,
                `commercialExamples` TEXT NOT NULL,
                `tags` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `mead_styles_fts` USING FTS4(
                `number`, `name`, `category`, `overallImpression`, `aroma`, `appearance`,
                `flavor`, `mouthfeel`, `comments`, `characteristicIngredients`,
                `commercialExamples`, `tags`, `sweetness`, `carbonation`, `strength`,
                content=`mead_styles`
            )
        """)
    }
}

@Database(
    entities = [
        BjcpStyleEntity::class,
        BjcpStyleFtsEntity::class,
        BaStyleEntity::class,
        BaStyleFtsEntity::class,
        FavoriteStyleEntity::class,
        HopVarietyEntity::class,
        HopVarietyFtsEntity::class,
        HopStyleCrossRef::class,
        MaltVarietyEntity::class,
        MaltVarietyFtsEntity::class,
        MaltStyleCrossRef::class,
        MeadStyleEntity::class,
        MeadStyleFtsEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class BeerworksDatabase : RoomDatabase() {
    abstract fun bjcpStyleDao(): BjcpStyleDao
    abstract fun baStyleDao(): BaStyleDao
    abstract fun favoriteStyleDao(): FavoriteStyleDao
    abstract fun hopDao(): HopDao
    abstract fun hopStyleCrossRefDao(): HopStyleCrossRefDao
    abstract fun maltDao(): MaltDao
    abstract fun maltStyleCrossRefDao(): MaltStyleCrossRefDao
    abstract fun meadStyleDao(): MeadStyleDao

    companion object {
        @Volatile
        private var INSTANCE: BeerworksDatabase? = null

        fun getInstance(context: Context): BeerworksDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeerworksDatabase::class.java,
                    "beerworks.db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
