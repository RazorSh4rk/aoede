package main

import scalafxml.core.macros.sfxml
import scalafx.event.ActionEvent
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.image.Image
import scalafx.scene.layout.BorderPane
import scalafx.scene.control.ListView
import javafx.collections.FXCollections
import scalafx.scene.paint.Color
import javafx.scene.control.ListCell
import com.wrapper.spotify.SpotifyApi

@sfxml
class UIController(
    private val albumart: ImageView,
    private val title: Label,
    private val artist: Label,
    private val album: Label,

    private val menuPage: BorderPane,
    private val playlistList: ListView[Label],
    private val playButton: ImageView,
) {
    val pauseImage = new Image("images/pause-button.png")
    val playImage = new Image("images/play-button.png")
    val spotify = UIControllerCompanion.spotify

    def playButtonClicked = {
        if(!spotify.isPlaying) {
            val song = spotify.play
            albumart.setImage(new Image(song.albumArtUrl))
            title.text = song.title
            artist.text = song.artist
            album.text = song.album
            if(song.title != "Title")
                playButton.setImage(pauseImage)
        } else {
            playButton.setImage(playImage)
            spotify.pause
        }
    }
    def prevButtonClicked = {
        val song = spotify.prevTrack
        albumart.setImage(new Image(song.albumArtUrl))
        title.text = song.title
        artist.text = song.artist
        album.text = song.album
        playButton.setImage(pauseImage)
    }
    def nextButtonClicked = {
        val song = spotify.nextTrack
        albumart.setImage(new Image(song.albumArtUrl))
        title.text = song.title
        artist.text = song.artist
        album.text = song.album
        playButton.setImage(pauseImage)
    }
    def volumeUpButtonClicked = {
        spotify.volUp
    }
    def volumeDownButtonClicked = {
        spotify.volDown
    }
    def menuButtonClicked = {
        val listItems = FXCollections.observableArrayList(
            new Label("test")
        )
        playlistList.setItems(listItems)
        menuPage.setVisible(true)
    }
    def hideMenuButtonCLicked = {
        menuPage.setVisible(false)
    }
}

object UIControllerCompanion {
    val spotify = new SpotifyController

    def playButtonClicked = {
        if(!spotify.isPlaying) 
            spotify.play
        else spotify.pause
    }

    def prevButtonClicked = spotify.prevTrack

    def nextButtonClicked = spotify.nextTrack
}
