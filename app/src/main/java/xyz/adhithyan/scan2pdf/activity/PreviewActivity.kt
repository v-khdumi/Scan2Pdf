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
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import com.yalantis.ucrop.UCrop
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_preview.*
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.core.Point
import ru.whalemare.sheetmenu.SheetMenu

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

import xyz.adhithyan.scan2pdf.R
import xyz.adhithyan.scan2pdf.extensions.applyMagicFilter
import xyz.adhithyan.scan2pdf.extensions.getProgressBar
import xyz.adhithyan.scan2pdf.extensions.toByteArray
import xyz.adhithyan.scan2pdf.listeners.SwypeListener
import xyz.adhithyan.scan2pdf.util.*
import xyz.adhithyan.scan2pdf.views.DocumentCanvas
import xyz.adhithyan.scan2pdf.views.PolygonView
import java.io.File
import java.io.FileOutputStream

class PreviewActivity : AppCompatActivity() {
    internal var selectedImageBitmap: Bitmap? = null
    internal var btnImageEnhance: Button? = null
    private lateinit var nativeClass: NativeClass
    private lateinit var polyView: PolygonView
    var i = 0
    val n = ResultHolder.images?.size!!

    private val btnImageEnhanceClick = View.OnClickListener {
        //save selected bitmap to our constants
        //this method will save the image to our device memory
        //so set this variable to null after the image is no longer used
        /*MyConstants.selectedImageBitmap = getCroppedImage();

            //create new intent to start process image
            Intent intent = new Intent(getApplicationContext(), ImageEnhanceActivity.class);
            startActivity(intent);*/
    }

    protected val croppedImage: Bitmap
        get() {

            val points = polyView.points

            val xRatio = selectedImageBitmap!!.width.toFloat() / imageView.width
            val yRatio = selectedImageBitmap!!.height.toFloat() / imageView.height

            val x1 = points[0]?.x!! * xRatio
            val x2 = points[1]?.x!! * xRatio
            val x3 = points[2]?.x!! * xRatio
            val x4 = points[3]?.x!! * xRatio
            val y1 = points[0]?.y!! * yRatio
            val y2 = points[1]?.y!! * yRatio
            val y3 = points[2]?.y!! * yRatio
            val y4 = points[3]?.y!! * yRatio

            return nativeClass.getScannedBitmap(selectedImageBitmap, x1, y1, x2, y2, x3, y3, x4, y4)

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        initializeElement()
        bottomNavigationPreview.setOnNavigationItemSelectedListener(bottomNavigationClickListener)
    }

    private fun initializeElement() {
        nativeClass = NativeClass()
        //btnImageEnhance = findViewById(R.id.btnImageEnhance);
        polyView = polygonView as PolygonView
        //holderImageCrop.post { initializeCropping() }
        initializeCropping()
        //btnImageEnhance.setOnClickListener(btnImageEnhanceClick);
        setImageviewSwypeListener()
    }

    private fun initializeCropping() {

        selectedImageBitmap = BitmapFactory.decodeByteArray(ResultHolder.image!!, 0, ResultHolder.image?.size!!)
        imageView.setImageBitmap(selectedImageBitmap)
        //MyConstants.selectedImageBitmap = null;

        val scaledBitmap = scaledBitmap(selectedImageBitmap!!, selectedImageBitmap?.width!!, selectedImageBitmap?.height!!)
        imageView.setImageBitmap(scaledBitmap)

        val tempBitmap = (imageView.drawable as BitmapDrawable).bitmap
        val pointFs = getEdgePoints(scaledBitmap)

        polyView.points = pointFs
        polyView.visibility = View.VISIBLE

        val padding = resources.getDimension(R.dimen.scanPadding).toInt()

        val layoutParams = FrameLayout.LayoutParams(tempBitmap.width + 2 * padding, tempBitmap.height + 2 * padding)
        layoutParams.gravity = Gravity.CENTER

        polyView.layoutParams = layoutParams

    }

