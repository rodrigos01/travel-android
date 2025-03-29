package travel.vola.android.extensions

import java.util.concurrent.TimeUnit

fun Int.hoursToMillis() = TimeUnit.HOURS.toMillis(this.toLong())
fun Int.minutesToMillis() = TimeUnit.MINUTES.toMillis(this.toLong())
