package travel.vola.android.extensions

import com.vola.android.model.data.Time
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration

fun Time(source: String): Time {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm Z", Locale.getDefault()).parse(source)
        ?: error("Invalid Date Format")
    val offsetString = source.split(" ").getOrNull(1) ?: "GMT"
    val timezone = TimeZone.getTimeZone("GMT$offsetString")
    return Time(date.time, timezone)
}

operator fun Time.plus(other: Long): Time =
    Time(timeInMillis = timeInMillis + other, timeZone = timeZone)

operator fun Time.plus(other: Time): Time = this + other.timeInMillis
operator fun Time.plus(duration: Duration) = this + duration.inWholeMilliseconds
operator fun Time.minus(other: Long): Time =
    Time(timeInMillis = timeInMillis - other, timeZone = timeZone)

operator fun Time.minus(duration: Duration) = this - duration.inWholeMilliseconds

fun Time.update(
    dayOfMonth: Int = this.dayOfMonth,
    month: Int = this.month,
    year: Int = this.year,
    hour: Int = this.hour,
    minute: Int = this.minute,
    second: Int = this.second,
    timeZone: TimeZone = this.timeZone,
): Time = Calendar.getInstance(timeZone).apply {
    set(year, month - 1, dayOfMonth, hour, minute, second)
    set(Calendar.MILLISECOND, 0)
}.let { Time(it.timeInMillis, it.timeZone) }

fun Time.toMidnight(): Time = update(hour = 0, minute = 0, second = 0)

fun Time.atTimeZone(timeZone: TimeZone): Time = Time(timeInMillis, timeZone)

fun Time.Companion.now() = Time(timeInMillis = System.currentTimeMillis(), TimeZone.getDefault())
