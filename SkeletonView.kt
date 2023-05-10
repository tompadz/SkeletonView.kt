class SkeletonView @JvmOverloads constructor(
    private val context: Context,
    private val attributeSet: AttributeSet? = null
) : View(context, attributeSet) {
 
    private val TAG = "SkeletonView"
 
    private val refreshIntervalInMillis = ((1000f / context.refreshRateInSeconds()) * .9f).toLong()
    private val matrix: Matrix = Matrix()
    private val angle = 45.0
    private val durationInMillis = 2800L
 
    private val skeletonColor = context.getColorCompat(R.color.skeleton_background)
    private val shimmerColor = context.getColorCompat(R.color.skeleton_shimmer)
 
    private val gradient get() = createGradient()
 
    private var animationTask: Runnable? = null
    private var animation: Handler? = null
 
    private var rootWidth = 0f
    private var rectF = RectF()
    private val paint = Paint().apply { isAntiAlias = true }
 
     var radius = 10.dpf
        set(value) {
            field = value
            invalidate()
        }
 
    private fun startAnimation() {
        if (animation == null) {
            animation = Handler(Looper.getMainLooper())
            animationTask = object : Runnable {
                override fun run() {
                    updateShimmer()
                    animation?.postDelayed(this, refreshIntervalInMillis)
                }
            }
            animationTask?.let { task -> animation?.post(task) }
        }
    }
 
    private fun stopAnimation() {
        animationTask?.let { task -> animation?.removeCallbacks(task) }
        animation = null
    }
 
    private fun updateShimmer() {
        try {
            matrix.setTranslate(currentOffset(), 0f)
            paint.shader.setLocalMatrix(matrix)
            invalidate()
        }catch (t:Throwable) {
 
        }
    }
 
    private fun currentOffset(): Float {
        val progress = currentProgress()
        val offset = rootWidth * 2
        val min = -offset
        val max = rootWidth + offset
        return progress * (max - min) + min
    }
 
    private fun currentProgress(): Float {
        val millis = System.currentTimeMillis()
        val current = millis.toDouble()
        val interval = durationInMillis
        val divisor = floor(current / interval)
        val start = interval * divisor
        val end = start + interval
        val percentage = (current - start) / (end - start)
        return percentage.toFloat()
    }
 
    private fun updateRectPosition(){
        val left = 0f
        val top = 0f
        val right = measuredWidth.toFloat()
        val bottom = measuredHeight.toFloat()
        rectF.set(left, top, right, bottom)
    }
 
    private fun createGradient() : LinearGradient {
        val radians = Math.toRadians(angle).toFloat()
        return LinearGradient(
            0f,
            0f,
            cos(radians) * rootWidth,
            sin(radians) * rootWidth,
            intArrayOf(skeletonColor, shimmerColor, skeletonColor),
            null,
            Shader.TileMode.CLAMP
        )
    }
 
    private fun Context.refreshRateInSeconds(): Float {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        return windowManager?.defaultDisplay?.refreshRate ?: 60f
    }
 
    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = result.coerceAtMost(specSize)
            }
        }
        return result
    }
 
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        updateRectPosition()
        canvas?.drawRoundRect(rectF, radius, radius, paint)
    }
 
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rootWidth = (parent as View).measuredWidth.toFloat()
        paint.shader = gradient
        invalidate()
    }
 
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }
 
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }
 
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
        setMeasuredDimension(
            measureDimension(desiredWidth, widthMeasureSpec),
            measureDimension(desiredHeight, heightMeasureSpec)
        )
    }
}
