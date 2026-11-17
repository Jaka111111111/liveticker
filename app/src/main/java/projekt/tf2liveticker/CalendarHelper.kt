package projekt.tf2liveticker

import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CalendarHelper {
    fun buildDateStringDDMMYY(timestamp: Long): String {
        val sdf =  DateTimeFormatter.ofPattern("dd.MM.yy\nHH:mm")
        val timestampAsDateString = sdf
            .withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(timestamp)).toString()
        return timestampAsDateString
    }

    fun buildRelativeDayString(timestamp: Long): String {
        val sdf =  DateTimeFormatter.ofPattern("HH:mm")
        val timestampAsDateString = sdf
            .withZone(ZoneId.systemDefault())
            .format(java.time.Instant.ofEpochSecond(timestamp)).toString()
        return getRelativeDay(timestamp) + timestampAsDateString
    }

    val relativeDayList = arrayOf("today at ", "yesterday at ", " days ago at ")

    fun getRelativeDay(timestamp: Long): String {
        val day = (System.currentTimeMillis() / 1000 - timestamp) / 86400
        if (day < 3) {
            return relativeDayList[day.toInt()]
        }
        else {
            return day.toString() + relativeDayList[2]
        }
    }
}