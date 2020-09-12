package com.martiandeveloper.exchangerate.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.martiandeveloper.exchangerate.model.ExchangeRate

@Dao
interface ExchangeDao {

    @Insert
    suspend fun insertAll(vararg exchangeRates: ExchangeRate): List<Long>

    @Query("SELECT * FROM exchanges")
    suspend fun getAll(): List<ExchangeRate>

    @Query("DELETE FROM exchanges")
    suspend fun deleteAll()
}
