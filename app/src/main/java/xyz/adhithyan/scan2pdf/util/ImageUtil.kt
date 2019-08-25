package xyz.adhithyan.scan2pdf.util

import android.graphics.*
import com.itextpdf.text.Rectangle
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.util.*


class ImageUtil {
    companion object {
        val THRESHOLD = 230

        fun calculateFitSize(originalWidth: Float, originalHeight: Float, documentSize: Rectangle): Rectangle {
            val widthChange = (originalWidth - documentSize.width) / originalWidth
            val heightChange = (originalHeight - documentSize.height) / originalHeight

            val changeFactor = if (heightChange >= widthChange) heightChange else widthChange
            val newWidth = (originalWidth - originalWidth * changeFactor).toInt()
            val newHeight = (originalHeight - originalHeight * changeFactor).toInt()

            return Rectangle(Math.abs(newWidth).toFloat(), Math.abs(newHeight).toFloat())
        }

        fun convertToGrayscale(image: ByteArray): Bitmap {
            val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
            val height = bitmap.height
            val width = bitmap.width

            val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val c = Canvas(bmpGrayscale)
            val paint = Paint()
            val cm = ColorMatrix()
            cm.setSaturation(0f)
            paint.colorFilter = ColorMatrixColorFilter(cm)
            c.drawBitmap(bitmap, 0F, 0F, paint)
            return bmpGrayscale
        }

        private fun colorToRgb(alpha: Int, red: Int, green: Int, blue: Int): Int {
            var newPixel = 0
            newPixel += alpha
            newPixel = newPixel shl 8
            newPixel += red
            newPixel = newPixel shl 8
            newPixel += green
            newPixel = newPixel shl 8
            newPixel += blue
            return newPixel
        }
    }
}

fun rotateBitmap(original: Bitmap, angle: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(90.toFloat())
    return Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
}

fun bitmapToMat(bitmap: Bitmap): Mat {
    val mat = Mat(bitmap.height, bitmap.width, CvType.CV_8U, Scalar(4.0))
    val bitmap32 = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    Utils.bitmapToMat(bitmap32, mat)
    return mat
}

fun matToBitmap(mat: Mat): Bitmap {
    val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(mat, bitmap)
    return bitmap
}


class PerspectiveTransformation {

    fun transform(src: Mat, corners: MatOfPoint2f): Mat {
        val sortedCorners = sortCorners(corners)
        val size = getRectangleSize(sortedCorners)

        val result = Mat.zeros(size, src.type())
        val imageOutline = getOutline(result)

        val transformation = Imgproc.getPerspectiveTransform(sortedCorners, imageOutline)
        Imgproc.warpPerspective(src, result, transformation, size)

        return result
    }

    private fun getRectangleSize(rectangle: MatOfPoint2f): Size {
        val corners = rectangle.toArray()

        val top = getDistance(corners[0], corners[1])
        val right = getDistance(corners[1], corners[2])
        val bottom = getDistance(corners[2], corners[3])
        val left = getDistance(corners[3], corners[0])

        val averageWidth = (top + bottom) / 2f
        val averageHeight = (right + left) / 2f

        return Size(Point(averageWidth, averageHeight))
    }

    private fun getDistance(p1: Point, p2: Point): Double {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return Math.sqrt(dx * dx + dy * dy)
    }

    private fun getOutline(image: Mat): MatOfPoint2f {
        val topLeft = Point(0.0, 0.0)
        val topRight = Point(image.cols().toDouble(), 0.0)
        val bottomRight = Point(image.cols().toDouble(), image.rows().toDouble())
        val bottomLeft = Point(0.0, image.rows().toDouble())
        val points = arrayOf(topLeft, topRight, bottomRight, bottomLeft)

        val result = MatOfPoint2f()
        result.fromArray(*points)

        return result
    }

    private fun sortCorners(corners: MatOfPoint2f): MatOfPoint2f {
        val center = getMassCenter(corners)
        val points = corners.toList()
        val topPoints = ArrayList<Point>()
        val bottomPoints = ArrayList<Point>()

        for (point in points) {
            if (point.y < center.y) {
                topPoints.add(point)
            } else {
                bottomPoints.add(point)
            }
        }

        val topLeft = if (topPoints[0].x > topPoints[1].x) topPoints[1] else topPoints[0]
        val topRight = if (topPoints[0].x > topPoints[1].x) topPoints[0] else topPoints[1]
        val bottomLeft = if (bottomPoints[0].x > bottomPoints[1].x) bottomPoints[1] else bottomPoints[0]
        val bottomRight = if (bottomPoints[0].x > bottomPoints[1].x) bottomPoints[0] else bottomPoints[1]

        val result = MatOfPoint2f()
        val sortedPoints = arrayOf(topLeft, topRight, bottomRight, bottomLeft)
        result.fromArray(*sortedPoints)

        return result
    }

    private fun getMassCenter(points: MatOfPoint2f): Point {
        var xSum = 0.0
        var ySum = 0.0
        val pointList = points.toList()
        val len = pointList.size
        for (point in pointList) {
            xSum += point.x
            ySum += point.y
        }
        return Point(xSum / len, ySum / len)
    }

    companion object {
        private val DEBUG_TAG = "PerspectiveTransformation"
    }

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
        orderedPoints.put(index, pointF)
    }
    return orderedPoints
}

fun isValidShape(pointFMap: Map<Int, PointF>): Boolean {
    return pointFMap.size == 4
}

private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int, PointF> {
    val outlinePoints = HashMap<Int, PointF>()
    outlinePoints.put(0, PointF(0f, 0f))
    outlinePoints.put(1, PointF(tempBitmap.width.toFloat(), 0f))
    outlinePoints.put(2, PointF(0f, tempBitmap.height.toFloat()))
    outlinePoints.put(3, PointF(tempBitmap.width.toFloat(), tempBitmap.height.toFloat()))
    return outlinePoints
}
