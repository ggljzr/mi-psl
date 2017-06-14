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

import FilterType._

object DepthBlurAlg {

	/*
	Calculates average for every color channel in array of pixels
	(for example from PixelReader.getPixels method)

	Returns average color values composed into 32 bit integer.
	Alpha channel (top 8 bits of returned integer) is always 0xFF.
	*/
	private def getAverage(pixels: Array[Int]) : Int = {
		val a = 0xFF
		val rAvg = (pixels.map(p => (p >> 16) & 255).sum) / pixels.length
		val gAvg = (pixels.map(p => (p >> 8) & 255).sum) / pixels.length
		val bAvg = (pixels.map(p => p & 255).sum) / pixels.length

		return (a << 24) + (rAvg << 16) + (gAvg << 8) + bAvg
	}

	private def gaussian(z: Double, sigma: Double) : Double = {
		val n = (z * z) * -1
		val d = (1 * sigma * sigma)
		return exp(n/d)
	}

	/*
	Calculates euclid distance between two points in 2d plane
	*/
	private def euclid(x1: Int, x2: Int, y1: Int, y2:Int) : Double = {
		val d = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
		return sqrt(d)
	}

	/*
	Calculates value of a filtered pixel from array of surrounding pixels and their depths, using bilateral filter.
	Weights of bilateral filter are distance between pixels and difference of pixel depths.

	For more info about bilateral filter see: https://en.wikipedia.org/wiki/Bilateral_filter.

	Returns argb values composed into 32bit integer. Alpha channel is always 0xFF.
	*/
	private def getBilateralPixel(pixels: Array[Int], depths: Array[Int], 
			row: Int, col: Int, pixDepth: Int, 
			spatialSigma: Double, depthSigma: Double) : Int = {
		var wp= 0.0
		var sumR = 0.0
		var sumG = 0.0
		var sumB = 0.0

		val k = sqrt(pixels.length).toInt

		for(i <- 0 to (pixels.length - 1)) {
			val depthDiff = pixDepth - (depths(i) & 255)

			val x = (i % k) + (col - (k/2)).toInt
			val y = (i / k).toInt + (row - (k/2)).toInt
			val dist = euclid(row, col, y, x)

			val spatVal = gaussian(dist, spatialSigma)
			val depthVal = gaussian(depthDiff, depthSigma)

			wp += spatVal * depthVal
			val pixVal = pixels(i)

			sumR += (spatVal * depthVal) * ((pixVal >> 16) & 255).toDouble
			sumG += (spatVal * depthVal) * ((pixVal >> 8) & 255).toDouble
			sumB += (spatVal * depthVal) * ((pixVal) & 255).toDouble
		}

		sumR = (sumR / wp)
		sumG = (sumG / wp)
		sumB = (sumB / wp)

		return (0xFF << 24) + (sumR.toInt << 16) + (sumG.toInt << 8) + sumB.toInt
	}

	/*
	Returns kernel size based on difference of pixel depths. This is used to control
	how blurred different portions of image should be: bigger difference => larger kernel
	=> more blur.
	*/
	private def getKernelSize(currentDepth: Int, targetDepth: Int) : Int = {
		val depth = (currentDepth - targetDepth).abs
		depth match {
			case x if x < 32 => return 1
			case x if x < 64 => return 3
			case x if x < 128 => return 5
			case x if x < 196 => return 7
			case _ => return 11
		}
	}

	/*
	Applies selected blur filter to the image.

	@x : Int, @y : Int -- point in depth map which sets the focused depth. 
												Portions of image with depth same as this point will
												remain unblurred. Larger difference between this point
												and filtered pixel => more blurring of the filtered pixel

	@img : Image, @dpt : Image -- Input image and its depth map. Depth map should have
															 values between 0-255
	@filter : FilterType -- filter to be used for image blurring (see FilterType). 
													Should be either BoxFilter or BilateralFilter.
	@depthSigma : Double, @spatialSigma : Double -- sigma parameters for gaussian functions
																									used for bilateral filtering 
																									(see https://en.wikipedia.org/wiki/Bilateral_filter).
																									Does not have effect on box filtering.

	Method returns filtered image as WritableImage class.
	*/
	def blurFilter(x: Int, y: Int, 
		img: Image, dpt: Image, 
		filter: FilterType, 
		depthSigma: Double = 1.0, spatialSigma: Double = 1.0) : WritableImage = {

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
					val newPix = getBilateralPixel(pixBuffer, depthBuffer, i, j,
					 currentDepth, spatialSigma, depthSigma)
					writer.setArgb(j, i, newPix)
				}
			}
		}

		return res
	}

	/*
	Performs simple bit negation of every color channel within image.
	*/
	def negation(img: Image) : WritableImage = {
	println("negation")

		val w = img.width.toInt
		val h = img.height.toInt

		val res = new WritableImage(w, h)

		val imgReader = img.getPixelReader

		val writer = res.getPixelWriter

		for(i <- 0 to (h - 1)){
			for(j <- 0 to (w - 1)){
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
