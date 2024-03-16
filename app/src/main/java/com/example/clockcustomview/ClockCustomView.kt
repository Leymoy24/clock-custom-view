package com.example.clockcustomview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val START_ANGLE = -Math.PI / 2
private const val REFRESH_PERIOD = 60L

class ClockCustomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var clockRadius = 0.0f
    private var centerX = 0.0f
    private var centerY = 0.0f

    private val position: PointF = PointF(0.0f, 0.0f)

    var baseColor = 0
    var textColor = 0
    var frameColor = 0
    var dotsColor = 0
    var hourHandColor = 0
    var minuteHandColor = 0
    var secondHandColor = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.ClockView) {
            baseColor = getColor(
                R.styleable.ClockView_baseColor,
                ContextCompat.getColor(context, R.color.light_gray)
            )
            textColor = getColor(
                R.styleable.ClockView_textColor,
                ContextCompat.getColor(context, R.color.black)
            )
            frameColor = getColor(
                R.styleable.ClockView_frameColor,
                ContextCompat.getColor(context, R.color.black)
            )
            dotsColor = getColor(
                R.styleable.ClockView_dotsColor,
                ContextCompat.getColor(context, R.color.black)
            )
            hourHandColor = getColor(
                R.styleable.ClockView_hourHandColor,
                ContextCompat.getColor(context, R.color.black)
            )
            minuteHandColor = getColor(
                R.styleable.ClockView_minuteHandColor,
                ContextCompat.getColor(context, R.color.black)
            )
            secondHandColor = getColor(
                R.styleable.ClockView_secondHandColor,
                ContextCompat.getColor(context, R.color.red)
            )
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textScaleX = 0.9f
        letterSpacing = -0.1f
        typeface = Typeface.DEFAULT
        strokeCap = Paint.Cap.ROUND
        val typeFaceFont = resources.getFont(R.font.roboto)
        typeface = Typeface.create(typeFaceFont, Typeface.BOLD)
    }

    private val paintHourHand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textScaleX = 0.9f
        letterSpacing = -0.15f
        typeface = Typeface.DEFAULT
        strokeCap = Paint.Cap.ROUND
    }

    private val paintMinuteHand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textScaleX = 0.9f
        letterSpacing = -0.15f
        typeface = Typeface.DEFAULT
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    private val paintSecondHand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textScaleX = 0.9f
        letterSpacing = -0.15f
        typeface = Typeface.DEFAULT
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    private val paintClockFrame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textScaleX = 0.9f
        letterSpacing = -0.15f
        typeface = Typeface.DEFAULT
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        clockRadius = min(width, height) / 2f - (min(width, height) / 25)
        centerX = width / 2f
        centerY = height / 2f
    }

    private fun PointF.computeXYForPoints(pos: Int, radius: Float) {
        val angle = (pos * (Math.PI / 30)).toFloat()
        x = radius * cos(angle) + centerX
        y = radius * sin(angle) + centerY
    }

    private fun PointF.computeXYForHourLabels(hour: Int, radius: Float) {
        val angle = (START_ANGLE + hour * (Math.PI / 6)).toFloat()
        x = radius * cos(angle) + centerX
        val textBaselineToCenter = (paint.descent() + paint.ascent()) / 2
        y = radius * sin(angle) + centerY - textBaselineToCenter
    }

    private fun drawClockBase(canvas: Canvas) {
        paint.color = baseColor
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, clockRadius, paint)
    }

    private fun drawClockFrame(canvas: Canvas) {
        paintClockFrame.color = frameColor
        paintClockFrame.style = Paint.Style.STROKE
        paintClockFrame.strokeWidth = clockRadius / 12
        val boundaryRadius = clockRadius - paintClockFrame.strokeWidth / 2
        val minOfHeightWidth = min(width, height)
        paintClockFrame.setShadowLayer(minOfHeightWidth / 2f / 20, 0.0f, 0.0f, Color.BLACK)
        canvas.drawCircle(centerX, centerY, boundaryRadius, paintClockFrame)
        paintClockFrame.strokeWidth = 0f
    }

    private fun drawDots(canvas: Canvas) {
        paint.color = dotsColor
        paint.style = Paint.Style.FILL
        val dotsDrawLineRadius = clockRadius * 5 / 6
        for (i in 0 until 60) {
            position.computeXYForPoints(i, dotsDrawLineRadius)
            val dotRadius = if (i % 5 == 0) clockRadius / 96 else clockRadius / 128
            canvas.drawCircle(position.x, position.y, dotRadius, paint)
        }
    }

    private fun drawHourLabels(canvas: Canvas) {
        paint.textSize = clockRadius * 2 / 7
        paint.strokeWidth = 0f
        paint.color = textColor
        val labelsDrawLineRadius = clockRadius * 11 / 16
        for (i in 1..12) {
            position.computeXYForHourLabels(i, labelsDrawLineRadius)
            val label = i.toString()
            canvas.drawText(label, position.x, position.y, paint)
        }
    }

    private fun drawClockHands(canvas: Canvas) {
        val calendar: Calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        hour = if (hour > 12) hour - 12 else hour
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        drawHourHand(canvas, hour + minute / 60f)
        drawMinuteHand(canvas, minute + second / 60f)
        drawSecondHand(canvas, second)
    }

    private fun drawHourHand(canvas: Canvas, hourWithMinutes: Float) {
        paintHourHand.color = hourHandColor
        paintHourHand.strokeWidth = clockRadius / 15
        val minOfHeightWidth = min(width, height)
        paintHourHand.setShadowLayer(
            minOfHeightWidth / 2f / 20,
            min(width, height) / 150.0f,
            min(width, height) / 150.0f,
            Color.BLACK
        )
        val angle = (Math.PI * hourWithMinutes / 6 + START_ANGLE).toFloat()
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 3 / 14,
            centerY - sin(angle) * clockRadius * 3 / 14,
            centerX + cos(angle) * clockRadius * 7 / 14,
            centerY + sin(angle) * clockRadius * 7 / 14,
            paintHourHand
        )
    }

    private fun drawMinuteHand(canvas: Canvas, minute: Float) {
        paintMinuteHand.color = minuteHandColor
        paintMinuteHand.strokeWidth = clockRadius / 40
        val minOfHeightWidth = min(width, height)
        paintMinuteHand.setShadowLayer(
            minOfHeightWidth / 2f / 20,
            min(width, height) / 80.0f,
            min(width, height) / 60.0f,
            Color.BLACK
        )
        val angle = (Math.PI * minute / 30 + START_ANGLE).toFloat()
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 2 / 7,
            centerY - sin(angle) * clockRadius * 2 / 7,
            centerX + cos(angle) * clockRadius * 5 / 7,
            centerY + sin(angle) * clockRadius * 5 / 7,
            paintMinuteHand
        )
    }

    private fun drawSecondHand(canvas: Canvas, second: Int) {
        paintSecondHand.color = secondHandColor
        val angle = (Math.PI * second / 30 + START_ANGLE).toFloat()
        val minOfHeightWidth = min(width, height)
        paintSecondHand.setShadowLayer(
            minOfHeightWidth / 2f / 25,
            min(width, height) / 25.0f,
            min(width, height) / 25.0f,
            Color.BLACK
        )
        paintSecondHand.strokeWidth = clockRadius / 80
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 1 / 14,
            centerY - sin(angle) * clockRadius * 1 / 14,
            centerX + cos(angle) * clockRadius * 5 / 7,
            centerY + sin(angle) * clockRadius * 5 / 7,
            paintSecondHand
        )
        paintSecondHand.strokeWidth = clockRadius / 50
        canvas.drawLine(
            centerX - cos(angle) * clockRadius * 2 / 7,
            centerY - sin(angle) * clockRadius * 2 / 7,
            centerX - cos(angle) * clockRadius * 1 / 14,
            centerY - sin(angle) * clockRadius * 1 / 14,
            paintSecondHand
        )
    }

    private fun drawCircle(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = paintSecondHand.color
        canvas.drawCircle(centerX, centerY, clockRadius / 35, paint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClockBase(canvas)
        drawClockFrame(canvas)
        drawDots(canvas)
        drawHourLabels(canvas)
        drawClockHands(canvas)
        drawCircle(canvas)
        postInvalidateDelayed(REFRESH_PERIOD)
    }
}