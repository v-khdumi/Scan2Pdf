package xyz.adhithyan.scan2pdf.util

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.util.*
import kotlin.collections.ArrayList


class DocumentDetection(internal val input: Mat) {

  fun detectDocument(): Boolean {
    //val contours = findContours()

    val quad = findContours()
    if(quad != null) {
      /*val c = quad.contour
      var previewPoints = Array<Point>(4, {Point()})

      var rescaledPoints = Array<Point>(4, {Point()})
      val ratio = input.size().height / 500
      for (i in 0..3) {
        val x = java.lang.Double.valueOf(quad.points[i].x * ratio)
        val y = java.lang.Double.valueOf(quad.points[i].y * ratio)
        rescaledPoints[i] = Point(x, y)
      }
      previewPoints = rescaledPoints*/
      ResultHolder.scannedDoc = Document(input, null, quad, quad.points, input.size())
      return true
    } else {
      val doc = Mat(input.size(), CvType.CV_8UC4)
      input.copyTo(doc)
      return true
    }
    return false
  }

  private fun findContours(): Quadrilateral? {
    var height = (input.size().height)
    var width = (input.size().width)
    val ratio = (800/width)
    val size = Size(ratio * height, height)

    val resizedImage = Mat(size, CvType.CV_8UC4)
    val grayImage = Mat(size, CvType.CV_8UC4)
    val cannedImage = Mat(size,  CvType.CV_8UC1)
    val bilateralFilterImage = Mat(size, CvType.CV_8UC4)

    Imgproc.resize(input, resizedImage, size)
    Imgproc.cvtColor(input, input, Imgproc.COLOR_RGBA2GRAY)
    Imgproc.bilateralFilter(input, bilateralFilterImage, 9, 75.0, 75.0)
    Imgproc.adaptiveThreshold(bilateralFilterImage, input, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 115, 4.0)
    Imgproc.medianBlur(input, input, 11)
    Core.copyMakeBorder(input, input, 5, 5, 5, 5, Core.BORDER_CONSTANT, Scalar(0.0,0.0,0.0))
    Imgproc.Canny(input, input, 75.toDouble(), 200.toDouble())

    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()

    Imgproc.findContours(input, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE)
    hierarchy.release()


    /*Collections.sort(contours, object : Comparator<MatOfPoint> {
      override fun compare(lhs: MatOfPoint, rhs: MatOfPoint): Int {
        return java.lang.Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs))
      }
    })*/

    resizedImage.release()
    grayImage.release()
    cannedImage.release()
    bilateralFilterImage.release()

    height = input.size().width
    width = input.size().height
    var MAX_CONTOUR_AREA = (width - 10) * (height - 10)
    var maxAreaFound = MAX_CONTOUR_AREA * 0.5
    var pageContour = arrayOf(arrayOf(5, 5), arrayOf(5, height-5), arrayOf(width-5, height-5), arrayOf(width-5, 5))
    var perimeter = 0.0
    var area = 0.0
    var quadrilateral: Quadrilateral? = null
    for(contour in contours) {
      try {
        perimeter = Imgproc.arcLength(MatOfPoint2f(*contour.toArray()), true)
        val approx = MatOfPoint2f()
        val cont = MatOfPoint2f(*contour.toArray())
        Imgproc.approxPolyDP(cont, approx, 0.03 * perimeter, true)
        area = Imgproc.contourArea(approx)
        val approxArray = approx.toArray()
        val approx1 = MatOfPoint()
        approx.convertTo(approx1, CvType.CV_32S);
        val isConvex = Imgproc.isContourConvex(approx1)

        if(area > maxAreaFound && area < MAX_CONTOUR_AREA && approxArray.size == 4 && isConvex) {
          maxAreaFound = area
          pageContour = arrayOf(arrayOf(approxArray[0]), arrayOf(approxArray[1]), arrayOf(approxArray[2]), arrayOf(approxArray[3]))
          val points = approx.toArray()
          val detectedPoints = sortPoints(points)
          quadrilateral = Quadrilateral(contour, detectedPoints, ratio)
        }
      } catch (ex: Exception) {
        val message = ex.message;
        ex.printStackTrace()
        message;
      }

    }
    perimeter
    pageContour
    resizedImage.release()
    bilateralFilterImage.release()

    //return pageContour
    if(quadrilateral == null && pageContour.size == 4) {
      //val array = arrayOf(Point(pageContour[0]))
      quadrilateral = Quadrilateral(null, arrayOf(Point(5.0, 5.0), Point(5.0, height - 5), Point(width-5, height-5), Point(width-5, 5.0)), ratio=ratio)
    }
    return quadrilateral
  }

