package xyz.adhithyan.scan2pdf.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_preview.*
import kotlinx.android.synthetic.main.content_preview.*
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import ru.whalemare.sheetmenu.SheetMenu
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R
import xyz.adhithyan.scan2pdf.extensions.applyMagicFilter
import xyz.adhithyan.scan2pdf.extensions.getProgressBar
import xyz.adhithyan.scan2pdf.extensions.toByteArray
import xyz.adhithyan.scan2pdf.listeners.SwypeListener
import xyz.adhithyan.scan2pdf.util.*
import xyz.adhithyan.scan2pdf.views.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.lang.NullPointerException
import java.util.*

class PreviewActivity : AppCompatActivity() {
    var i = 0
    val n = ResultHolder.images!!.size
    external fun getMagicColorBitmap(bitmap: Bitmap): Bitmap
    private lateinit var currentBitmap: Bitmap
    private lateinit var imagesCropped: BooleanArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        setSupportActionBar(toolbar)

        title = "Preview - Scan2Pdf"
        imagesCropped = BooleanArray(n) {false}
        setBitmap(BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!), true)
        bottomNavigationPreview.setOnNavigationItemSelectedListener(bottomNavigationClickListener)
        setImageviewSwypeListener()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                if (requestCode == UCrop.REQUEST_CROP) {
                    setCroppedImage(data)
                }
            }
            UCrop.RESULT_ERROR -> {
                Toast.makeText(this, "Error occured while cropping.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun saveAsPdf() {
        val progress = getProgressBar("Creating pdf..")
        progress.show()

        val file = createNewPdfFile()

        Observable.just<Unit>(Unit)
                .map { PdfUtil(file.absolutePath, ResultHolder.images!!).createPdf() }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(this, "Pdf created successfully.", Toast.LENGTH_LONG).show()
                    progress.dismiss()
                    finish()
                    val intent = Intent(this@PreviewActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent)
                }
    }

    fun previousImage() {
        if(i > 0){
            i-=1
        }

        if(i == 0) {
            i=n-1
        }
        setCurrentImage()
    }

    fun nextImage() {
        if(i<n-1) {
            i += 1
        }

        if(i == n-1) {
            i = 0
        }
        setCurrentImage()
    }

    fun cropImage() {
        val srcFile = FileUtil.createFile("temp_crop_src.jpg", filesDir)
        if (srcFile.exists()) {
            srcFile.delete()
        }
        srcFile.createNewFile()
        val fos = FileOutputStream(srcFile)
        fos.write(ResultHolder.images?.get(Math.abs(i % n)))

        val destFile = File(filesDir, "temp_crop_dest.jpg")
        if (destFile.exists()) {
            destFile.delete()
        }
        destFile.createNewFile()
        UCrop.of(Uri.fromFile(srcFile), Uri.fromFile(destFile))
                //.withAspectRatio(16F, 9F)
                .withMaxResultSize(ResultHolder.currentImageWidth, ResultHolder.currentImageHeight)
                .start(this)
    }

    fun performCrop() {
        val points = polygonView.getPoints()
        val currentBitmap = (previewImage.drawable as BitmapDrawable).bitmap
        val xRatio = ResultHolder.currentImageWidth.toFloat() / previewImage.width.toFloat()
        val yRatio = ResultHolder.currentImageHeight.toFloat() / previewImage.height.toFloat()

        val x1 = points.get(0)?.x!! * xRatio
        val x2 = points.get(1)?.x!! * xRatio
        val x3 = points.get(2)?.x!! * xRatio
        val x4 = points.get(3)?.x!! * xRatio
        val y1 = points.get(0)?.y!! * yRatio
        val y2 = points.get(1)?.y!! * yRatio
        val y3 = points.get(2)?.y!! * yRatio
        val y4 = points.get(3)?.y!! * yRatio

        val croppedBitmap = DocumentUtil().getScannedBitmap(currentBitmap, x1, y1, x2, y2, x3, y3, x4, y4)
        setBitmap(croppedBitmap, false)
        ResultHolder.images!![i] =  croppedBitmap.toByteArray()
        polygonView.visibility = View.INVISIBLE
    }

    fun convertToBw() {
        val progress = getProgressBar("Converting to black & white..")
        progress.show()

        Observable.just<Unit>(Unit)
                .map { ImageUtil.convertToGrayscale(ResultHolder.images?.get(Math.abs(i % n))!!) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    ResultHolder.images!![Math.abs(i % n)] = it.toByteArray()
                    setBitmap(it, true)
                    progress.dismiss()
                }
    }

    private fun setCurrentImage() {
        val currentImage = ResultHolder.images?.get(Math.abs(i % n))
        val currentImageSize = currentImage?.size!!
        setBitmap(BitmapFactory.decodeByteArray(currentImage, 0, currentImageSize), true)
    }

    private fun setBitmap(image: Bitmap, detectDoc: Boolean) {
        ResultHolder.currentImageWidth = image.width
        ResultHolder.currentImageHeight = image.height

        previewImage.setImageBitmap(image)

        val mat = Mat(Size(image.width.toDouble(), image.height.toDouble()), CvType.CV_8U)
        Utils.bitmapToMat(image, mat)

        holderImageCrop.post {
            if(detectDoc) {
                try {
                    detectDocument()
                } catch (ex: NullPointerException) {

                }
            }
        }
    }

    private fun setCroppedImage(data: Intent?) {
        val result = UCrop.getOutput(data!!)
        val croppedImage = FileUtil.getByteArray(result!!, contentResolver)
        ResultHolder.images!![Math.abs(i % n)] = croppedImage
        previewImage.setImageBitmap(BitmapFactory.decodeByteArray(croppedImage, 0, croppedImage.size))
    }

    private fun setImageviewSwypeListener() {
        frameLayout.setOnTouchListener(object : SwypeListener(this@PreviewActivity) {
            override fun onSwipeTop() {}

            override fun onSwipeRight() {
                nextImage()
            }

            override fun onSwipeLeft() {
                previousImage()
            }

            override fun onSwipeBottom() {}
        })
    }

    private val bottomNavigationClickListener by lazy {
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.preview_menu_crop -> {
                    performCrop()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.preview_menu_filters -> {
                    showPhotoFilters()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.preview_menu_convert -> {
                    saveAsPdf()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    private fun showPhotoFilters() {
        SheetMenu().apply {
            titleId = R.string.sheet_menu_title
            click = MenuItem.OnMenuItemClickListener {
                when (it.itemId) {
                    R.id.filter_bw -> {
                        convertToBw();true
                    }
                    R.id.filter_magic -> {
                        magicFilter(); true
                    }
                    else -> {
                        true
                    }
                }
            }
            menu = R.menu.photo_filters
        }.show(this)
    }

    private fun magicFilter() {
        val progress = this.getProgressBar("Applying magic filter ..")
        progress.show()

        val currentImage = ResultHolder.images?.get(Math.abs(i % n))
        val bitmap = BitmapFactory.decodeByteArray(currentImage, 0, currentImage!!.size)

        Observable.just<Unit>(Unit)
                .map { bitmap.applyMagicFilter() }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    ResultHolder.images!![Math.abs(i % n)] = it.toByteArray()
                    setBitmap(it, true)
                    progress.dismiss()
                }
    }

    private fun detectDocument() {
        var tmpBitmap = ((previewImage.drawable) as BitmapDrawable).bitmap
        val scaledBitmap = scaledBitmap(tmpBitmap, holderImageCrop.width, holderImageCrop.height)
        previewImage.setImageBitmap(scaledBitmap)
        ResultHolder.currentImageHeight = scaledBitmap.height
        ResultHolder.currentImageWidth = scaledBitmap.width

        if(!imagesCropped[i]) {
            tmpBitmap = scaledBitmap
            val edgepoints = edgePoints(tmpBitmap)
            polygonView.setPoints(edgepoints)
            polygonView.visibility = View.VISIBLE

            val padding = resources.getDimension(R.dimen.fab_margin).toInt()
            val layoutParams = FrameLayout.LayoutParams(tmpBitmap.getWidth() + 2 * padding, tmpBitmap.getHeight() + 2 * padding)
            layoutParams.gravity = Gravity.CENTER

            polygonView.layoutParams = layoutParams
        }
    }

    fun edgePoints(bitmap: Bitmap): Map<Int, PointF> {
        val pointsF = DocumentUtil().getContourEdgePoints(bitmap)
        val orderedPoints = orderedValidEdgePoints(bitmap, pointsF)
        return orderedPoints
    }


    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        val outlinePoints = HashMap<Int, PointF>()
        outlinePoints.put(0, PointF(0f, 0f))
        outlinePoints.put(1, PointF(tempBitmap.width.toFloat(), 0f))
        outlinePoints.put(2, PointF(0f, tempBitmap.height.toFloat()))
        outlinePoints.put(3, PointF(tempBitmap.width.toFloat(), tempBitmap.height.toFloat()))
        return outlinePoints
    }

    private fun orderedValidEdgePoints(tempBitmap: Bitmap, pointFs: List<PointF>): Map<Int, PointF> {
        var orderedPoints = getOrderedPoints(pointFs);
        if (isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap);
        }
        return orderedPoints;
    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val m = Matrix()
        m.setRectToRect(RectF(0F, 0F, bitmap.width.toFloat(), bitmap.height.toFloat()), RectF(0F, 0F, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.CENTER)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

}
