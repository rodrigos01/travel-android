package com.combah.travel2.ui.databinding

import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.combah.travel2.ui.widget.ReactiveAdapter
import com.combah.travel2.ui.widget.ResolvingString

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, url: String?) {
    url?.let {
        Glide.with(view.context)
            .load(url)
            .into(view)
    }
}

@BindingAdapter("visibleOrGone")
fun visibleOrGone(view: View, visible: Boolean?) {
    if (visible == false) {
        view.visibility = View.GONE
    }
}

@BindingAdapter("visible")
fun setVisible(view: View, visible: Boolean?) {
    if (visible == false) {
        view.visibility = View.INVISIBLE
    }
}

@BindingAdapter("android:text")
fun setText(view: TextView, resolvingString: ResolvingString?) {
    val context = view.context

    resolvingString?.let {
        val string = it.resolve(context)
        view.text = string
    }
}

@BindingAdapter("resourceId")
fun setImage(view: ImageView, resourceId: Int?) {
    if (resourceId != null) {
        try {
            val drawable = ContextCompat.getDrawable(view.context, resourceId)
            drawable?.let(view::setImageDrawable)
        } catch (ignored: Resources.NotFoundException) {

        }
    }
}

@Suppress("UNCHECKED_CAST")
@BindingAdapter("items")
fun <T> setItems(view: RecyclerView, items: List<T>?) {
    items ?: return
    val adapter = (view.adapter as? ReactiveAdapter<T, *>) ?: return

    adapter.setItems(items)
}