package com.combah.travel2.ui.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class ViewBindingAdapter<T, R : ViewBinding> :
    ReactiveAdapter<T, R, ViewBindingAdapter.ViewHolder<R>>() {

    class ViewHolder<R : ViewBinding>(override val binding: R) :
        ReactiveAdapter.ViewHolder<R>(binding.root)

    override fun getRootView(binding: R): View {
        return binding.root
    }

    override fun createViewHolder(binding: R): ViewHolder<R> {
        return ViewHolder(binding)
    }
}

inline fun <T, reified R : ViewBinding> createAdapter(crossinline bindFunction: (R, T) -> Unit): ViewBindingAdapter<T, R> {
    return object : ViewBindingAdapter<T, R>() {
        override fun getBinding(context: Context, parent: ViewGroup, viewType: Int): R {
            val bindingClass = R::class.java

            val inflateMethod = bindingClass.getDeclaredMethod(
                "inflate",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Boolean::class.java
            )
            return inflateMethod.invoke(null, LayoutInflater.from(context), parent, false) as R

        }

        override fun bind(binding: R, item: T) {
            bindFunction(binding, item)
        }
    }
}