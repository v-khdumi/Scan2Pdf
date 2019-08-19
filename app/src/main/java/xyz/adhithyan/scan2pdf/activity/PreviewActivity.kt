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
import android.view.MenuItem
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
import java.io.File
import java.io.FileOutputStream
import java.util.*

class PreviewActivity : AppCompatActivity() {
    var i = 0
    val n = ResultHolder.images!!.size
    external fun getMagicColorBitmap(bitmap: Bitmap): Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        setSupportActionBar(toolbar)

        title = "Preview - Scan2Pdf"
        setBitmap(BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!))
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

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), System.currentTimeMillis().toString() + ".pdf")
        file.createNewFile()

        Observable.just<Unit>(Unit)
                .map { PdfUtil(file.absolutePath, ResultHolder.images!!).createPdf() }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Toast.makeText(this, "Pdf created successfully.", Toast.LENGTH_LONG).show()
                    progress.dismiss()
                }
    }

    fun previousImage() {
        i -= 1
        setCurrentImage()
    }

    fun nextImage() {
        i += 1
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


    fun convertToBw() {
        val progress = getProgressBar("Converting to black & white..")
        progress.show()

        Observable.just<Unit>(Unit)
                .map { ImageUtil.convertToGrayscale(ResultHolder.images?.get(Math.abs(i % n))!!) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    ResultHolder.images!![Math.abs(i % n)] = it.toByteArray()
                    setBitmap(it)
                    progress.dismiss()
                }
    }

    private fun setCurrentImage() {
        val currentImage = ResultHolder.images?.get(Math.abs(i % n))
        val currentImageSize = currentImage?.size!!
        setBitmap(BitmapFactory.decodeByteArray(currentImage, 0, currentImageSize))
    }

    private fun setBitmap(image: Bitmap) {
        ResultHolder.currentImageWidth = image.width
        ResultHolder.currentImageHeight = image.height

        previewImage.setImageBitmap(image)

        val mat = Mat(Size(image.width.toDouble(), image.height.toDouble()), CvType.CV_8U)
        Utils.bitmapToMat(image, mat)
        //detectDocument(mat, image)
        detectDocument()
    }

    private fun setCroppedImage(data: Intent?) {
        val result = UCrop.getOutput(data!!)
        val croppedImage = FileUtil.getByteArray(result!!, contentResolver)
        ResultHolder.images!![Math.abs(i % n)] = croppedImage
        previewImage.setImageBitmap(BitmapFactory.decodeByteArray(croppedImage, 0, croppedImage.size))
    }

    private fun setImageviewSwypeListener() {
        previewImage.setOnTouchListener(object : SwypeListener(this@PreviewActivity) {
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
                    cropImage()
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
                    setBitmap(it)
                    progress.dismiss()
                }
    }

    private fun detectDocument() {
        val tmpBitmap = ((previewImage.drawable) as BitmapDrawable).bitmap
        /*val scaledBitmap = scaledBitmap(tmpBitmap, previewImage.width, previewImage.height)
        previewImage.setImageBitmap(scaledBitmap)*/

        val edgepoints = edgePoints(tmpBitmap)
        previewImage.setPoints(edgepoints)
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
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true);
    }

}
