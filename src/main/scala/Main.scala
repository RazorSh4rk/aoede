package main

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.input._
import scalafx.Includes._
import scalafxml.core.{DependenciesByType, FXMLView}
import java.io._
import scala.io.Source
import javafx.stage.StageStyle
import scala.reflect.runtime.universe.typeOf
import scalafx.scene.input.KeyCode.{ A, S, D }
import java.net.URL
import java.net.MalformedURLException

object Main extends JFXApp{
  // check for internet
  val URL = new URL("https://www.google.com")
  var connected = false
  do {
    try {
      println("trying to connect...")
      URL.openConnection.connect
      connected = true
    } catch {
      case e: Exception => {
        println(e.toString)
        Thread.sleep(1000)
      }
    }
  } while(!connected)

  // fx:controller="main.UIController"
  val settings = Settings.load
 
  val view = getClass().getClassLoader().getResource("layout/main.fxml")
  val controller = new UIController(null)
  val root = FXMLView(view, new DependenciesByType(Map(
    typeOf[UIController] -> controller
  )))

  stage = new JFXApp.PrimaryStage {
    title = "spotify"
    resizable = false
    if(settings.borderless) {
      initStyle(StageStyle.UNDECORATED)
      fullScreen = true
    }
    scene = new Scene(root)
  
    handleEvent(KeyEvent.KeyReleased){
      (evt: KeyEvent) => {
        evt.code match {
          case S => {UIControllerCompanion.playButtonClicked; {}}
          case A => {UIControllerCompanion.prevButtonClicked; {}}
          case D => {UIControllerCompanion.nextButtonClicked; {}}
          case _ => {}
        }
      }
    }
  }
}