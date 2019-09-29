package com.yh.stepviewlib.model

import com.yh.stepviewlib.ReviewStepStatus

/**
 * Created by CYH on 2019-09-29 11:17
 */
class ReviewOwnerInfo {
    var name: String = ""
    var auditStatus: ReviewStepStatus = ReviewStepStatus.UnStart
    var auditAt: String = ""
}