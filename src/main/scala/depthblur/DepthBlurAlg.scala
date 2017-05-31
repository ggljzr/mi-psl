package depthblur

import scala.collection.mutable.ArrayBuffer
import scalafx.scene.image.{Image, WritableImage, PixelReader, PixelWriter}
import javafx.scene.image.{WritablePixelFormat, PixelFormat}


object DepthBlurAlg {
    def boxFilter(x: Int, y: Int, img: Image, dpt: Image) : WritableImage = {
        println(s"test: $x $y")

        val w = img.width.toInt
        val h = img.height.toInt

        val res = new WritableImage(w, h)

        val imgReader = img.getPixelReader
        val dptReader = dpt.getPixelReader

        val writer = res.getPixelWriter

        for(i <- 0 to (h - 1)){
        	for(j <- 0 to (w - 1))
        	{
        		//val pixBuffer = new Array[Byte](3)
        		//imgReader.getPixels(i, j, 1, 1, format, pixBuffer, 1, 1)
        		//println(pixBuffer)
        		val ip = imgReader.getArgb(j, i)
        		val dp = dptReader.getArgb(j, i)

        		writer.setArgb(j, i, ip + dp)
        	}
        }

        return res
    }

    def bilateralFilter(x: Int, y: Int, img: Image, dpt: Image) : WritableImage = {
		println(s"bilateral test: $x $y")

        val w = img.width.toInt
        val h = img.height.toInt

        val res = new WritableImage(w, h)

        val imgReader = img.getPixelReader
        val dptReader = dpt.getPixelReader

        val writer = res.getPixelWriter

        for(i <- 0 to (h - 1)){
        	for(j <- 0 to (w - 1))
        	{
        		val ip = imgReader.getArgb(j, i)
        		val dp = dptReader.getArgb(j, i)

        		writer.setArgb(j, i, ip - dp)
        	}
        }

        return res
    }

    def negation(img: Image) : WritableImage = {
		println("negation")

        val w = img.width.toInt
        val h = img.height.toInt

        val res = new WritableImage(w, h)

        val imgReader = img.getPixelReader

        val writer = res.getPixelWriter

        for(i <- 0 to (h - 1)){
        	for(j <- 0 to (w - 1))
        	{
        		val ip = imgReader.getArgb(j, i)

        		val a = (ip >> 24) & 255
        		val r = ~(ip >> 16) & 255
        		val g = ~(ip >> 8) & 255
        		val b = ~(ip) & 255

        		val new_color = (a << 24) + (r << 16) + (g << 8) + b

        		writer.setArgb(j, i, new_color)
        	}
        }

        return res
    }
}
