package com.yh.stepviewlib.ext

import android.graphics.Canvas
import android.graphics.Paint

/**
 * Created by CYH on 2019-07-11 10:39
 */

/**
 * 修正text的Baseline，使其显示到Rect的中间位置
 */
fun Canvas.drawTextWithRectCenter(
    text: String, x: Float, rectCenterY: Float, paint: Paint
) {
    val fontMetrics = paint.fontMetrics
    val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
    val baseline = rectCenterY + distance
    drawText(text, x, baseline, paint)
}
