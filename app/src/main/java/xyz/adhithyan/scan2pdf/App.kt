package xyz.adhithyan.scan2pdf

import android.app.Application
import android.content.Loader
import android.util.Log
import android.widget.Toast
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import xyz.adhithyan.scan2pdf.extensions.showLongToast

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val font = CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/GoogleSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        CalligraphyConfig.initDefault(font)
        initOpenCv()
    }

    private fun initOpenCv() {
        val opencvInitialized = OpenCVLoader.initDebug()
        if (opencvInitialized) {
            Log.v("s2poc", "Open cv init success")
            return
        }

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, object : LoaderCallbackInterface {
            override fun onManagerConnected(status: Int) {
                when (status) {
                    LoaderCallbackInterface.SUCCESS -> Log.v("s2poc", "init success async")
                    LoaderCallbackInterface.INIT_FAILED -> Log.v("s2poc", "async init fail")
                    LoaderCallbackInterface.MARKET_ERROR -> Log.v("s2poc", "async marker error")
                    LoaderCallbackInterface.INSTALL_CANCELED -> Log.v("s2poc", "async install cancel")
                    LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION -> Log.v("s2poc", "async incom man")
                }
            }

            override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface?) {
                Log.v("s2poc", "Open cv install from gplay")
            }
        })
    }
}