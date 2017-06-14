package depthblur
 
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.{Button, RadioButton, ToggleGroup, Slider}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{VBox, HBox}
import scalafx.scene.input.MouseEvent
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import javafx.scene.control.{RadioButton => JfxRadioBtn}

import scalafx.event.ActionEvent

import depthblur.DepthBlurAlg
import depthblur.FilterType._

import java.awt.image.BufferedImage
import java.io.File

import javafx.embed.swing.SwingFXUtils
import javax.imageio.ImageIO

object DepthBlur extends JFXApp {

  val stageWidth = 800
  val stageHeight = 600

  stage = new PrimaryStage {

    width = stageWidth
    height = stageHeight
    resizable = false

    title = "DepthBlur"

    var img = new Image("file:resources/placeholder.png")
    var dpt = new Image("file:resources/placeholder.png")
    var showDepth = false

    val fileChooser = new FileChooser {
      extensionFilters ++= Seq(
        new ExtensionFilter("Image Files", Seq("*.png", "*.jpg"))
      )
      initialFileName = "image.png"
    }

    scene = new Scene{

      val defaultInfoMessage = "Click image to apply filter"
      val defaultDepthSigma = 60
      val defaultSpatialSigma = 60

      val mapToggle = new Button("Show depth map")
      val reset = new Button("Reset")
      val save = new Button("Save image")
      val load = new Button("Load image")
      val loadDepth = new Button("Load depth map")
      loadDepth.disable = true
      mapToggle.disable = true

      val filterGroup = new ToggleGroup()
      val rbBoxFilter = new RadioButton("Box filter")
      val rbBilateralFilter = new RadioButton("Bilateral filter")
      val rbNegation = new RadioButton("Negation")
      rbBoxFilter.setToggleGroup(filterGroup)
      rbBilateralFilter.setToggleGroup(filterGroup)
      rbNegation.setToggleGroup(filterGroup)
      rbNegation.setSelected(true)

      rbBoxFilter.disable = true
      rbBilateralFilter.disable = true

      val spatialSigma = new Slider(1, 255, defaultSpatialSigma)
      val depthSigma = new Slider(1, 255, defaultDepthSigma)
      val spatialSigmaL = new Label(s"Spatial sigma: $defaultSpatialSigma")
      val depthSigmaL = new Label(s"Depth sigma: $defaultDepthSigma")

      val buttons = new HBox(5.0, reset, save, load, loadDepth, mapToggle)
      val info = new Label(defaultInfoMessage)

      val display = new ImageView(img)
      display.fitWidth = stageWidth.toDouble * 0.8
      display.fitHeight = stageHeight.toDouble * 0.8
      display.preserveRatio = true

      val displayControl = new VBox(5.0, display, info, buttons)
      val bilateralControl = new VBox(3.0, rbBilateralFilter, 
                                      spatialSigmaL, spatialSigma, 
                                      depthSigmaL, depthSigma)
      val filterSelect = new VBox(5.0, rbNegation, 
                                       rbBoxFilter, 
                                       bilateralControl)

      def resetScene {
        showDepth = false
        display.image = img
        mapToggle.text = "Show depth map"
        info.text =defaultInfoMessage
      }

      def loadImage : Option[(Image, String)] = {
        val file = fileChooser.showOpenDialog(stage)

        if(file != null) {
          return Option((new Image(s"file:$file"), file.toString))
        }
        return Option(null)
      } 

      spatialSigma.valueProperty.addListener { 
        (o: javafx.beans.value.ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
        val s = newVal.doubleValue
        spatialSigmaL.text = f"Spatial sigma: $s%.2f"
      }

      depthSigma.valueProperty.addListener { 
        (o: javafx.beans.value.ObservableValue[_ <: Number], oldVal: Number, newVal: Number) =>
        val s = newVal.doubleValue
        depthSigmaL.text = f"Depth sigma: $s%.2f"
      }

      mapToggle.onAction = handle{
        if(showDepth == true) {
          mapToggle.text = "Show depth map"
          display.image = img
          showDepth = false
        }
        else {
          mapToggle.text = "Show original image"
          display.image = dpt
          showDepth = true
        }
      }

      reset.onAction = handle {
        resetScene
      }

      save.onAction = handle {
        val imageFile = fileChooser.showSaveDialog(stage)
        if(imageFile != null) {
          val bImage = SwingFXUtils.fromFXImage(display.image.value, null)
          ImageIO.write(bImage, "png", imageFile)
          println(s"saved in: $imageFile")
          info.text = s"Picture saved in: $imageFile"
        }
      }

      load.onAction = handle {
        loadImage match {
          case Some(image) => {
            img = image._1
            val filename = image._2
            loadDepth.disable = false
            println("loaded new image")
            resetScene
            info.text = s"Loaded image from: $filename"
            rbBilateralFilter.disable = true
            rbBoxFilter.disable = true
            rbNegation.setSelected(true)
            mapToggle.disable = true
          }
          case None => println("no image specified")
        }
      }

      loadDepth.onAction = handle {
        loadImage match{
          case Some(image) => {
            val w = image._1.width.toInt
            val h = image._1.height.toInt
            if(h == img.height.toInt && w == img.width.toInt) {
              dpt = image._1
              val filename = image._2
              println("loaded new depth map")
              info.text = s"Loaded depth map from: $filename"
              rbBilateralFilter.disable = false
              rbBoxFilter.disable = false
              mapToggle.disable = false
            }
            else {
              info.text = "Image and depth map size does not match"
              println("no matching image/depth map")
            }
          }
          case None => println("no depthmap specified")
        }
      }

      display.onMouseClicked = (event: MouseEvent) => {
        //we need to scale coordinates back to original image size
        val bounds = display.layoutBounds()
        val scaleX = bounds.width.toDouble / img.width.toDouble
        val scaleY = bounds.height.toDouble / img.height.toDouble

        val x = (event.sceneX / scaleX).toInt
        val y = (event.sceneY / scaleY).toInt

        //cast to java.scene.control.RadioButton
        //since I need .getText() method
        val filterBtn = filterGroup.selectedToggle().asInstanceOf[JfxRadioBtn]
        val filterName = filterBtn.getText

        filterName match {
          case "Box filter" => { 
            display.image = DepthBlurAlg.blurFilter(x, y, 
              img, dpt, BoxFilter)
          }
          case "Bilateral filter" => { 
            display.image = DepthBlurAlg.blurFilter(x, y, 
              img, dpt, BilateralFilter, 
              spatialSigma.value.toDouble, depthSigma.value.toDouble)
          }
          case "Negation" => {
            display.image = DepthBlurAlg.negation(img)
          }
        }

        info.text = s"Applying $filterName at [$x, $y]."
      }

      content = new HBox(5.0, displayControl, filterSelect)
    }
  }
}