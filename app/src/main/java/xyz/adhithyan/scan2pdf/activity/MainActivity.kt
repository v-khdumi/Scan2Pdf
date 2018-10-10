package xyz.adhithyan.scan2pdf.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import xyz.adhithyan.scan2pdf.R

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)
    bottomNavigation.setOnNavigationItemSelectedListener(bottomNavigationClickListener)
    title = "My Scans"
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

  fun startScan(v: View) {
    val intent = Intent(this, ScanActivity::class.java)
    startActivity(intent)
  }

  private val bottomNavigationClickListener by lazy {
    BottomNavigationView.OnNavigationItemSelectedListener { item ->
      when (item.itemId) {
        R.id.navigation_all_scans -> {

          return@OnNavigationItemSelectedListener true
        }
        R.id.navigation_new_scan -> {
          startActivity(Intent(this@MainActivity, ScanActivity::class.java))
          return@OnNavigationItemSelectedListener true
        }
        R.id.navigation_settings -> {

          return@OnNavigationItemSelectedListener true
        }
      }
      false
    }
  }
}
