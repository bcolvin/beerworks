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
import com.bc.beerworks.data.local.dao.FavoriteIngredientDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.HoneyDao
import com.bc.beerworks.data.local.dao.HoneyStyleCrossRefDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MaltDao
import com.bc.beerworks.data.local.dao.MaltStyleCrossRefDao
import com.bc.beerworks.data.local.dao.MeadStyleDao
import com.bc.beerworks.data.local.dao.OffFlavorDao
import com.bc.beerworks.data.local.dao.OffFlavorStyleCrossRefDao
import com.bc.beerworks.data.local.dao.PomeDao
import com.bc.beerworks.data.local.dao.PomeStyleCrossRefDao
import com.bc.beerworks.data.local.dao.ScoresheetDao
import com.bc.beerworks.data.local.dao.YeastDao
import com.bc.beerworks.data.local.dao.YeastStyleCrossRefDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BaStyleFtsEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleFtsEntity
import com.bc.beerworks.data.local.entity.CiderStyleEntity
import com.bc.beerworks.data.local.entity.CiderStyleFtsEntity
import com.bc.beerworks.data.local.entity.FavoriteIngredientEntity
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
import com.bc.beerworks.data.local.entity.ScoresheetEntity
import com.bc.beerworks.data.local.entity.OffFlavorEntity
import com.bc.beerworks.data.local.entity.OffFlavorFtsEntity
import com.bc.beerworks.data.local.entity.OffFlavorStyleCrossRef
import com.bc.beerworks.data.local.entity.PomeStyleCrossRef
import com.bc.beerworks.data.local.entity.PomeVarietyEntity
import com.bc.beerworks.data.local.entity.PomeVarietyFtsEntity
import com.bc.beerworks.data.local.entity.YeastStrainEntity
import com.bc.beerworks.data.local.entity.YeastStrainFtsEntity
import com.bc.beerworks.data.local.entity.YeastStyleCrossRef

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

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `pomes` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `fruitType` TEXT NOT NULL,
                `classification` TEXT NOT NULL,
                `fruitUse` TEXT NOT NULL,
                `botanicalName` TEXT NOT NULL,
                `origin` TEXT NOT NULL,
                `countryCode` TEXT NOT NULL,
                `acidityMalicPercent` REAL NOT NULL,
                `tanninPolyphenolPercent` REAL NOT NULL,
                `tanninLevel` TEXT NOT NULL,
                `sgMin` REAL NOT NULL,
                `sgMax` REAL NOT NULL,
                `harvestSeason` TEXT NOT NULL,
                `sorbitolGramsPerLiter` REAL,
                `description` TEXT NOT NULL,
                `blendingGuidance` TEXT NOT NULL,
                `pressingCharacteristics` TEXT NOT NULL,
                `descriptors` TEXT NOT NULL,
                `substituteIds` TEXT NOT NULL,
                `radarAcidity` REAL,
                `radarTannin` REAL,
                `radarFreshFruit` REAL,
                `radarBakedFruit` REAL,
                `radarCitrus` REAL,
                `radarFloral` REAL,
                `radarEarthy` REAL,
                `radarSweet` REAL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `pomes_fts` USING FTS4(
                `name`, `fruitType`, `classification`, `botanicalName`, `origin`,
                `description`, `blendingGuidance`, `pressingCharacteristics`, `descriptors`,
                content=`pomes`
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `pome_style_cross_ref` (
                `pomeId` TEXT NOT NULL,
                `styleId` TEXT NOT NULL,
                `guidelineType` TEXT NOT NULL,
                PRIMARY KEY(`pomeId`, `styleId`, `guidelineType`)
            )
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_pome_style_cross_ref_pomeId`
            ON `pome_style_cross_ref` (`pomeId`)
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_pome_style_cross_ref_styleId_guidelineType`
            ON `pome_style_cross_ref` (`styleId`, `guidelineType`)
        """)
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `yeasts` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `scientificName` TEXT NOT NULL,
                `origin` TEXT NOT NULL,
                `countryCode` TEXT NOT NULL,
                `discipline` TEXT NOT NULL,
                `classification` TEXT NOT NULL,
                `form` TEXT NOT NULL,
                `attenuationMin` REAL NOT NULL,
                `attenuationMax` REAL NOT NULL,
                `flocculation` TEXT NOT NULL,
                `tempMinC` REAL NOT NULL,
                `tempMaxC` REAL NOT NULL,
                `tempMinF` REAL NOT NULL,
                `tempMaxF` REAL NOT NULL,
                `alcoholTolerance` REAL NOT NULL,
                `isDiastaticus` INTEGER NOT NULL,
                `isPhenolic` INTEGER NOT NULL,
                `description` TEXT NOT NULL,
                `pitchingDynamics` TEXT NOT NULL,
                `descriptors` TEXT NOT NULL,
                `commercialEquivalentsJson` TEXT NOT NULL,
                `substituteIds` TEXT NOT NULL,
                `radarClean` REAL,
                `radarFruity` REAL,
                `radarSpicy` REAL,
                `radarTart` REAL,
                `radarFunky` REAL,
                `radarSulfury` REAL,
                `radarAlcohol` REAL,
                `radarMouthfeel` REAL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `yeasts_fts` USING FTS4(
                `name`, `scientificName`, `origin`, `description`, `pitchingDynamics`,
                `descriptors`, `commercialEquivalentsJson`,
                content=`yeasts`
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `yeast_style_cross_ref` (
                `yeastId` TEXT NOT NULL,
                `styleId` TEXT NOT NULL,
                `guidelineType` TEXT NOT NULL,
                PRIMARY KEY(`yeastId`, `styleId`, `guidelineType`)
            )
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_yeast_style_cross_ref_yeastId`
            ON `yeast_style_cross_ref` (`yeastId`)
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_yeast_style_cross_ref_styleId_guidelineType`
            ON `yeast_style_cross_ref` (`styleId`, `guidelineType`)
        """)
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `favorite_ingredients` (
                `id` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `off_flavors` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `emoji` TEXT NOT NULL,
                `category` TEXT NOT NULL,
                `chemicalCompound` TEXT NOT NULL,
                `perceptionThreshold` TEXT NOT NULL,
                `typicalConcentration` TEXT NOT NULL,
                `meilgaardCode` TEXT NOT NULL,
                `detectedIn` TEXT NOT NULL,
                `disciplines` TEXT NOT NULL,
                `stylePermissibility` TEXT NOT NULL,
                `description` TEXT NOT NULL,
                `aromaPerception` TEXT NOT NULL,
                `flavorPerception` TEXT NOT NULL,
                `mouthfeelPerception` TEXT NOT NULL,
                `descriptors` TEXT NOT NULL,
                `diagnosticContrastsJson` TEXT NOT NULL,
                `typicalOriginsJson` TEXT NOT NULL,
                `preventionTipsJson` TEXT NOT NULL,
                `whenAppropriate` TEXT NOT NULL,
                `confusedWithIds` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
        db.execSQL("""
            CREATE VIRTUAL TABLE IF NOT EXISTS `off_flavors_fts` USING FTS4(
                `name`, `chemicalCompound`, `category`, `meilgaardCode`, `description`,
                `descriptors`, `diagnosticContrastsJson`, `typicalOriginsJson`,
                `preventionTipsJson`, `whenAppropriate`,
                content=`off_flavors`
            )
        """)
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `off_flavor_style_cross_ref` (
                `offFlavorId` TEXT NOT NULL,
                `styleId` TEXT NOT NULL,
                `guidelineType` TEXT NOT NULL,
                PRIMARY KEY(`offFlavorId`, `styleId`, `guidelineType`)
            )
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_off_flavor_style_cross_ref_offFlavorId`
            ON `off_flavor_style_cross_ref` (`offFlavorId`)
        """)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS `index_off_flavor_style_cross_ref_styleId_guidelineType`
            ON `off_flavor_style_cross_ref` (`styleId`, `guidelineType`)
        """)
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `scoresheets` (
                `id` TEXT NOT NULL,
                `formType` TEXT NOT NULL,
                `entryTitle` TEXT NOT NULL,
                `brewer` TEXT NOT NULL,
                `styleId` TEXT,
                `styleName` TEXT NOT NULL,
                `styleNumber` TEXT NOT NULL,
                `evaluatedAtMillis` INTEGER NOT NULL,
                `aromaScore` INTEGER NOT NULL,
                `appearanceScore` INTEGER NOT NULL,
                `flavorScore` INTEGER NOT NULL,
                `mouthfeelScore` INTEGER NOT NULL,
                `overallScore` INTEGER NOT NULL,
                `aromaNotes` TEXT NOT NULL,
                `appearanceNotes` TEXT NOT NULL,
                `flavorNotes` TEXT NOT NULL,
                `mouthfeelNotes` TEXT NOT NULL,
                `overallNotes` TEXT NOT NULL,
                `bottleInspectionOk` INTEGER NOT NULL,
                `bottleInspectionNotes` TEXT NOT NULL,
                `stylisticAccuracy` INTEGER NOT NULL,
                `technicalMerit` INTEGER NOT NULL,
                `intangibles` INTEGER NOT NULL,
                `descriptorValues` TEXT NOT NULL,
                `offFlavorLevels` TEXT NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
        """)
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `scoresheets`")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `scoresheets` (
                `id` TEXT NOT NULL,
                `formType` TEXT NOT NULL,
                `entryTitle` TEXT NOT NULL,
                `brewer` TEXT NOT NULL,
                `styleId` TEXT,
                `styleName` TEXT NOT NULL,
                `styleNumber` TEXT NOT NULL,
                `evaluatedAtMillis` INTEGER NOT NULL,
                `aromaScore` INTEGER NOT NULL,
                `appearanceScore` INTEGER NOT NULL,
                `flavorScore` INTEGER NOT NULL,
                `mouthfeelScore` INTEGER NOT NULL,
                `overallScore` INTEGER NOT NULL,
                `aromaNotes` TEXT NOT NULL,
                `appearanceNotes` TEXT NOT NULL,
                `flavorNotes` TEXT NOT NULL,
                `mouthfeelNotes` TEXT NOT NULL,
                `overallNotes` TEXT NOT NULL,
                `bottleInspectionOk` INTEGER NOT NULL,
                `bottleInspectionNotes` TEXT NOT NULL,
                `stylisticAccuracy` INTEGER NOT NULL,
                `technicalMerit` INTEGER NOT NULL,
                `intangibles` INTEGER NOT NULL,
                `descriptorValues` TEXT NOT NULL,
                `offFlavorLevels` TEXT NOT NULL,
                `updatedAtMillis` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
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
        FavoriteIngredientEntity::class,
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
        HoneyStyleCrossRef::class,
        PomeVarietyEntity::class,
        PomeVarietyFtsEntity::class,
        PomeStyleCrossRef::class,
        YeastStrainEntity::class,
        YeastStrainFtsEntity::class,
        YeastStyleCrossRef::class,
        OffFlavorEntity::class,
        OffFlavorFtsEntity::class,
        OffFlavorStyleCrossRef::class,
        ScoresheetEntity::class
    ],
    version = 13,
    exportSchema = false
)
abstract class BeerworksDatabase : RoomDatabase() {
    abstract fun bjcpStyleDao(): BjcpStyleDao
    abstract fun baStyleDao(): BaStyleDao
    abstract fun favoriteStyleDao(): FavoriteStyleDao
    abstract fun favoriteIngredientDao(): FavoriteIngredientDao
    abstract fun hopDao(): HopDao
    abstract fun hopStyleCrossRefDao(): HopStyleCrossRefDao
    abstract fun maltDao(): MaltDao
    abstract fun maltStyleCrossRefDao(): MaltStyleCrossRefDao
    abstract fun meadStyleDao(): MeadStyleDao
    abstract fun ciderStyleDao(): CiderStyleDao
    abstract fun honeyDao(): HoneyDao
    abstract fun honeyStyleCrossRefDao(): HoneyStyleCrossRefDao
    abstract fun pomeDao(): PomeDao
    abstract fun pomeStyleCrossRefDao(): PomeStyleCrossRefDao
    abstract fun yeastDao(): YeastDao
    abstract fun yeastStyleCrossRefDao(): YeastStyleCrossRefDao
    abstract fun offFlavorDao(): OffFlavorDao
    abstract fun offFlavorStyleCrossRefDao(): OffFlavorStyleCrossRefDao
    abstract fun scoresheetDao(): ScoresheetDao

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
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
