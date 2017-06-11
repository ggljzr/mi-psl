package depthblur

import scala.collection.mutable.ArrayBuffer
import scalafx.scene.image.{Image, WritableImage, PixelReader, PixelWriter}
import javafx.scene.image.{WritablePixelFormat, PixelFormat}

import scala.math.{exp, sqrt}

object FilterType extends Enumeration
{
	type FilterType = Value
	val BoxFilter, BilateralFilter = Value
}

object DepthBlurAlg {

	val sigmaDepth = 60
	val sigmaDist = 60

	private def getAverage(pixels: Array[Int]) : Int = {
		val a = 0xFF
		val rAvg = (pixels.map(p => (p >> 16) & 255).sum) / pixels.length
		val gAvg = (pixels.map(p => (p >> 8) & 255).sum) / pixels.length
		val bAvg = (pixels.map(p => p & 255).sum) / pixels.length

		return (a << 24) + (rAvg << 16) + (gAvg << 8) + bAvg
	}

	private def gaussian(z: Double, sigma: Double) : Double = 
	{
		val n = (z * z) * -1
		val d = (1 * sigma * sigma)
		return exp(n/d)
	}

	private def euclid(x1: Int, x2: Int, y1: Int, y2:Int) : Double = {
		val d = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
		return sqrt(d)
	}

	private def getBilateralPixel(pixels: Array[Int], depths: Array[Int], 
			row: Int, col: Int, pixDepth: Int) : Int = {
		return 255
	}

	private def getKernelSize(currentDepth: Int, targetDepth: Int) : Int = {
		val depth = (currentDepth - targetDepth).abs
		depth match{
			case x if x < 32 => return 1
			case x if x < 64 => return 5
			case x if x < 128 => return 7
			case x if x < 196 => return 11
			case _ => return 13
		}
	}

	import FilterType._

    def blurFilter(x: Int, y: Int, img: Image, dpt: Image, filter: FilterType) : WritableImage = {

        val w = img.width.toInt
        val h = img.height.toInt

        val res = new WritableImage(w, h)

        val imgReader = img.getPixelReader
        val dptReader = dpt.getPixelReader

        val targetDepth = dptReader.getArgb(x, y) & 255

        println(s"$filter : target depth[$x $y] = $targetDepth")

        val writer = res.getPixelWriter
        val format = PixelFormat.getIntArgbInstance()

        for(i <- 7 to (h - 8)){
        	for(j <- 7 to (w - 8)){
        		val currentDepth = dptReader.getArgb(j, i) & 255
        		val k = getKernelSize(currentDepth, targetDepth)
        		val pixBuffer = new Array[Int](k*k)
        		imgReader.getPixels(j-(k/2), i-(k/2), k, k, format, pixBuffer, 0, k)

        		if(filter == BoxFilter){
        			val newPix = getAverage(pixBuffer).toInt
        			writer.setArgb(j, i, newPix)
        		}
        		else if(filter == BilateralFilter){
        			val depthBuffer = new Array[Int](k * k)
        			dptReader.getPixels(j - (k / 2), i - (k / 2), k, k, format, depthBuffer, 0, k)
        			val newPix = getBilateralPixel(pixBuffer, depthBuffer, i, j, currentDepth)
        			writer.setArgb(j, i, newPix)
        		}
        	}
        }

        //val pixBuffer = new Array[Int](50*50)
        //imgReader.getPixels(x-25, y-25, 50, 50, format, pixBuffer, 0, 50)
        //writer.setPixels(x-25,y-25,50,50,format,pixBuffer,0,50)

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

        		val new_pix = (a << 24) + (r << 16) + (g << 8) + b

        		writer.setArgb(j, i, new_pix)
        	}
        }

        return res
    }
}