    private fun scaledBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        Log.v("aashari-tag", "scaledBitmap")
        Log.v("aashari-tag", "$width $height")
        val m = Matrix()
        m.setRectToRect(RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()), RectF(0f, 0f, width.toFloat(), height.toFloat()), Matrix.ScaleToFit.CENTER)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
    }

    private fun getEdgePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        Log.v("aashari-tag", "getEdgePoints")
        val pointFs = getContourEdgePoints(tempBitmap)
        return orderedValidEdgePoints(tempBitmap, pointFs)
    }

    private fun getContourEdgePoints(tempBitmap: Bitmap): List<PointF> {
        Log.v("aashari-tag", "getContourEdgePoints")

        val point2f = nativeClass.getPoint(tempBitmap)
        val points = Arrays.asList(*point2f!!.toArray())

        val result = ArrayList<PointF>()
        for (i in points.indices) {
            result.add(PointF(points[i].x.toFloat(), points[i].y.toFloat()))
        }

        return result

    }

    private fun getOutlinePoints(tempBitmap: Bitmap): Map<Int, PointF> {
        Log.v("aashari-tag", "getOutlinePoints")
        val outlinePoints = HashMap<Int, PointF>()
        outlinePoints[0] = PointF(0f, 0f)
        outlinePoints[1] = PointF(tempBitmap.width.toFloat(), 0f)
        outlinePoints[2] = PointF(0f, tempBitmap.height.toFloat())
        outlinePoints[3] = PointF(tempBitmap.width.toFloat(), tempBitmap.height.toFloat())
        return outlinePoints
    }

    private fun orderedValidEdgePoints(tempBitmap: Bitmap, pointFs: List<PointF>): Map<Int, PointF> {
        Log.v("aashari-tag", "orderedValidEdgePoints")
        var orderedPoints = polyView.getOrderedPoints(pointFs)
        if (!polyView.isValidShape(orderedPoints)) {
            orderedPoints = getOutlinePoints(tempBitmap)
        }
        return orderedPoints
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                if(requestCode == UCrop.REQUEST_CROP) {
                    setCroppedImage(data)
                }
            }
            UCrop.RESULT_ERROR -> { Toast.makeText(this, "Error occured while cropping.", Toast.LENGTH_LONG).show() }
        }
    }

    fun saveAsPdf() {
        val progress = getProgressBar("Creating pdf..")
        progress.show()

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),System.currentTimeMillis().toString() + ".pdf")
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
        if(srcFile.exists()) {
            srcFile.delete()
        }
        srcFile.createNewFile()
        val fos = FileOutputStream(srcFile)
        fos.write(ResultHolder.images?.get(Math.abs(i % n)))

        val destFile = File(filesDir, "temp_crop_dest.jpg")
        if(destFile.exists()) {
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

        imageView.setImageBitmap(image)
    }

    private fun setCroppedImage(data: Intent?) {
        val result = UCrop.getOutput(data!!)
        val croppedImage = FileUtil.getByteArray(result!!, contentResolver)
        ResultHolder.images!![Math.abs(i % n)] = croppedImage
        imageView.setImageBitmap(BitmapFactory.decodeByteArray(croppedImage, 0, croppedImage.size))
    }

    private fun setImageviewSwypeListener() {
        imageView.setOnTouchListener(object : SwypeListener(this@PreviewActivity) {
            override fun onSwipeTop() { }

            override fun onSwipeRight() { nextImage() }

            override fun onSwipeLeft() { previousImage() }

            override fun onSwipeBottom() { }
        })
    }

    private val bottomNavigationClickListener by lazy {
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.preview_menu_crop -> {
                    //cropImage()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.preview_menu_filters-> {
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
                    R.id.filter_bw -> { convertToBw();true }
                    R.id.filter_magic -> { magicFilter(); true }
                    else -> {true}
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
}
