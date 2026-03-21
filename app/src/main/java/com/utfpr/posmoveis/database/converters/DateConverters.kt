package com.utfpr.posmoveis.database.converters

import androidx.room.TypeConverter
import java.util.Date

/**
 *  converter dados de data para o database
 */
class DateConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time

}
}