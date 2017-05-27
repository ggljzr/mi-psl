package depthblur

import scalafx.scene.image.{Image, WritableImage, PixelReader, PixelWriter}

object DepthBlurAlg {
    def test(x: Int, y: Int, img: Image, dpt: Image) : Image = {
        println(s"test: $x $y")
    }
}