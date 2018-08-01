package com.combah.travel2.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.combah.travel2.R
import com.combah.travel2.extensions.children
import com.facebook.litho.Column
import com.facebook.litho.ComponentContext
import com.facebook.litho.LithoView
import com.facebook.litho.widget.Text
import com.facebook.yoga.YogaEdge

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val componentContext = ComponentContext(this)

        val component = Column.create(componentContext).apply {
            paddingDip(YogaEdge.ALL, 16f)
            backgroundColor(Color.WHITE)
            children(
                    Text.create(componentContext).apply {
                        textRes(R.string.hello_world)
                        textSizeSp(40f)
                    },
                    Text.create(componentContext).apply {
                        textRes(R.string.main_subtitle)
                        textSizeSp(20f)
                    }
            )
        }.build()

        setContentView(LithoView.create(componentContext, component))

    }
}
