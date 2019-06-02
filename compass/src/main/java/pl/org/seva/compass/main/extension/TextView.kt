package pl.org.seva.compass.main.extension

import android.widget.TextView

infix fun TextView.set(text: CharSequence?) = setText(text)