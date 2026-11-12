package com.bc.beerworks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.CiderStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.HoneyDao
import com.bc.beerworks.data.local.dao.HoneyStyleCrossRefDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BaStyleFtsEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleFtsEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleFtsEntity
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.local.entity.HoneyStyleCrossRef
import com.bc.beerworks.data.local.entity.HoneyVarietyEntity
import com.bc.beerworks.data.local.entity.HoneyVarietyFtsEntity
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

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `cider_styles` (
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
            CREATE VIRTUAL TABLE IF NOT EXISTS `cider_styles_fts` USING FTS4(
                `number`, `name`, `category`, `overallImpression`, `aroma`, `appearance`,
                `flavor`, `mouthfeel`, `comments`, `characteristicIngredients`,
                `commercialExamples`, `tags`, `sweetness`, `carbonation`, `strength`,
                content=`cider_styles`
            )
        """)
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `honeys` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `floralClassification` TEXT NOT NULL,
                `botanicalSource` TEXT NOT NULL,
                `origin` TEXT NOT NULL,
                `countryCode` TEXT NOT NULL,
                `pfundMin` REAL NOT NULL,
                `pfundMax` REAL NOT NULL,
                `usdaColor` TEXT NOT NULL,
                `moistureTypicalPercent` REAL NOT NULL,
                `fgRatio` REAL NOT NULL,
                `crystallizationTendency` TEXT NOT NULL,
                `extractPotentialSg` REAL NOT NULL,
                `flavorIntensity` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `fermentationBehavior` TEXT NOT NULL,
                `pairingNotes` TEXT NOT NULL,
                `descriptors` TEXT NOT NULL,
                `substituteIds` TEXT NOT NULL,
                `radarFloral` REAL,
                `radarFruity` REAL,
                `radarHerbal` REAL,
                `radarSpicy` REAL,
                `radarSweet` REAL,
                `radarWoody` REAL,
                `radarEarthy` REAL,
                `radarPungent` REAL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `honeys_fts` USING FTS4(
                `name`, `floralClassification`, `botanicalSource`, `origin`,
                `description`, `fermentationBehavior`, `pairingNotes`, `descriptors`,
                content=`honeys`
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `honey_style_cross_ref` (
                `honeyId` TEXT NOT NULL,
                `styleId` TEXT NOT NULL,
                `guidelineType` TEXT NOT NULL,
                PRIMARY KEY(`honeyId`, `styleId`, `guidelineType`)
            )
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_honey_style_cross_ref_honeyId`
            ON `honey_style_cross_ref` (`honeyId`)
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_honey_style_cross_ref_styleId_guidelineType`
            ON `honey_style_cross_ref` (`styleId`, `guidelineType`)
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
        MeadStyleFtsEntity::class,
        CiderStyleEntity::class,
        CiderStyleFtsEntity::class,
        HoneyVarietyEntity::class,
        HoneyVarietyFtsEntity::class,
        HoneyStyleCrossRef::class
    ],
    version = 7,
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
    abstract fun ciderStyleDao(): CiderStyleDao
    abstract fun honeyDao(): HoneyDao
    abstract fun honeyStyleCrossRefDao(): HoneyStyleCrossRefDao

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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
