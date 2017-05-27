package depthblur
 
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets

import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.image.{Image, ImageView, PixelWriter, WritableImage}
import scalafx.scene.control.{Button, ProgressBar}
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.VBox
import scalafx.scene.input.MouseEvent

import scalafx.event.ActionEvent


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
      val display = new ImageView(img)

      mapToggle.onAction = handle{
        if(showDepth == true){
          display.image = dpt
          showDepth = false
        }
        else{
          display.image = img
          showDepth = true
        }
      }

      display.onMouseClicked = (event: MouseEvent) => {
        val x = event.sceneX
        val y = event.sceneY

        println(s"$x, $y")
      }

      content = new VBox{
        children = Seq(display, mapToggle)
      }
    }
  }
}