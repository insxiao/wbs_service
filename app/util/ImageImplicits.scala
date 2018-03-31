package util

import java.awt.geom.AffineTransform
import java.awt.image.{AffineTransformOp, BufferedImage}

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

  implicit class ImageScale(image: BufferedImage) {

    import AffineTransformOp.TYPE_BICUBIC

    private def toX(px: Int, interpolation: Int): BufferedImage = {
      val (w, h) = (image.getWidth(), image.getHeight())
      val scaleFactor = px.toDouble / (w max h).toDouble

      val transform = new AffineTransform()
      transform.scale(scaleFactor, scaleFactor)
      val op = new AffineTransformOp(transform, interpolation)
      val bounds = op.getBounds2D(image)
      val (tw, th) = (bounds.getWidth.toInt, bounds.getHeight.toInt)

      val target = new BufferedImage(tw, th, image.getType)

      op.filter(image, target)
    }

    def to32x: BufferedImage = toX(32, TYPE_BICUBIC)

    def to64x: BufferedImage = toX(64, TYPE_BICUBIC)

    def to128x: BufferedImage = toX(128, TYPE_BICUBIC)

    def to256x: BufferedImage = toX(256, TYPE_BICUBIC)

    def to512x: BufferedImage = toX(512, TYPE_BICUBIC)
  }

}
