package xyz.adhithyan.scan2pdf.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import xyz.adhithyan.scan2pdf.R
import java.util.HashMap


internal class CropImageView : FrameLayout {
    private lateinit var image1: ImageView
    private lateinit var image2: ImageView
    private lateinit var image3: ImageView
    private lateinit var image4: ImageView
    private lateinit var paint: Paint
    private lateinit  var midimage13: ImageView
    private lateinit var midimage12: ImageView
    private lateinit var midimage34: ImageView
    private lateinit var midimage24: ImageView
    private lateinit var cropImageView: CropImageView

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
        cropImageView = this
        image1 = getImageview(0, 0)
        image2 = getImageview(width, 0)
        image3 = getImageview(0, height)
        image4 = getImageview(width, height)

        midimage13 = getImageview(0, height / 2)
        midimage13.setOnTouchListener(MidPointTouchListenerImpl(image1, image3))

        midimage12 = getImageview(0, width / 2)
        midimage12.setOnTouchListener(MidPointTouchListenerImpl(image1, image2))

        midimage34 = getImageview(0, height / 2)
        midimage34.setOnTouchListener(MidPointTouchListenerImpl(image3, image4))

        midimage24 = getImageview(0, height / 2)
        midimage24.setOnTouchListener(MidPointTouchListenerImpl(image2, image4))

