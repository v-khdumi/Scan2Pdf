package xyz.adhithyan.scan2pdf.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import ru.whalemare.sheetmenu.SheetMenu
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R
import xyz.adhithyan.scan2pdf.extensions.checkOrGetPermissions
import xyz.adhithyan.scan2pdf.extensions.requestPermissionsResult
import java.io.File
import android.support.v4.content.FileProvider
import android.text.InputType
import android.view.ContextMenu
import android.view.View
import android.widget.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import xyz.adhithyan.scan2pdf.BuildConfig
import xyz.adhithyan.scan2pdf.util.*


class MainActivity : AppCompatActivity() {
    val CHOOSE_IMAGES = 3592
    private lateinit var PDF_LIST: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        bottomNavigation.setOnNavigationItemSelectedListener(bottomNavigationClickListener)
        title = "My Scans"

        checkOrGetPermissions()
        PDF_LIST = listAllFiles()
        var adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, PDF_LIST)
        scans_list_view.adapter = adapter

        scans_list_view.setOnItemClickListener { adapterView, view, i, l ->
            val pdfFile = File(ROOT_PATH + PDF_LIST[i])
            val pdfURI = FileProvider.getUriForFile(this@MainActivity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    pdfFile)

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(pdfURI, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        }

        registerOrRemoveContextMenu()
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

    fun chooseFromGallery() {
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
                    showScanMenu()
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

    private fun showScanMenu() {
        SheetMenu().apply {
            titleId = R.string.sheet_menu_title_new_scan
            click = MenuItem.OnMenuItemClickListener {
                when (it.itemId) {
                    R.id.new_scan_cam -> {
                        startActivity(Intent(this@MainActivity, ScanActivity::class.java));true
                    }
                    R.id.new_scan_gallery -> {
                        chooseFromGallery(); true
                    }
                    else -> {
                        true
                    }
                }
            }
            menu = R.menu.new_scan
        }.show(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        if(PDF_LIST.size == 1 && PDF_LIST[0] == NO_SCANS_PRESENT) {
            return;
        }
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.pdf_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo

        when(item.itemId) {
            R.id.scan_pdf_rename -> {
                val i = info.id.toInt()
                val srcFileName = PDF_LIST[i]
                showRenameFileDialog(srcFileName, i)
                return true
            }
            R.id.scan_pdf_share -> {
                val i = info.id.toInt()
                sharePdf(i)
            }
            R.id.scan_pdf_delete -> {
                val i = info.id.toInt()
                deletePdf(i)
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun showRenameFileDialog(srcFile: String, i: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Rename file")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        builder.setView(input)

        builder.setPositiveButton("OK", object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                var destFileName = input.text.toString().trim()
                if(!destFileName.endsWith(".pdf")) {
                    destFileName += ".pdf"
                }
                if(destFileName.isEmpty()) {
                    Toast.makeText(this@MainActivity, "File name cannot be empty", Toast.LENGTH_LONG).show()
                    dialog?.dismiss()
                } else {
                    val isFileRenamed = this@MainActivity.rename(srcFile, destFileName)
                    if(isFileRenamed) {
                        Toast.makeText(this@MainActivity, "File rename success", Toast.LENGTH_LONG).show()
                        PDF_LIST[i] = destFileName
                        var adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, PDF_LIST)
                        scans_list_view.adapter = adapter
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(this@MainActivity, "File rename failed.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })

        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun sharePdf(i: Int) {
        val pdfFile = File(ROOT_PATH + PDF_LIST[i])
        val pdfURI = FileProvider.getUriForFile(this@MainActivity,
                BuildConfig.APPLICATION_ID + ".provider",
                pdfFile)

        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, "Pdf created using Scan2Pdf app. \n")
        intent.putExtra(Intent.EXTRA_STREAM, pdfURI)
        intent.type = "application/pdf"
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Share PDF"))
    }

    private fun deletePdf(i: Int) {
        val progress = ProgressDialog(this)
        progress.setTitle("Deleting pdf ...")
        progress.show()
        val pdfFile = File(ROOT_PATH + PDF_LIST[i])

        Observable.just<Unit>(Unit)
                .map { pdfFile.delete() }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if(it) {
                        Toast.makeText(this, "Pdf deleted successfully.", Toast.LENGTH_LONG).show()
                        val newPdfList = PDF_LIST.toMutableList()
                        newPdfList.removeAt(i)
                        PDF_LIST = listAllFiles()
                        var adapter = ArrayAdapter<String>(this@MainActivity, android.R.layout.simple_list_item_1, PDF_LIST)
                        scans_list_view.adapter = adapter
                        adapter.notifyDataSetChanged()
                        registerOrRemoveContextMenu()
                    } else {
                        Toast.makeText(this, "Pdf deletion failed.", Toast.LENGTH_LONG).show()
                    }
                    progress.dismiss()
                }
    }


    private fun registerOrRemoveContextMenu() {
        if(PDF_LIST.size == 1 &&  PDF_LIST[0] == NO_SCANS_PRESENT) {
            return;
        } else {
            registerForContextMenu(scans_list_view)
        }
    }
}
