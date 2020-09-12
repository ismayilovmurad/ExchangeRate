package com.martiandeveloper.exchangerate.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.martiandeveloper.exchangerate.model.ExchangeRate

@Database(entities = [ExchangeRate::class], version = 1)
abstract class ExchangeDatabase : RoomDatabase() {

    abstract fun exchangeDao(): ExchangeDao

    companion object {

        @Volatile
        private var instance: ExchangeDatabase? = null

        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, ExchangeDatabase::class.java, "exchange_database"
        ).build()
    }
}