  /*private fun getQuadrilateral(contours: ArrayList<MatOfPoint>, srcSize: Size): Quadrilateral? {
    val ratio = (srcSize.height / 500).toDouble()
    val height = (srcSize.height / ratio)
    val width = (srcSize.width / ratio)

    var maxArea = 0
    var quadrilateral: Quadrilateral? = null
    for(c in contours) {
      val area = Imgproc.contourArea(c)
      if(area.toInt() > 100) {
        val c2f = MatOfPoint2f(*c.toArray())
        val perimeter = Imgproc.arcLength(c2f, true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(c2f, approx, 0.02 * perimeter, true)
        if(area > maxArea && approx.toArray().size == 4) {
          maxArea = area.toInt()
          val points = approx.toArray()
          val detectedPoints = sortPoints(points)
          quadrilateral = Quadrilateral(c, detectedPoints)
        }
      }
    }
    quadrilateral;
    maxArea;
    return quadrilateral
  }

  private fun getQuadrilateral(contours: ArrayList<MatOfPoint>, srcSize: Size): Quadrilateral? {
    val ratio = (srcSize.height / 500).toDouble()
    val height = (srcSize.height / ratio)
    val width = (srcSize.width / ratio)

    var maxArea = 0
    var quadrilateral: Quadrilateral? = null
    for(c in contours) {
      val area = Imgproc.contourArea(c)
      if(area.toInt() > 100) {
        val c2f = MatOfPoint2f(*c.toArray())
        val perimeter = Imgproc.arcLength(c2f, true)
        val approx = MatOfPoint2f()
        Imgproc.approxPolyDP(c2f, approx, 0.02 * perimeter, true)
        if(area > maxArea && approx.toArray().size == 4) {
          maxArea = area.toInt()
          val points = approx.toArray()
          val detectedPoints = sortPoints(points)
          quadrilateral = Quadrilateral(c, detectedPoints)
        }
      }
    }
    return quadrilateral
  }*/

    fun contourOffset(cnt: Array<Point>, offset: Point): Array<Point> {
        cnt.plus(offset)
        return cnt
    }

    private fun getQuadrilateral2(contours: ArrayList<MatOfPoint>, srcSize: Size): detected? {
        val height = srcSize.height
        val width = srcSize.width


        var maxContourArea = (width - 10) * (height - 10)
        var maxArea = maxContourArea * 0.5
        var quadrilateral: detected? = null
        var pageContour = arrayOf(Point(5.0, 5.0), Point(5.0, height-5), Point(width-5, height-5), Point(width-5, 5.0))
        for(cnt in contours) {
            val cont = MatOfPoint2f(*cnt.toArray())
            val perimeter = Imgproc.arcLength(cont, true)
            val approx = MatOfPoint2f()
            Imgproc.approxPolyDP(cont, approx, 0.03* perimeter, true)
            val approxContourArea = Imgproc.contourArea(approx)
            if(approx.toArray().size == 4 && Imgproc.isContourConvex(MatOfPoint(*approx.toArray())) && (maxArea < approxContourArea) && (approxContourArea< maxContourArea)) {
                maxArea = Imgproc.contourArea(approx)
                pageContour = approx.toArray()
            }
        }
        pageContour = sortPoints(pageContour)
        pageContour = contourOffset(pageContour, Point(-5.0, -5.0))
        quadrilateral = detected(points = pageContour)
        return quadrilateral
    }
  private fun sortPoints(src: Array<Point>): Array<Point> {
    // .inbuilt method didnt work, so manual addition
    val srcPoints = ArrayList<Point>()
    for(s in src) { srcPoints.add(s) }

    val result = Array<Point>(4, {Point()})

    val sumComparator = Comparator<Point> { lhs, rhs -> java.lang.Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x) }

    val diffComparator = Comparator<Point> { lhs, rhs -> java.lang.Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x) }

    // top-left corner = minimal sum
    Collections.min(srcPoints, sumComparator)
    result[0] = Collections.min(srcPoints, sumComparator)

    // bottom-right corner = maximal sum
    result[2] = Collections.max(srcPoints, sumComparator)

    // top-right corner = minimal diference
    result[1] = Collections.min(srcPoints, diffComparator)

    // bottom-left corner = maximal diference
    result[3] = Collections.max(srcPoints, diffComparator)

    return result
  }

  fun detectDoc2() {
    val img = Mat()
    Imgproc.cvtColor(input, img, Imgproc.COLOR_BGR2RGB)
    var height = 800
      val ratio =  height / img.size().height
      var width = img.size().width
      //val size = Size(ratio * width, height.toDouble())
    //Imgproc.resize(img, img, size)
    Imgproc.bilateralFilter(img, img, 9, 75.0, 75.0)
      Imgproc.adaptiveThreshold(img, img, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 115, 4.0)
      Imgproc.medianBlur(img, img, 11)
      Imgproc.Canny(img, img, 200.0, 500.0)
      val contours = ArrayList<MatOfPoint>()
      val hierarchy = Mat()
      Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)
      height = img.size().height.toInt()
      width = img.size().width
      var MAX_CONTOUR_AREA = (width - 10) * (height - 10)
      var maxAreaFound = MAX_CONTOUR_AREA * 0.5
  }
}


data class detected(val points: Array<Point>)
data class Quadrilateral(val contour: MatOfPoint?, val points: Array<Point>, val ratio: Double)
data class Document(val original: Mat, val processed: Mat?, val quad: Quadrilateral?, val previewPoints: Array<Point>, val size: Size)
