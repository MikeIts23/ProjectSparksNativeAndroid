package com.example.nativesparksapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class ProfileBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private val paintTop = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintBottom = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val curveHeight = height * 0.4f

        // Gradient top: giallo -> verde
        val gradientTop = LinearGradient(
            0f, 0f,
            0f, curveHeight,
            intArrayOf(
                Color.parseColor("#FBB03B"), // Giallo
                Color.parseColor("#7BE495"), // Verde
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paintTop.shader = gradientTop

        val pathTop = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, curveHeight)
            quadTo(width * 0.5f, curveHeight + 50f, 0f, curveHeight)
            close()
        }
        canvas.drawPath(pathTop, paintTop)

        // Gradient bottom: #1C203D -> #4B56A3
        val gradientBottom = LinearGradient(
            0f, curveHeight,
            0f, height,
            intArrayOf(
                Color.parseColor("#1C203D"),
                Color.parseColor("#4B56A3")
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paintBottom.shader = gradientBottom

        val pathBottom = Path().apply {
            moveTo(0f, curveHeight)
            quadTo(width * 0.5f, curveHeight + 50f, width, curveHeight)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        canvas.drawPath(pathBottom, paintBottom)
    }
}
