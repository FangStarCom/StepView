package com.yh.stepviewlib.ext

import android.view.View
import android.view.ViewConfiguration

/**
 * Created by CYH on 2019-07-11 10:46
 */

fun View.getScaledTouchSlop(): Int {
    return ViewConfiguration.get(context)
        .scaledTouchSlop
}
