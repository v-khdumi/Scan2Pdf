package xyz.adhithyan.scan2pdf.util

import java.util.*

class ResultHolder {
  companion object {
    var image: ByteArray? = null
    var timeToCallback = 0L
    var images: LinkedList<ByteArray>? = LinkedList()


    fun clearImages() {
      images = null
      images = LinkedList()
    }

  }
}
