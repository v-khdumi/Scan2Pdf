package xyz.adhithyan.scan2pdf.util

import android.graphics.Bitmap
import java.util.*

class ResultHolder {
  companion object {
    var image: ByteArray? = null
    var timeToCallback = 0L
    var images: LinkedList<ByteArray>? = LinkedList()
    var currentImageHeight = 0
    var currentImageWidth = 0

    fun clearImages() {
      images = null
      images = LinkedList()
    }

  }
}
