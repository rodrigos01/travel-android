package com.combah.travel2.ui.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding

abstract class DataBindingAdapter<T, R : ViewDataBinding> :
    ReactiveAdapter<T, R, DataBindingAdapter.ViewHolder<R>>() {

    class ViewHolder<R : ViewDataBinding>(override val binding: R) :
        ReactiveAdapter.ViewHolder<R>(binding.root)

    override fun getRootView(binding: R): View {
        return binding.root
    }

    override fun createViewHolder(binding: R): ViewHolder<R> {
        return ViewHolder(binding)
    }
}