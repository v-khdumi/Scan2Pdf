package xyz.adhithyan.scan2pdf.extensions

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import java.util.*


val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA
)
val PERMISSION_REQUEST_CALLBACK = 1000

fun Context.getProgressBar(message: String): ProgressDialog {
    val progress = ProgressDialog(this)
    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER)
    progress.isIndeterminate = true
    progress.setMessage(message)
    progress.setCancelable(false)
    return progress
}


fun Context.showLongToast(info: String) {
    Toast.makeText(this, info, Toast.LENGTH_LONG).show()
}

fun Activity.checkOrGetPermissions() {
    val notGrantedPermissions = LinkedList<String>()

    for (permission in PERMISSIONS) {
        if (ContextCompat.checkSelfPermission(this.applicationContext, permission) != PackageManager.PERMISSION_GRANTED) {
            notGrantedPermissions.add(permission)
        }
    }

    if (notGrantedPermissions.size > 0) {
        ActivityCompat.requestPermissions(this, notGrantedPermissions.toTypedArray(), PERMISSION_REQUEST_CALLBACK)
    }
}

fun Activity.requestPermissionsResult(requestCode: Int,
                                      permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
        PERMISSION_REQUEST_CALLBACK -> {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            } else {
                Toast.makeText(this, "Grant all permissions.", Toast.LENGTH_LONG).show()
                this.finish()
            }
            return
        }

        else -> {
        }
    }
}