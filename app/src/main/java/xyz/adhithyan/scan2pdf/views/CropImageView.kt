package xyz.adhithyan.scan2pdf.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.ImageView


internal class CropImageView: ImageView {

  constructor(context: Context) : super(context) {}

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    /*val paint = Paint().apply {
      color = Color.BLACK
      style = Paint.Style.FILL_AND_STROKE
      strokeWidth = 10F
    }

    val leftx = 20f
    val topy = 20f
    val rightx = 50f
    val bottomy = 100f
    canvas.drawRect(leftx, topy, rightx, bottomy, paint)*/
  }
}