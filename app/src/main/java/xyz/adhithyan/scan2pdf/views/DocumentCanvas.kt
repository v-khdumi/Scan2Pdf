package xyz.adhithyan.scan2pdf.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.shapes.PathShape
import android.util.Log
import android.view.View
import xyz.adhithyan.scan2pdf.extensions.showLongToast
import xyz.adhithyan.scan2pdf.util.ResultHolder
import java.lang.Exception

class DocumentCanvas(context: Context): View(context) {
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    try {
      val doc = ResultHolder?.scannedDoc
      val quad = doc?.quad
      val points = quad?.points!!
      val size = doc?.size

      if(doc != null) {
        //Log.v("HELLO", )
        val path = Path()

        val width = size?.width.toFloat()
        val height = size?.height.toFloat()
        path.moveTo((width - points[0].y).toFloat(), points[0].x.toFloat())
        path.lineTo((width - points[1].y).toFloat(), points[1].x.toFloat())
        path.lineTo((width - points[2].y).toFloat(), points[2].x.toFloat())
        path.lineTo((width - points[3].y).toFloat(), points[3].x.toFloat())
        path.close()

        val p = Paint()
        p.color = Color.BLUE
        p.strokeWidth = 5F


        canvas.drawPath(path, p)
        
      }
    } catch(ex: Exception) { context.showLongToast("excep")}
  }
}