package travel.vola.android.extensions

import java.time.Instant
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.TimeZone
import kotlin.time.Duration

fun zonedDateTime(source: String): ZonedDateTime {
    return try {
        ZonedDateTime.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (_: DateTimeParseException) {
        try {
            ZonedDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm Z"))
        } catch (_: DateTimeParseException) {
            ZonedDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm z"))
        }
    }
}

fun zonedDateTime(epochMillis: Long, timeZone: TimeZone): ZonedDateTime =
    zonedDateTime(epochMillis, timeZone.toZoneId())

fun zonedDateTime(epochMillis: Long, zoneId: ZoneId): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId)

val ZonedDateTime.timeInMillis: Long
    get() = this.toInstant().toEpochMilli()

operator fun ZonedDateTime.plus(other: Long): ZonedDateTime =
    this.plus(java.time.Duration.ofMillis(other))

operator fun ZonedDateTime.minus(other: Long): ZonedDateTime =
    this.minus(java.time.Duration.ofMillis(other))

operator fun ZonedDateTime.plus(other: ZonedDateTime): ZonedDateTime =
    this.plusSeconds(other.toEpochSecond())

operator fun ZonedDateTime.plus(duration: Duration) = this + duration.inWholeMilliseconds

operator fun ZonedDateTime.minus(duration: Duration) = this - duration.inWholeMilliseconds

fun ZonedDateTime.update(
    dayOfMonth: Int = this.dayOfMonth,
    month: Month = this.month,
    year: Int = this.year,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    timeZone: ZoneId = this.zone,
): ZonedDateTime =
    ZonedDateTime.of(year, month.value, dayOfMonth, hour, minute, second, 0, timeZone)

fun ZonedDateTime.toMidnight(): ZonedDateTime = this.withHour(0)

fun ZonedDateTime.atTimeZone(timeZone: TimeZone): ZonedDateTime =
    withZoneSameInstant(timeZone.toZoneId())
