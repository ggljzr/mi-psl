package depthblur
 
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.{Button, ProgressBar}
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
      val mapToggle = new Button("Map toggle")
      val applyFilter = new Button("Apply filter")
      val display = new ImageView(img)

      mapToggle.onAction = handle{
        if(showDepth == true){
          display.image = img
          showDepth = false
        }
        else{
          display.image = dpt
          showDepth = true
        }
      }

      display.onMouseClicked = (event: MouseEvent) => {
        val x = event.sceneX.toInt
        val y = event.sceneY.toInt
        DepthBlurAlg.test(x, y, img, dpt)
      }

      content = new VBox{
        val buttons = new HBox(5.0, mapToggle, applyFilter)
        children = Seq(display, buttons)
      }
    }
  }
}