package berlin.mfn.naturblick.room

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object CustomTypeConverters {
    @TypeConverter
    @JvmStatic
    fun uuidToString(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    @JvmStatic
    fun stringToUUID(value: String?): UUID? {
        return if (value == null) {
            null
        } else {
            UUID.fromString(value)
        }
    }

    @TypeConverter
    @JvmStatic
    fun zonedDateTimeToString(dateTime: ZonedDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
    }

    @TypeConverter
    @JvmStatic
    fun stringToZonedDateTime(value: String?): ZonedDateTime? {
        return if (value == null) {
            null
        } else {
            ZonedDateTime.parse(value, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        }
    }
}
