package com.example.trackme.tracking.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.ui.tooling.preview.Devices
import androidx.ui.tooling.preview.Preview

data class Point(val x: Float, val y: Float)

class Interpolation(srcFrom: Float, srcTo: Float, targetFrom: Float, targetTo: Float) {
    private var scaleFactor: Float = 1f
    private var offset: Float = 0f

    init {
        scaleFactor = (targetTo - targetFrom) / (srcTo - srcFrom)
        offset = targetTo - scaleFactor * srcTo
    }

    fun interpolate(value: Float): Float {
        return offset + scaleFactor * value
    }
}

data class DrawingContext(
    val xInter: Interpolation,
    val yInter: Interpolation,
    val drawScope: DrawScope,
)

@Composable
fun LineGraph(
    modifier: Modifier,
    data: List<Point>,
) {
    val xMin = data.minOf { it.x }
    val xMax = data.maxOf { it.x }
    val yMin = data.minOf { it.y }
    val yMax = data.maxOf { it.y }

    Canvas(modifier = modifier.background(Color.White)) {
        val xInter = Interpolation(xMin - xMax * 0.05f, xMax * 1.1f, 0f, size.width)
        val yInter = Interpolation(yMin - yMax * 0.05f, yMax * 1.1f, size.height, 0f)
        val drawingContext = DrawingContext(xInter, yInter, this)

        drawOriginLines(drawingContext)

        val color = Color.Red
        drawLine(drawingContext, data, color)
        drawPoints(drawingContext, data, color)
    }
}

fun drawOriginLines(drawingContext: DrawingContext) {
    val (xInter, yInter) = drawingContext
    drawingContext.drawScope.apply {
        val xOrigin = xInter.interpolate(0f)
        val yOrigin = yInter.interpolate(0f)
        drawLine(Color.Black, Offset(0f, yOrigin), Offset(size.width, yOrigin))
        drawLine(Color.Black, Offset(xOrigin, 0f), Offset(xOrigin, size.height))
    }
}

fun drawLine(drawingContext: DrawingContext, data: List<Point>, color: Color) {
    val (xInter, yInter) = drawingContext
    drawingContext.drawScope.apply {
        val path = Path()
        data.forEachIndexed { index, point ->
            val x = xInter.interpolate(point.x)
            val y = yInter.interpolate(point.y)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke())
    }
}

fun drawPoints(drawingContext: DrawingContext, data: List<Point>, color: Color) {
    val (xInter, yInter) = drawingContext
    drawingContext.drawScope.apply {
        data.forEach { p ->
            val center = Offset(xInter.interpolate(p.x),
                                yInter.interpolate(p.y))
            drawCircle(color, 15f, center)
        }
    }
}


@Composable
@Preview(
    device = Devices.PIXEL_3,
    showBackground = true,
    uiMode = Configuration.ORIENTATION_LANDSCAPE
)
fun LineGraphPreview() {
    MaterialTheme {
        LineGraph(
            modifier = Modifier.fillMaxSize(),
            data = listOf(
                Point(0f, 0f),
                Point(1f, 10f),
                Point(2f, 6.4f),
                Point(3f, 16.44f)
            )
        )
    }
}