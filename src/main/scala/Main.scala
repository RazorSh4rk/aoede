package main

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.Includes._
import scalafxml.core.{DependenciesByType, FXMLView}
import java.io._
import scala.io.Source
import javafx.stage.StageStyle
import scala.reflect.runtime.universe.typeOf

object Main extends JFXApp{
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
    if(settings.borderless) 
      initStyle(StageStyle.UNDECORATED)
    scene = new Scene(root)
  }
}