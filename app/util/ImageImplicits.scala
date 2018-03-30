package util

import java.awt.image.BufferedImage

object ImageImplicits {
  implicit class Image2BufferedImage(image: java.awt.Image) {
    def toBufferedImage: BufferedImage = {
      val bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)
      val g = bi.createGraphics()
      g.drawImage(image, 0, 0, null)
      g.dispose()

      bi
    }
  }
}
