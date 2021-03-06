package com.combah.travel2.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.combah.travel2.extensions.format
import java.util.*

class DatePickerEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var date: Date? = null
        set(value) {
            field = value
            setText(field?.format())
        }

    var minDate: Date? = null

}