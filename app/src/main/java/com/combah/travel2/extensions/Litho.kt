package com.combah.travel2.extensions

import com.facebook.litho.Column
import com.facebook.litho.Component
import com.facebook.litho.ComponentContext
import com.facebook.litho.Row
import com.facebook.litho.widget.Image
import com.facebook.litho.widget.Text

fun Column.Builder.children(vararg children: Component.Builder<*>): Column.Builder {
    children.forEach {
        child(it)
    }
    return this
}

fun Row.Builder.children(vararg children: Component.Builder<*>): Row.Builder {
    children.forEach {
        child(it)
    }
    return this
}

fun Column(context: ComponentContext, builder: Column.Builder.() -> Unit) = Column.create(context).apply(builder)

fun Row(context: ComponentContext, builder: Row.Builder.() -> Unit) = Row.create(context).apply(builder)

fun Text(context: ComponentContext, builder: Text.Builder.() -> Unit) = Text.create(context).apply(builder)

fun Image(context: ComponentContext, builder: Image.Builder.() -> Unit) = Image.create(context).apply(builder)