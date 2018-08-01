package com.combah.travel2.extensions

import com.facebook.litho.Column
import com.facebook.litho.Component

fun Column.Builder.children(vararg children: Component.Builder<*>): Column.Builder {
    children.forEach {
        child(it)
    }
    return this
}