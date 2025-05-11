package travel.vola.android.extensions

import travel.vola.android.model.data.Time
import java.time.Instant
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.TimeZone
import kotlin.time.Duration

fun Time(source: String): Time {
    return try {
        Time.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    } catch (e: DateTimeParseException) {
        try {
            Time.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm Z"))
        } catch (e: DateTimeParseException) {
            Time.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm z"))
        }
    }
}

fun Time(epochMillis: Long, timeZone: TimeZone): Time = Time(epochMillis, timeZone.toZoneId())

fun Time(epochMillis: Long, zoneId: ZoneId): Time =
    Time.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId)

val Time.timeInMillis: Long
    get() = this.toInstant().toEpochMilli()

operator fun Time.plus(other: Long): Time = this.plus(java.time.Duration.ofMillis(other))

operator fun Time.minus(other: Long): Time = this.minus(java.time.Duration.ofMillis(other))

operator fun Time.plus(other: Time): Time = this.plusSeconds(other.toEpochSecond())
operator fun Time.plus(duration: Duration) = this + duration.inWholeMilliseconds

operator fun Time.minus(duration: Duration) = this - duration.inWholeMilliseconds

fun Time.update(
    dayOfMonth: Int = this.dayOfMonth,
    month: Month = this.month,
    year: Int = this.year,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    timeZone: ZoneId = this.zone,
): Time = Time.of(year, month.value, dayOfMonth, hour, minute, second, 0, timeZone)

fun Time.toMidnight(): Time = this.withHour(0)

fun Time.atTimeZone(timeZone: TimeZone): Time = withZoneSameInstant(timeZone.toZoneId())
