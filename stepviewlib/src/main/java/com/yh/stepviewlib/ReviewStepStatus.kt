package com.yh.stepviewlib

import androidx.annotation.DrawableRes

/**
 * Created by CYH on 2019-09-29 11:12
 */

enum class ReviewStepStatus(
    @JvmField
    val stateCode: Int,
    @JvmField
    val stateName: String,
    @JvmField
    @DrawableRes
    val imgRes: Int
) {

    //    0：未开始   1：待审核  2：通过  3：驳回
    Unknown(-1, "未知", R.mipmap.ic_disallow),
    UnStart(0, "未开始", R.mipmap.ic_clock),
    WaitReview(1, "待审核", R.mipmap.ic_clock),
    Passed(2, "通过", R.mipmap.ic_audited),
    Disallow(3, "驳回", R.mipmap.ic_disallow);

    companion object {
        @JvmStatic
        fun getItem(code: Int): ReviewStepStatus {
            return values().find { status ->
                code == status.stateCode
            } ?: Unknown
        }
    }

    fun getStateCode(): Int {
        return stateCode
    }
}