package depthblur

import scalafx.scene.image.{Image, WritableImage, PixelReader, PixelWriter}

object DepthBlurAlg {
    def boxFilter(x: Int, y: Int, img: Image, dpt: Image) : WritableImage = {
        println(s"test: $x $y")

        val res = new WritableImage(10,10)

        return res
    }

    def bilateralFilter(x: Int, y: Int, img: Image, dpt: Image) : WritableImage = {

    	println(s"test bilateral: $x $y")

    	val res = new WritableImage(10, 10)

    	return res
    }
}