        addView(image1)
        addView(image2)
        addView(midimage13)
        addView(midimage12)
        addView(midimage34)
        addView(midimage24)
        addView(image3)
        addView(image4)
        initPaint()
    }

    private fun initPaint() {
        paint = Paint()
        paint.color = resources.getColor(xyz.adhithyan.scan2pdf.R.color.red)
        paint.setStrokeWidth(2F)
        paint.isAntiAlias = true
    }

    private fun getImageview(x: Int, y: Int): ImageView {
        val imageView = ImageView(context)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.layoutParams = layoutParams
        imageView.setImageResource(R.drawable.circle)
        imageView.x = x.toFloat()
        imageView.y = y.toFloat()
        imageView.setOnTouchListener(TouchListener())
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
        //invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        canvas.drawLine(image1.x + (image1.width / 2), image1.y + (image1.height / 2), image3.x + (image3.width / 2), image3.y + (image3.height / 2), paint)
        canvas.drawLine(image1.x + (image1.width / 2), image1.y + (image1.height / 2), image2.x + (image2.width / 2), image2.y + (image2.height / 2), paint)
        canvas.drawLine(image2.x + (image2.width / 2), image2.y + (image2.height / 2), image4.x + (image4.width / 2), image4.y + (image4.height / 2), paint)
        canvas.drawLine(image3.x + (image3.width / 2), image3.y + (image3.height / 2), image4.x + (image4.width / 2), image4.y + (image4.height / 2), paint)

        midimage13.x = image3.getX() - (image3.getX() - image1.getX()) / 2
        midimage13.y = image3.getY() - (image3.getY() - image1.getY()) / 2
        midimage24.x = image4.getX() - (image4.getX() - image2.getX()) / 2
        midimage24.y = image4.getY() - (image4.getY() - image2.getY()) / 2
        midimage34.x = image4.getX() - (image4.getX() - image3.getX()) / 2
        midimage34.y = image4.getY() - (image4.getY() - image3.getY()) / 2
        midimage12.x = image2.getX() - (image2.getX() - image1.getX()) / 2
        midimage12.y = image2.getY() - (image2.getY() - image1.getY()) / 2
    }

    override fun attachViewToParent(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.attachViewToParent(child, index, params)
    }

    fun isValidShape(pointFMap: Map<Int, PointF>): Boolean {
        return pointFMap.size == 4
    }

    fun getOrderedPoints(points: List<PointF>): Map<Int, PointF> {

        val centerPoint = PointF()
        val size = points.size
        for (pointF in points) {
            centerPoint.x += pointF.x / size
            centerPoint.y += pointF.y / size
        }
        val orderedPoints = HashMap<Int, PointF>()
        for (pointF in points) {
            var index = -1
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3
            }
            orderedPoints[index] = pointF
        }
        return orderedPoints
    }
    
    fun getPoints(): Map<Int, PointF> {
        val points = ArrayList<PointF>()
        points.add(PointF(image1.x, image1.y))
        points.add(PointF(image2.x, image2.y))
        points.add(PointF(image3.x, image3.y))
        points.add(PointF(image4.x, image4.y))
        
        return getOrderedPoints(points)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }
    
    private inner class TouchListener: View.OnTouchListener {
        internal var startPoint = PointF()
        internal var downPoint = PointF()

        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val eid = event.action

            when(eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - downPoint.x, event.y - downPoint.y)
                    if (startPoint.x + mv.x + view.getWidth().toFloat() < cropImageView.getWidth() && startPoint.y + mv.y + view.getHeight().toFloat() < cropImageView.getHeight() && startPoint.x + mv.x > 0 && startPoint.y + mv.y > 0) {
                        view.x = (startPoint.x + mv.x).toInt().toFloat()
                        view.y = (startPoint.y + mv.y).toInt().toFloat()
                        startPoint = PointF(view.x, view.y)
                    }
                }

                MotionEvent.ACTION_DOWN -> {
                    downPoint.x = event.x
                    downPoint.y = event.y
                    startPoint = PointF(view.x, view.y)
                }

                MotionEvent.ACTION_UP -> {
                    var color = 0
                    if(isValidShape(getPoints())) {
                        color = resources.getColor(R.color.blue)
                    } else {
                        color = resources.getColor(R.color.orange)
                    }
                    paint.color = color
                }

            }
            cropImageView.invalidate()
            return true
        }
    }

    private inner class MidPointTouchListenerImpl(private val mainimage1: ImageView, private val mainimage2: ImageView) : View.OnTouchListener {

        internal var DownPT = PointF() // Record Mouse Position When Pressed Down
        internal var StartPT = PointF() // Record Start Position of 'img'

        override fun onTouch(v: View, event: MotionEvent): Boolean {
            val eid = event.action
            when (eid) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - DownPT.x, event.y - DownPT.y)

                    if (Math.abs(mainimage1.x - mainimage2.x) > Math.abs(mainimage1.y - mainimage2.y)) {
                        if (mainimage2.y + mv.y + v.height.toFloat() < cropImageView.getHeight() && mainimage2.y + mv.y > 0) {
                            v.x = (StartPT.y + mv.y).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainimage2.y = (mainimage2.y + mv.y).toInt().toFloat()
                        }
                        if (mainimage1.y + mv.y + v.height.toFloat() < cropImageView.getHeight() && mainimage1.y + mv.y > 0) {
                            v.x = (StartPT.y + mv.y).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainimage1.y = (mainimage1.y + mv.y).toInt().toFloat()
                        }
                    } else {
                        if (mainimage2.x + mv.x + v.width.toFloat() < cropImageView.getWidth() && mainimage2.x + mv.x > 0) {
                            v.x = (StartPT.x + mv.x).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainimage2.x = (mainimage2.x + mv.x).toInt().toFloat()
                        }
                        if (mainimage1.x + mv.x + v.width.toFloat() < cropImageView.getWidth() && mainimage1.x + mv.x > 0) {
                            v.x = (StartPT.x + mv.x).toInt().toFloat()
                            StartPT = PointF(v.x, v.y)
                            mainimage1.x = (mainimage1.x + mv.x).toInt().toFloat()
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    DownPT.x = event.x
                    DownPT.y = event.y
                    StartPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    if (isValidShape(getPoints())) {
                        color = resources.getColor(R.color.blue)
                    } else {
                        color = resources.getColor(R.color.orange)
                    }
                    paint.color = color
                }
                else -> {
                }
            }
            cropImageView.invalidate()
            return true
        }
    }

}