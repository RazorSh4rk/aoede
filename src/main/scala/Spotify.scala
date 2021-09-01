package main

import com.wrapper.spotify.SpotifyApi
import java.net.URI
import com.wrapper.spotify.SpotifyHttpManager
import scala.sys.process._

class SpotifyController {
    val settings = Settings.load
    val scope = "user-top-read,user-read-playback-position,user-read-playback-state,user-modify-playback-state,user-read-currently-playing,app-remote-control,playlist-read-private,user-library-read"
    var spotifyAPI: SpotifyApi = null

    // fully set up
    if(settings.RefreshToken != ""){
        spotifyAPI = new SpotifyApi
            .Builder()
            .setClientId(settings.clientId)
            .setClientSecret(settings.clientSecret)
            .setRedirectUri(SpotifyHttpManager.makeUri(settings.redirectUrl))
            .setRefreshToken(settings.RefreshToken)
            .setAccessToken(settings.accessToken)
            .build

        val refreshReq = spotifyAPI
            .authorizationCodeRefresh
            .build
            .execute
        
        spotifyAPI.setAccessToken(refreshReq.getAccessToken)
        Settings.update("access-token", refreshReq.getAccessToken)

        spotifyAPI.setRefreshToken(refreshReq.getRefreshToken)
        Settings.update("refresh-token", refreshReq.getRefreshToken)

    } else {
        spotifyAPI = new SpotifyApi
            .Builder()
            .setClientId(settings.clientId)
            .setClientSecret(settings.clientSecret)
            .setRedirectUri(SpotifyHttpManager.makeUri(settings.redirectUrl))
            .build

        // zero setup
        if(settings.authCode == "") {
            val authUriReq = spotifyAPI
                .authorizationCodeUri
                .scope(scope)
                .show_dialog(true)
                .build
            val authUri = authUriReq.execute()
            println(s"AUTH URI: ${authUri.toString}")
            s"firefox ${authUri.toString}".!
            System.exit(0)
        // only need tokens
        } else {
            val authReq = spotifyAPI
                .authorizationCode(settings.authCode)
                .build
            val authCreds = authReq.execute
            spotifyAPI.setAccessToken(authCreds.getAccessToken)
            Settings.update("access-token", authCreds.getAccessToken)

            spotifyAPI.setRefreshToken(authCreds.getRefreshToken)
            Settings.update("refresh-token", authCreds.getRefreshToken)
        }
    }

    var volume = 0

    def isPlaying: Boolean = {
        val resp = spotifyAPI
            .getUsersCurrentlyPlayingTrack
            .build
            .execute

        resp != null && resp.getIs_playing
    }

    def currentVolume = {
        spotifyAPI
            .getInformationAboutUsersCurrentPlayback
            .build
            .execute
            .getDevice
            .getVolume_percent
    }

    def getCurrentPlayback = {
        val response = spotifyAPI
            .getUsersCurrentlyPlayingTrack
            .build
            .execute
        val id = response
            .getItem
            .getId
        val track = spotifyAPI
            .getTrack(id)
            .build
            .execute
        
        val title = track
            .getName
        val artist = track
            .getArtists
            .head
            .getName
        val album = track
            .getAlbum
            .getName
        val art = track
            .getAlbum
            .getImages
            .apply(1)
            .getUrl()
            
        Song(title, artist, album, art)
    }

    def play = {
        spotifyAPI
            .startResumeUsersPlayback
            .build
            .execute
        getCurrentPlayback
    }   
    def pause = {
        spotifyAPI
            .pauseUsersPlayback
            .build
            .execute
    }
    def nextTrack = {
        spotifyAPI
            .skipUsersPlaybackToNextTrack
            .build
            .execute

        Thread.sleep(500)
        getCurrentPlayback
    }
    def prevTrack = {
        spotifyAPI
            .skipUsersPlaybackToPreviousTrack
            .build
            .execute

        Thread.sleep(500)
        getCurrentPlayback
    }
    def volUp = {
        val volume = currentVolume + settings.volumeSteps
        try {
            spotifyAPI
                .setVolumeForUsersPlayback(volume)
                .build
                .execute
        } catch {
            case _: Throwable => ()
        }
    }
    def volDown = {
        val volume = currentVolume + settings.volumeSteps
        try{
            spotifyAPI
                .setVolumeForUsersPlayback(volume)
                .build
                .execute
        } catch {
            case _: Throwable => ()
        }
    }
}
