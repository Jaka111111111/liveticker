package projekt.tf2liveticker

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class CalendarHelper {
    fun buildDateStringDDMMYY(timestamp: Long): String {
        val sdf =  DateTimeFormatter.ofPattern("dd.MM.yy \n HH:mm")
        val timestampAsDateString = sdf
            .withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(timestamp)).toString()
        return timestampAsDateString
    }
}