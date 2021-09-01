package main

import scala.io.Source
import java.util.Properties
import java.io.StringReader
import java.net.URI
import java.io.PrintWriter
import java.io.File


case class Settings(
    val accessToken: String,
    val RefreshToken: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUrl: String,
    val authCode: String,
    val borderless: Boolean,
    val volumeSteps: Int,
    val localServer: Boolean,
    val defaultBrowser: String
)

object Settings {
    def load = {
        val raw = Source.fromFile("application.properties").getLines.mkString("\n")
        val props = new Properties
        props.load(new StringReader(raw))
        new Settings(
            props.getProperty("access-token"),
            props.getProperty("refresh-token"),
            props.getProperty("client-id"),
            props.getProperty("client-secret"),
            props.getProperty("redirect-url"),
            props.getProperty("authorization-code"),
            props.getProperty("borderless").toBoolean,
            props.getProperty("volume-steps").toInt,
            props.getProperty("local-server").toBoolean,
            props.getProperty("default-browser")
        )
    }
    def update(key: String, value: String) = {
        if(value != null) {
            val raw = Source.fromFile("application.properties").getLines.mkString("\n")
            val props = new Properties
            props.load(new StringReader(raw))
            props.setProperty(key, value)
            val pw = new PrintWriter(new File("application.properties"))
            pw.write(props.toString.replace(", ", "\n").replace("{", "").replace("}", ""))
            pw.close
        }
    }
}