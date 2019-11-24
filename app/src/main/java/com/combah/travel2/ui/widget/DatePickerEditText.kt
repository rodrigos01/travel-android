package com.combah.travel2.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.combah.travel2.extensions.format
import java.util.*

class DatePickerEditText @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : EditText(context, attrs, defStyleAttr) {

    var date: Date? = null
        set(value) {
            field = value
            setText(field?.format())
        }

    var minDate: Date? = null

}

@BindingAdapter("dateAttrChanged")
fun setListeners(view: DatePickerEditText, listener: InverseBindingListener) {
    view.setOnClickListener {
        makeDatePickerDialog(view.context, view.date, minDate = view.minDate) { newDate ->
            view.date = newDate
            listener.onChange()
        }
    }
}

@BindingAdapter("minDate")
fun setMinDate(view: DatePickerEditText, minDate: Date?) {
    if (minDate != view.minDate) {
        view.minDate = minDate
    }
}

@BindingAdapter("date")
fun setDate(view: DatePickerEditText, date: Date?) {
    if (date != view.date) {
        view.date = date
    }
}

@InverseBindingAdapter(attribute = "date")
fun getDate(view: DatePickerEditText): Date? {
    return view.date
}