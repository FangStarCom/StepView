package com.yh.stepviewlib.ext

import android.content.res.Resources
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by CYH on 2019-09-29 11:10
 */

fun dp2px(dp: Float): Float {
    return dp * Resources.getSystem().displayMetrics.densityDpi / 160f
}

fun time2Millis(time: String?): Long {
    if(time?.isEmpty() != false) {
        return 0
    }
    return time2Millis(time.toLong())
}

fun time2Millis(time: Long): Long = if(time < 9999999999L) 1000L * time else time

fun formatDate(
    time: String?, pattern: String = "yyyy-MM-dd HH:mm"
): String = formatDate(time2Millis(time), pattern)

fun formatDate(
    time: Int?, pattern: String = "yyyy-MM-dd HH:mm"
): String = formatDate(time?.toLong(), pattern)

fun formatDate(time: Long?): String = formatDate(time, "yyyy-MM-dd HH:mm")

fun formatDate(time: Long?, pattern: String): String {
    if(null != time && time > 0) {
        return SimpleDateFormat(pattern, Locale.CHINESE).format(time2Millis(time))
    }
    return ""
}