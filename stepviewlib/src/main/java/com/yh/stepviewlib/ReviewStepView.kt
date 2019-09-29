package com.yh.stepviewlib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import com.yh.stepviewlib.ext.*
import com.yh.stepviewlib.model.ReviewOwnerInfo
import kotlin.math.abs

/**
 * Created by CYH on 2019-07-10 19:25
 */
@Suppress("ConstantConditionIf")
class ReviewStepView(
    context: Context,
    @Nullable
    attrs: AttributeSet? = null
) : View(context, attrs),
    View.OnClickListener {

    companion object {
        private const val TAG = "ReviewStepView"
        private const val mDevFlag = true
    }

    private var mIClickListener: IClickListener? = null

    private val mSteps = arrayListOf<StepInfo>()
    private val mItemHeight = dp2px(55F)
    private var mHeight: Int

    private val mPaddingWithLine = dp2px(25F)

    private val mEnableFakeData = true

    private var mDevPaint = if (mDevFlag) Paint() else null
    private val mItemRect = if (mDevFlag) RectF() else null

    private val mItemLinePaint = Paint()

    private val mStepLinePaint = Paint()
    private val mStepLinePath = Path()
    private val mStepLineStartPoint = PointF()
    private val mStepLineEndPoint = PointF()

    private val mContentPadding = dp2px(15F)
    private val mPointBackgroundPaint = Paint()
    private val mPointBackgroundRadius = dp2px(6F)
    private val mPointForegroundPaint = Paint()
    private val mPointForegroundRadius = dp2px(3F)

    private val mTextOffsetX = dp2px(26F)
    private val mReviewOwnerPaint = Paint()
    private val mReviewOwnerRect: Rect = Rect()
    private val mCallBitmapPaint = Paint()
    private val mCallBitmap: Bitmap
    private val mCallIconRect: RectF = RectF()
    private val mAllCallClickRect: HashMap<StepInfo, RectF> = hashMapOf()
    private val mCallIconPadding = dp2px(3F)
    private val mReviewStatusPaint = Paint()
    private val mReviewTimePaint = Paint()

    init {
        if (mDevFlag) {
            mDevPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = dp2px(0.5F)
                //绘制长度为4的实线后再绘制长度为4的空白区域，0位间隔
                pathEffect = DashPathEffect(floatArrayOf(dp2px(1F), dp2px(3F)), 0f)
                color = Color.parseColor("#FFFF0000")
                isAntiAlias = true
            }
            if (mEnableFakeData) {
                mSteps.add(StepInfo(ReviewStepStatus.Passed))
                mSteps.add(StepInfo(ReviewStepStatus.Disallow))
                mSteps.add(StepInfo(ReviewStepStatus.WaitReview))
            }
        }
        mItemLinePaint.style = Paint.Style.FILL
        mItemLinePaint.strokeWidth = dp2px(0.5F)
        mItemLinePaint.color = Color.parseColor("#FFECECEC")
        mItemLinePaint.isAntiAlias = true

        mStepLinePaint.style = Paint.Style.FILL_AND_STROKE
        mStepLinePaint.strokeWidth = dp2px(0.5F)
        mStepLinePaint.pathEffect = DashPathEffect(floatArrayOf(dp2px(3F), dp2px(1F)), 0f)
        mStepLinePaint.color = Color.parseColor("#FFA7A7A7")
        mStepLinePaint.isAntiAlias = true

        mPointBackgroundPaint.style = Paint.Style.FILL
        mPointBackgroundPaint.strokeWidth = dp2px(2F)
        mPointBackgroundPaint.isAntiAlias = true
        setLayerType(LAYER_TYPE_SOFTWARE, mPointBackgroundPaint)
        mPointBackgroundPaint.maskFilter = BlurMaskFilter(50f, BlurMaskFilter.Blur.SOLID)

        mPointForegroundPaint.style = Paint.Style.FILL
        mPointForegroundPaint.strokeWidth = dp2px(2F)
        mPointForegroundPaint.isAntiAlias = true

        mReviewOwnerPaint.isAntiAlias = true
        mReviewOwnerPaint.style = Paint.Style.FILL
        mReviewOwnerPaint.textAlign = Paint.Align.LEFT
        mReviewOwnerPaint.color = Color.parseColor("#2A2B2E")
        mReviewOwnerPaint.textSize = dp2px(14F)
        mReviewOwnerPaint.isFakeBoldText = false

        mReviewStatusPaint.isAntiAlias = true
        mReviewStatusPaint.style = Paint.Style.FILL
        mReviewStatusPaint.textAlign = Paint.Align.RIGHT
        mReviewStatusPaint.textSize = dp2px(13F)
        mReviewStatusPaint.isFakeBoldText = false

        mReviewTimePaint.isAntiAlias = true
        mReviewTimePaint.style = Paint.Style.FILL
        mReviewTimePaint.textAlign = Paint.Align.LEFT
        mReviewTimePaint.color = Color.parseColor("#2A2B2E")
        mReviewTimePaint.textSize = dp2px(11F)
        mReviewTimePaint.isFakeBoldText = false

        mCallBitmapPaint.isAntiAlias = true
        mCallBitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_call_blue)

        setOnClickListener(this)

        invalidate()

        mHeight = (mItemHeight * mSteps.size).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var tmpHeightMeasureSpec = heightMeasureSpec
        if (mSteps.isNotEmpty()) {
            tmpHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, tmpHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        if (null == canvas) {
            return
        }
        val steps = mSteps
        steps.forEachIndexed { index, stepInfo ->
            onPreviewHook(stepInfo)
            val itemX = 0F
            val itemY = index * mItemHeight
            val stepStartX = itemX + mPaddingWithLine
            val stepStartY = itemY + mContentPadding
            calculationLinePoint(index, itemY, stepStartX, stepStartY)
            drawItemRect(itemX, itemY, canvas)
            if (steps.size - 1 > index) {
                drawItemLine(itemY, stepStartX, canvas)
            }
            if (steps.size > 1) {
                drawStepLine(canvas)
            }
            drawStepPoint(stepStartX, stepStartY, canvas)
            drawReviewOwner(stepInfo, stepStartX, stepStartY, canvas)
            drawReviewTime(stepInfo, itemY, stepStartX, canvas)
            drawReviewStatus(stepInfo, itemY, canvas)
        }
    }

    private fun drawReviewStatus(
        stepInfo: StepInfo, itemY: Float, canvas: Canvas
    ) {
        if (ReviewStepStatus.UnStart == stepInfo.reviewStatus || ReviewStepStatus.Unknown == stepInfo.reviewStatus) {
            return
        }
        val textEndX = width - mTextOffsetX
        val textCenterY = itemY + mItemHeight / 2F
        canvas.drawTextWithRectCenter(
            stepInfo.reviewStatusStr, textEndX, textCenterY, mReviewStatusPaint
        )
    }

    private fun drawReviewTime(
        stepInfo: StepInfo, itemY: Float, stepStartX: Float, canvas: Canvas
    ) {
        val textStartX = stepStartX + mTextOffsetX
        val textCenterY = itemY + mItemHeight - mContentPadding
        canvas.drawTextWithRectCenter(stepInfo.time, textStartX, textCenterY, mReviewTimePaint)
    }

    private fun drawReviewOwner(
        stepInfo: StepInfo, stepStartX: Float, stepStartY: Float, canvas: Canvas
    ) {
        val textStartX = stepStartX + mTextOffsetX
        val text = stepInfo.empName
        canvas.drawTextWithRectCenter(text, textStartX, stepStartY, mReviewOwnerPaint)
        if (!mCallBitmap.isRecycled) {
            mReviewOwnerPaint.getTextBounds(text, 0, text.length, mReviewOwnerRect)
            val iconX =
                textStartX + abs(mReviewOwnerRect.right - mReviewOwnerRect.left) + mCallIconPadding
            val iconSize = mReviewOwnerPaint.textSize
            mCallIconRect.set(
                iconX,
                stepStartY - mContentPadding / 2F,
                iconX + iconSize,
                stepStartY + iconSize - mContentPadding / 2F
            )
            canvas.drawBitmap(mCallBitmap, null, mCallIconRect, mCallBitmapPaint)
            mAllCallClickRect[stepInfo]?.apply rect@{
                this@rect.setEmpty()
                this@rect.set(
                    textStartX, mCallIconRect.top, mCallIconRect.right, mCallIconRect.bottom
                )
                if (mDevFlag) {
                    canvas.drawRect(this@rect, mDevPaint!!)
                }
            }
        }
    }

    private fun calculationLinePoint(
        index: Int, itemY: Float, stepStartX: Float, stepStartY: Float
    ) {
        when (index) {
            0 -> {
                //first
                val startY = stepStartY + mPointBackgroundRadius
                mStepLineStartPoint.set(stepStartX, startY)
                mStepLineEndPoint.set(stepStartX, itemY + mItemHeight)
            }

            mSteps.size - 1 -> {
                //last
                val endY = stepStartY + mPointBackgroundRadius
                mStepLineStartPoint.set(stepStartX, itemY)
                mStepLineEndPoint.set(stepStartX, endY)
            }

            else -> {
                //other
                mStepLineStartPoint.set(stepStartX, itemY)
                mStepLineEndPoint.set(stepStartX, itemY + mItemHeight)
            }
        }
    }

    private fun drawStepPoint(stepStartX: Float, stepStartY: Float, canvas: Canvas) {
        canvas.drawCircle(stepStartX, stepStartY, mPointBackgroundRadius, mPointBackgroundPaint)
        canvas.drawCircle(stepStartX, stepStartY, mPointForegroundRadius, mPointForegroundPaint)
    }

    private fun drawStepLine(canvas: Canvas) {
        mStepLinePath.reset()
        mStepLinePath.moveTo(mStepLineStartPoint.x, mStepLineStartPoint.y)
        mStepLinePath.lineTo(mStepLineEndPoint.x, mStepLineEndPoint.y)
        canvas.drawPath(mStepLinePath, mStepLinePaint)
    }

    private fun drawItemLine(
        itemY: Float, stepStartX: Float, canvas: Canvas
    ) {
        canvas.drawLine(
            stepStartX + mTextOffsetX,
            itemY + mItemHeight,
            width - mTextOffsetX,
            itemY + mItemHeight,
            mItemLinePaint
        )
    }

    private fun drawItemRect(
        itemX: Float, itemY: Float, canvas: Canvas
    ) {
        if (mDevFlag) {
            mItemRect?.set(itemX, itemY, width.toFloat(), itemY + mItemHeight)
            canvas.drawRect(mItemRect!!, mDevPaint!!)
        }
    }

    private fun onPreviewHook(stepInfo: StepInfo) {
        if (stepInfo.isSelect) {
            mPointBackgroundPaint.color = Color.parseColor("#E2EAFF")
            mPointForegroundPaint.color = Color.parseColor("#467CFF")
        } else {
            mPointBackgroundPaint.color = Color.parseColor("#F5F4F4")
            mPointForegroundPaint.color = Color.parseColor("#D2D2D2")
        }
        when (stepInfo.reviewStatus) {
            ReviewStepStatus.Unknown, ReviewStepStatus.UnStart -> {
            }

            ReviewStepStatus.WaitReview -> {
                mReviewStatusPaint.color = Color.parseColor("#467CFF")
            }

            ReviewStepStatus.Passed -> {
                mReviewStatusPaint.color = Color.parseColor("#449156")
            }

            ReviewStepStatus.Disallow -> {
                mReviewStatusPaint.color = Color.parseColor("#FD3A47")
            }
        }
    }

    @Suppress("unused")
    class StepInfo {

        val empName: String
        val time: String
        val reviewStatus: ReviewStepStatus
        val isSelect: Boolean
        val reviewStatusStr: String
        val position: Int

        constructor(reviewOwnerInfo: ReviewOwnerInfo, index: Int) {
            position = index
            empName = reviewOwnerInfo.name ?: ""
            reviewStatus = reviewOwnerInfo.auditStatus ?: ReviewStepStatus.Unknown
            val millis = time2Millis(reviewOwnerInfo.auditAt)
            time = if (millis > 0) {
                formatDate(millis, "yyyy-MM-dd HH:mm")
            } else {
                ""
            }
            when (reviewStatus) {
                ReviewStepStatus.WaitReview,
                ReviewStepStatus.Passed,
                ReviewStepStatus.Disallow -> {
                    reviewStatusStr = reviewStatus.stateName
                    isSelect = true
                }

                else -> {
                    reviewStatusStr = ""
                    isSelect = false
                }
            }
        }

        constructor(reviewStatus: ReviewStepStatus) {
//            if (BuildConfig.BUILD_TYPE != "dev") {
//                throw Exception("Current build type ${BuildConfig.BUILD_TYPE} can not invoke this constructor")
//            }
            position = -1
            empName = "大法师"
            time = "2019-01-03 12:12"
            this.reviewStatus = reviewStatus
            when (reviewStatus) {
                ReviewStepStatus.WaitReview -> {
                    reviewStatusStr = "等待审核"
                    isSelect = true
                }

                ReviewStepStatus.Passed -> {
                    reviewStatusStr = "已同意"
                    isSelect = true
                }

                ReviewStepStatus.Disallow -> {
                    reviewStatusStr = "已驳回"
                    isSelect = true
                }

                else -> {
                    reviewStatusStr = ""
                    isSelect = false
                }
            }
        }

        override fun toString(): String {
            return "StepInfo(empName='$empName', time='$time', reviewStatus=$reviewStatus, isSelect=$isSelect, reviewStatusStr='$reviewStatusStr')"
        }
    }

    private var mY: Float = 0F
    private var mX: Float = 0F
    private var isClick: Boolean = false
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount > 1) {
            return false
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mX = event.x
                mY = event.y
                val target = mAllCallClickRect.filter { it.value.contains(mX, mY) }
                isClick = target.isNotEmpty() && target.size == 1
                if (isClick) {
                    tag = target.keys.first()
                } else {
                    tag = null
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val mDY: Float
                if (isClick) {
                    mDY = event.y - mY
                    isClick = abs(mDY) <= getScaledTouchSlop()
                    if (!isClick) {
                        tag = null
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                mX = event.x
                mY = event.y
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: $isClick")
        if (null == v?.tag as? StepInfo || !isClick) {
            return
        }
        mIClickListener?.onClick(v.tag as StepInfo)
    }

    fun setClickListener(iClickListener: IClickListener) {
        mIClickListener = iClickListener
    }

    fun setupData(steps: ArrayList<StepInfo>) {
        if (steps.size > 0) {
            mAllCallClickRect.clear()
            mSteps.clear()
            mSteps.addAll(steps)
            steps.forEach { step ->
                mAllCallClickRect[step] = RectF()
            }
            invalidate()
            mHeight = (mItemHeight * mSteps.size).toInt()
            requestLayout()
        }
    }

    interface IClickListener {
        fun onClick(stepInfo: StepInfo)
    }
}