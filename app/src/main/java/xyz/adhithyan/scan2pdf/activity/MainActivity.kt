package xyz.adhithyan.scan2pdf.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.engine.impl.PicassoEngine

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R
import xyz.adhithyan.scan2pdf.extensions.PERMISSION_REQUEST_CALLBACK
import xyz.adhithyan.scan2pdf.extensions.checkOrGetPermissions
import xyz.adhithyan.scan2pdf.extensions.requestPermissionsResult
import xyz.adhithyan.scan2pdf.util.ResultHolder
import xyz.adhithyan.scan2pdf.util.toByteArray

class MainActivity : AppCompatActivity() {
    val CHOOSE_IMAGES = 3592

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        bottomNavigation.setOnNavigationItemSelectedListener(bottomNavigationClickListener)
        title = "My Scans"

        checkOrGetPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    fun startScan() {
        /*val intent = Intent(this, ScanActivity::class.java)
        startActivity(intent)*/
        Matisse.from(this)
                .choose(MimeType.ofImage(), false)
                .countable(true)
                .maxSelectable(5)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(PicassoEngine())
                .forResult(CHOOSE_IMAGES)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_IMAGES && resultCode == Activity.RESULT_OK) {
            val selected = Matisse.obtainResult(data)
            for (selectedImage in selected) {
                val imageBytes = (selectedImage as Uri).toByteArray(this)
                ResultHolder.images?.add(imageBytes)
            }
            ResultHolder.image = ResultHolder.images?.first
            val intent = Intent(this, PreviewActivity::class.java)
            startActivity(intent)
            Log.d("Chosen images", "selected:" + selected)
        }
    }

    private val bottomNavigationClickListener by lazy {
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_all_scans -> {

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_new_scan -> {
                    //startActivity(Intent(this@MainActivity, ScanActivity::class.java))
                    startScan()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_settings -> {

                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        this.requestPermissionsResult(requestCode, permissions, grantResults)
    }
}
