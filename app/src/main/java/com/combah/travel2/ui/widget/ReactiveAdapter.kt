package com.combah.travel2.ui.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

abstract class ReactiveAdapter<T, R, S : ReactiveAdapter.ViewHolder<R>> : RecyclerView.Adapter<S>() {

    private val _onItemClicked = MutableLiveData<T>()
    val onItemClicked: LiveData<T>
        get() = _onItemClicked

    private var items: List<T>? = null

    fun setItems(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }

    abstract fun createViewHolder(binding: R): S

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): S {
        val binding = getBinding(parent.context, parent, viewType)
        return createViewHolder(binding)
    }

    abstract fun getBinding(context: Context, parent: ViewGroup, viewType: Int): R

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onBindViewHolder(holder: S, position: Int) {
        items?.get(position)?.let { feature ->
            bind(holder.binding, feature)
            getRootView(holder.binding).setOnClickListener { _onItemClicked.postValue(feature) }
        }
    }

    abstract fun getRootView(binding: R): View

    override fun getItemViewType(position: Int): Int {
        return items?.get(position)?.let(this::getItemViewType) ?: 0
    }

    open fun getItemViewType(item: T): Int {
        return 0
    }

    abstract fun bind(binding: R, item: T)

    abstract class ViewHolder<R>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract val binding: R
    }
}
