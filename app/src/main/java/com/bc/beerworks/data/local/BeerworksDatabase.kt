package com.bc.beerworks.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bc.beerworks.data.local.dao.BaStyleDao
import com.bc.beerworks.data.local.dao.BjcpStyleDao
import com.bc.beerworks.data.local.dao.FavoriteStyleDao
import com.bc.beerworks.data.local.dao.HopDao
import com.bc.beerworks.data.local.dao.HopStyleCrossRefDao
import com.bc.beerworks.data.local.entity.BaStyleEntity
import com.bc.beerworks.data.local.entity.BaStyleFtsEntity
import com.bc.beerworks.data.local.entity.BjcpStyleEntity
import com.bc.beerworks.data.local.entity.BjcpStyleFtsEntity
import com.bc.beerworks.data.local.entity.FavoriteStyleEntity
import com.bc.beerworks.data.local.entity.HopStyleCrossRef
import com.bc.beerworks.data.local.entity.HopVarietyEntity
import com.bc.beerworks.data.local.entity.HopVarietyFtsEntity

@Database(
    entities = [
        BjcpStyleEntity::class,
        BjcpStyleFtsEntity::class,
        BaStyleEntity::class,
        BaStyleFtsEntity::class,
        FavoriteStyleEntity::class,
        HopVarietyEntity::class,
        HopVarietyFtsEntity::class,
        HopStyleCrossRef::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BeerworksDatabase : RoomDatabase() {
    abstract fun bjcpStyleDao(): BjcpStyleDao
    abstract fun baStyleDao(): BaStyleDao
    abstract fun favoriteStyleDao(): FavoriteStyleDao
    abstract fun hopDao(): HopDao
    abstract fun hopStyleCrossRefDao(): HopStyleCrossRefDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
