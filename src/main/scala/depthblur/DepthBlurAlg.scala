package depthblur

import scala.collection.mutable.ArrayBuffer
import scalafx.scene.image.{Image, WritableImage, PixelReader, PixelWriter}
import javafx.scene.image.{WritablePixelFormat, PixelFormat}

object DepthBlurAlg {

	private def getAverage(pixels: Array[Int]) : Int = 
	{
		val a = 0xFF
		val rAvg = (pixels.map(p => (p >> 16) & 255).sum) / pixels.length
		val gAvg = (pixels.map(p => (p >> 8) & 255).sum) / pixels.length
		val bAvg = (pixels.map(p => p & 255).sum) / pixels.length

		return (a << 24) + (rAvg << 16) + (gAvg << 8) + bAvg
	}

    def boxFilter(x: Int, y: Int, img: Image, dpt: Image) : WritableImage = {

        val w = img.width.toInt
        val h = img.height.toInt

        val res = new WritableImage(w, h)

        val imgReader = img.getPixelReader
        val dptReader = dpt.getPixelReader

        val target_depth = dptReader.getColor(x,y).getRed

        println(s"box filter: target depth[$x $y] = $target_depth")

        val writer = res.getPixelWriter
        val format = PixelFormat.getIntArgbInstance()

        
        for(i <- 7 to (h - 8)){
        	for(j <- 7 to (w - 8))
        	{
        		val pixBuffer = new Array[Int](13*13)
        		imgReader.getPixels(j-7, i-7, 13, 13, format, pixBuffer, 0, 13)
        		val new_pix = getAverage(pixBuffer).toInt
        		writer.setArgb(j,i,new_pix)
        	}
        }

        //val pixBuffer = new Array[Int](50*50)
        //imgReader.getPixels(x-25, y-25, 50, 50, format, pixBuffer, 0, 50)
        //writer.setPixels(x-25,y-25,50,50,format,pixBuffer,0,50)

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
