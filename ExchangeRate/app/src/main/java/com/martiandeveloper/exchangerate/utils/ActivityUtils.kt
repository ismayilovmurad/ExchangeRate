package com.martiandeveloper.exchangerate.utils

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun Activity.getColorRes(@ColorRes id: Int) = ContextCompat.getColor(applicationContext, id)
