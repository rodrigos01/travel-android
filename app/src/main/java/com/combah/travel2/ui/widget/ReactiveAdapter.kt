package com.combah.travel2.ui.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

abstract class ReactiveAdapter<T, R : ViewDataBinding> : RecyclerView.Adapter<ReactiveAdapter<T, R>.ViewHolder>() {

    private val _onItemClicked = MutableLiveData<T>()
    val onItemClicked: LiveData<T>
        get() = _onItemClicked

    private var items: List<T>? = null

    fun setItems(newItems: List<T>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = getBinding(parent.context, parent, viewType)
        return ViewHolder(binding)
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

inline fun <T, reified R : ViewDataBinding> createAdapter(crossinline bindFunction: (R, T) -> Unit): ReactiveAdapter<T, R> {
    return object : ReactiveAdapter<T, R>() {
        override fun getBinding(context: Context, parent: ViewGroup, viewType: Int): R {
            val bindingClass = R::class.java

            val inflateMethod = bindingClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
            return inflateMethod.invoke(null, LayoutInflater.from(context), parent, false) as R

        }

        override fun bind(binding: R, item: T) {
            bindFunction(binding, item)
        }
    }
}