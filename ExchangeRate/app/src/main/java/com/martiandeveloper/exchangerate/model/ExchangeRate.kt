package com.martiandeveloper.exchangerate.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "exchanges")
data class ExchangeRate(
    @ColumnInfo(name = "code")
    @Json(name = "code")
    val exchangeCode: String,
    @ColumnInfo(name = "rate")
    @Json(name = "rate")
    val exchangeRate: Double?,

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
)
