package com.combah.travel2.ui.widget

import android.content.Context
import com.combah.travel2.R
import java.util.*

class ResolvingString constructor(private val resourceId: Int, private vararg val components: Any) {

    constructor(arg: String) : this(R.string.resolving_string_default, arg)

    fun resolve(context: Context): String {

        val fixedArgs = components.map {
            if (it is ResolvingString) {
                it.resolve(context)
            } else {
                it
            }
        }
        return context.getString(resourceId, *fixedArgs.toTypedArray())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResolvingString

        if (resourceId != other.resourceId) return false
        if (!Arrays.equals(components, other.components)) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * resourceId + Arrays.hashCode(components)
    }
}
