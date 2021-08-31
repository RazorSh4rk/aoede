package main

import com.wrapper.spotify.SpotifyApi
import java.net.URI
import com.wrapper.spotify.SpotifyHttpManager
import scala.sys.process._

// https://accounts.spotify.com/en/authorize?client_id=59ee80ad1eb64d5182c63959950a0b0a&response_type=code&redirect_uri=localhost:9001&scope=user-top-read%20user-read-playback-position%20user-read-playback-state%20user-modify-playback-state%20user-read-currently-playing%20app-remote-control%20playlist-read-private%20user-library-read

class SpotifyController {
    val settings = Settings.load
    val scope = "user-top-read,user-read-playback-position,user-read-playback-state,user-modify-playback-state,user-read-currently-playing,app-remote-control,playlist-read-private,user-library-read"
    var spotifyAPI: SpotifyApi = null

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
        val authCredsRefreshed = refreshReq.execute
        spotifyAPI.setAccessToken(authCredsRefreshed.getAccessToken)
        Settings.update("access-token", authCredsRefreshed.getAccessToken)

        spotifyAPI.setRefreshToken(authCredsRefreshed.getRefreshToken)
        Settings.update("refresh-token", authCredsRefreshed.getRefreshToken)

        println(s"expires in ${authCredsRefreshed.getExpiresIn}")
    } else {
        spotifyAPI = new SpotifyApi
            .Builder()
            .setClientId(settings.clientId)
            .setClientSecret(settings.clientSecret)
            .setRedirectUri(SpotifyHttpManager.makeUri(settings.redirectUrl))
            .build

        if(settings.authCode == "") {
            val authUriReq = spotifyAPI
                .authorizationCodeUri
                .scope(scope)
                .show_dialog(true)
                .build()
            val authUri = authUriReq.execute()
            println(s"AUTH URI: ${authUri.toString}")
            s"firefox ${authUri.toString}".!
            System.exit(0)
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

    }
    def volDown = {

    }
}