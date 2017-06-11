package depthblur
 
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.{Button, RadioButton, ToggleGroup}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{VBox, HBox}
import scalafx.scene.input.MouseEvent
import scalafx.stage.FileChooser
import scalafx.stage.FileChooser.ExtensionFilter

import javafx.scene.control.{RadioButton => JfxRadioBtn}

import scalafx.event.ActionEvent

import depthblur.DepthBlurAlg


object DepthBlur extends JFXApp {

  val stageWidth = 800
  val stageHeight = 600

  stage = new PrimaryStage {

    width = stageWidth
    height = stageHeight
    resizable = false

    title = "DepthBlur"

    var img = new Image("file:cones/im2.png")
    var dpt = new Image("file:cones/disp2.png")

    var showDepth = false

    val fileChooser = new FileChooser {
      extensionFilters ++= Seq(
        new ExtensionFilter("Image Files", Seq("*.png", "*.jpg"))
      )
    }

    scene = new Scene{

      val defaultInfoMessage = "Click image to apply filter."

      val mapToggle = new Button("Show depth map")
      val reset = new Button("Reset")
      val save = new Button("Save image")
      val load = new Button("Load image")

      val filterGroup = new ToggleGroup()
      val rbBoxFilter = new RadioButton("Box filter")
      val rbBilateralFilter = new RadioButton("Bilateral filter")
      val rbNegation = new RadioButton("Negation")
      rbBoxFilter.setToggleGroup(filterGroup)
      rbBilateralFilter.setToggleGroup(filterGroup)
      rbNegation.setToggleGroup(filterGroup)
      rbBoxFilter.setSelected(true)

      val buttons = new HBox(5.0, mapToggle, reset, save, load)
      val info = new Label(defaultInfoMessage)

      val display = new ImageView(img)
      display.fitWidth = stageWidth.toDouble * 0.8
      display.fitHeight = stageHeight.toDouble * 0.8
      display.preserveRatio = true

      val displayControl = new VBox(5.0, display, info, buttons)
      val filterSelect = new VBox(5.0, rbBoxFilter, rbBilateralFilter, rbNegation)

      def resetScene {
        showDepth = false
        display.image = img
        mapToggle.text = "Show depth map"
        info.text =defaultInfoMessage
      }

      mapToggle.onAction = handle{
        if(showDepth == true){
          mapToggle.text = "Show depth map"
          display.image = img
          showDepth = false
        }
        else{
          mapToggle.text = "Show original image"
          display.image = dpt
          showDepth = true
        }
      }

      reset.onAction = handle{
        resetScene
      }

      save.onAction = handle{
        val filename = fileChooser.showSaveDialog(stage)
        println(s"saving image in $filename")
      }

      load.onAction = handle{
        val filename = fileChooser.showOpenDialog(stage)
        println(s"opening image in $filename")
        img = new Image(s"file:$filename")
        resetScene
        info.text = s"Loaded image: $filename"
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
            display.image = DepthBlurAlg.boxFilter(x, y, img, dpt)
          }
          case "Bilateral filter" => { 
            display.image = DepthBlurAlg.bilateralFilter(x, y, img, dpt)
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