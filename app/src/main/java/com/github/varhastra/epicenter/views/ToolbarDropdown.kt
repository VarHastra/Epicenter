package com.github.varhastra.epicenter.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.transition.Fade
import androidx.transition.TransitionManager
import com.github.varhastra.epicenter.R

class ToolbarDropdown : Toolbar {

    lateinit var dropdownTextView: TextView
    private lateinit var titleTextView: TextView

    private var onClick: (() -> Unit)? = null

    private val transition = Fade(Fade.MODE_OUT or Fade.MODE_IN).apply {
        duration = 250
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        // Hide title and subtitle
        title = ""
        subtitle = ""

        // Add custom title and dropdown
        val view = LayoutInflater.from(context).inflate(R.layout.toolbar_dropdown, null) as FrameLayout
        dropdownTextView = view.findViewById(R.id.tv_dropdown_toolbar)
        dropdownTextView.setOnClickListener { onClick?.invoke() }
        titleTextView = view.findViewById(R.id.tv_title_toolbar)
        addView(view, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))

        // Apply attributes
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.ToolbarDropdown)
        styleAttrs.apply {
            val dropdownText = styleAttrs.getString(R.styleable.ToolbarDropdown_dropDownText) ?: ""
            val titleText = styleAttrs.getString(R.styleable.ToolbarDropdown_titleText) ?: ""
            val displayDropdown = styleAttrs.getBoolean(R.styleable.ToolbarDropdown_displayDropdown, false)

            setDropdownText(dropdownText)
            setTitleText(titleText)
            showDropDown(displayDropdown)
        }

        styleAttrs.recycle()
    }

    fun showDropDown(show: Boolean) {
        TransitionManager.beginDelayedTransition(this, transition)
        if (show) {
            titleTextView.visibility = INVISIBLE
            dropdownTextView.visibility = VISIBLE
        } else {
            dropdownTextView.visibility = INVISIBLE
            titleTextView.visibility = VISIBLE
        }
    }

    fun setTitleText(title: String) {
        titleTextView.text = title
    }

    fun setDropdownText(text: String) {
        dropdownTextView.text = text
    }

    fun setOnDropdownClickListener(onClick: () -> Unit) {
        this.onClick = onClick
    }
}