package com.combah.travel2.ui.widget

import android.content.Context
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

abstract class ReactiveAdapter<T, R : ViewDataBinding>(private val owner: LifecycleOwner, liveData: LiveData<List<T>>) : RecyclerView.Adapter<ReactiveAdapter<T, R>.ViewHolder>() {

    private val _onItemClicked = MutableLiveData<T>()
    val onItemClicked: LiveData<T>
        get() = _onItemClicked

    private var items: List<T>? = null

    init {
        liveData.observe(owner, Observer {
            items = it
            notifyDataSetChanged()
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = getBinding(parent.context, parent, viewType)
        binding.setLifecycleOwner(owner)
        return ViewHolder(getBinding(parent.context, parent, viewType))
    }

    abstract fun getBinding(context: Context, parent: ViewGroup, viewType: Int): R

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.get(position)?.let { feature ->
            bind(holder.binding, feature)
            holder.binding.root.setOnClickListener { _onItemClicked.postValue(feature) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items?.get(position)?.let(this::getItemViewType) ?: 0
    }

    open fun getItemViewType(item: T): Int {
        return 0
    }

    abstract fun bind(binding: R, item: T)

    inner class ViewHolder(binding: R) : BindingViewHolder<R>(binding)
}