import scala.sys.process._

scalaVersion := "2.13.3"

name := "spotify"
organization := "io.github.razorsh4rk"
version := "1.0"

libraryDependencies += "org.scalafx" %% "scalafx" % "16.0.0-R24"
libraryDependencies += "se.michaelthelin.spotify" % "spotify-web-api-java" % "6.5.4"
libraryDependencies += "io.undertow" % "undertow-core" % "2.2.10.Final"
scalacOptions += "-Ymacro-annotations"
libraryDependencies += "org.scalafx" %% "scalafxml-core-sfx8" % "0.5"

lazy val javaFXModules = {
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux")   => "linux"
    case n if n.startsWith("Mac")     => "mac"
    case n if n.startsWith("Windows") => "win"
    case _                            => 
      throw new Exception("Unknown platform!")
  }
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map( m=> "org.openjfx" % s"javafx-$m" % "16" classifier osName)
}
libraryDependencies ++= javaFXModules

assemblyMergeStrategy in assembly := {
      case x if x.contains("module-info.class") => MergeStrategy.concat
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
}

val _ = "cp target/scala-2.13/spotify-assembly-1.0.jar ./spotify.jar".!
