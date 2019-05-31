package xyz.adhithyan.scan2pdf.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView


internal class CropImageView : ImageView {
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var image3: ImageView
    private lateinit var image4: ImageView
    private lateinit var paint: Paint

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    /*override fun onDraw(canvas: Canvas) {
      super.onDraw(canvas)
    }*/

    private fun init() {
        image1 = getImageView(0, 0)
        image2 = getImageView(width, 0)
        image3 = getImageView(0, height)
        image4 = getImageView(width, height)


        initPaint()
    }

    private fun initPaint() {
        paint = Paint()
        paint.color = resources.getColor(xyz.adhithyan.scan2pdf.R.color.red)
        paint.setStrokeWidth(2F)
        paint.isAntiAlias = true
    }

    private fun getImageView(x: Int, y: Int): ImageView {
        val imageView = ImageView(context)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.layoutParams = layoutParams
        //imageView.setImageResource(R.drawable.circle)
        imageView.x = x.toFloat()
        imageView.y = y.toFloat()
        //imageView.setOnTouchListener(TouchListenerImpl())
        return imageView
    }

    fun setPoints(pointMap: Map<Int, PointF>) {
        if (pointMap.size == 4) {
            setPointInView(pointMap)
        }
    }

    private fun setPointInView(pointFMap: Map<Int, PointF>) {
        image1.setX(pointFMap[0]?.x!!)
        image1.setY(pointFMap[0]?.y!!)

        image2.setX(pointFMap[1]?.x!!)
        image2.setY(pointFMap[1]?.y!!)

        image3.setX(pointFMap[2]?.x!!)
        image3.setY(pointFMap[2]?.y!!)

        image4.setX(pointFMap[3]?.x!!)
        image4.setY(pointFMap[3]?.y!!)
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        canvas.drawLine(image1.x + (image1.width / 2), image1.y + (image1.height / 2), image3.x + (image3.width / 2), image3.y + (image3.height / 2), paint)
        canvas.drawLine(image1.x + (image1.width / 2), image1.y + (image1.height / 2), image2.x + (image2.width / 2), image2.y + (image2.height / 2), paint)
        canvas.drawLine(image2.x + (image2.width / 2), image2.y + (image2.height / 2), image4.x + (image4.width / 2), image4.y + (image4.height / 2), paint)
        canvas.drawLine(image3.x + (image3.width / 2), image3.y + (image3.height / 2), image4.x + (image4.width / 2), image4.y + (image4.height / 2), paint)
    }
}