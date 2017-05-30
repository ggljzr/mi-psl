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

import scalafx.event.ActionEvent

import depthblur.DepthBlurAlg


object DepthBlur extends JFXApp {

  val stageWidth = 500
  val stageHeight = 500

  stage = new PrimaryStage {

    minWidth = stageWidth
    minHeight = stageHeight

    title = "some title"

    val img = new Image("file:cones/im2.png")
    val dpt = new Image("file:cones/disp2.png")

    var showDepth = false

    scene = new Scene{
      val mapToggle = new Button("Show depth map")
      val reset = new Button("Reset")
      val save = new Button("Save")

      val filterGroup = new ToggleGroup()
      val rbBoxFilter = new RadioButton("Box filter")
      val rbBilateralFilter = new RadioButton("Bilateral filter")
      rbBoxFilter.setToggleGroup(filterGroup)
      rbBilateralFilter.setToggleGroup(filterGroup)
      rbBoxFilter.setSelected(true)

      val buttons = new HBox(5.0, mapToggle, reset, save)
      val info = new Label("Click to image to apply filter.")
      val display = new ImageView(img)

      val displayControl = new VBox(5.0, display, info, buttons)
      val filterSelect = new VBox(5.0, rbBoxFilter, rbBilateralFilter)

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

      display.onMouseClicked = (event: MouseEvent) => {
        val x = event.sceneX.toInt
        val y = event.sceneY.toInt
        DepthBlurAlg.boxFilter(x, y, img, dpt)
      }

      content = new HBox(5.0, displayControl, filterSelect)
    }
  }
}