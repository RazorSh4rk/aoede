package main

import com.wrapper.spotify.SpotifyApi
import java.net.URI
import com.wrapper.spotify.SpotifyHttpManager
import scala.sys.process._
import com.wrapper.spotify.exceptions.detailed.NotFoundException

class SpotifyController {
    var settings = Settings.load
    val scope = "user-top-read,user-read-playback-position,user-read-playback-state,user-modify-playback-state,user-read-currently-playing,app-remote-control,playlist-read-private,user-library-read"
    var spotifyAPI: SpotifyApi = null

    def getAuthCode = {
        println("getting auth code")

        // start local server to fetch code
        if(settings.localServer) {
            println("starting local server")
            Server.start
        }

        // build default auth form handler
        spotifyAPI = new SpotifyApi
            .Builder()
            .setClientId(settings.clientId)
            .setClientSecret(settings.clientSecret)
            .setRedirectUri(SpotifyHttpManager.makeUri(settings.redirectUrl))
            .build    
        
        // get auth URL
        val authUri = spotifyAPI
            .authorizationCodeUri
            .scope(scope)
            .show_dialog(true)
            .build
            .execute
        
        println(s"AUTH URI: ${authUri.toString}")

        // open it in browser
        // this will redirect to the local server, that 
        // updates the config file
        s"${settings.defaultBrowser} ${authUri.toString}".!

        // loop until code is in config
        var _conf = Settings.load
        while(_conf.authCode == "" || _conf.authCode == settings.authCode) {
            Thread.sleep(2000)
            _conf = Settings.load
            println(s"reloaded settings, auth code: ${_conf.authCode}")
        }

        println("stopping server")
        Server.stop

        println("reloading settings")
        settings = Settings.load

        println("reconstructing API")
        spotifyAPI = new SpotifyApi
            .Builder()
            .setClientId(settings.clientId)
            .setClientSecret(settings.clientSecret)
            .setRedirectUri(SpotifyHttpManager.makeUri(settings.redirectUrl))
            .setRefreshToken(settings.RefreshToken)
            .setAccessToken(settings.accessToken)
            .build
    }

    def getTokens = {
        println("getting fresh tokens")

        val authCreds = spotifyAPI
            .authorizationCode(settings.authCode)
            .build
            .execute
            
        spotifyAPI.setAccessToken(authCreds.getAccessToken)
        Settings.update("access-token", authCreds.getAccessToken)
            
        spotifyAPI.setRefreshToken(authCreds.getRefreshToken)
        Settings.update("refresh-token", authCreds.getRefreshToken)
    }

    def refreshTokenExpired = {
        println("checking if tokens are expired")
        // build with saved tokens
        spotifyAPI = new SpotifyApi
            .Builder()
            .setClientId(settings.clientId)
            .setClientSecret(settings.clientSecret)
            .setRedirectUri(SpotifyHttpManager.makeUri(settings.redirectUrl))
            .setRefreshToken(settings.RefreshToken)
            .setAccessToken(settings.accessToken)
            .build

        var ret = false
        try {
            spotifyAPI
                .authorizationCodeRefresh
                .build
                .execute
        } catch {
            case t: Throwable => ret = true
        }

        println(s"tokens expired: $ret")

        ret
    }

    def refreshTokens = {
        println("refreshing tokens")
        val refreshReq = spotifyAPI
            .authorizationCodeRefresh
            .build
            .execute

        spotifyAPI.setAccessToken(refreshReq.getAccessToken)
        Settings.update("access-token", refreshReq.getAccessToken)

        spotifyAPI.setRefreshToken(refreshReq.getRefreshToken)
        Settings.update("refresh-token", refreshReq.getRefreshToken)
    }

    // INIT

    // first launch
    if(settings.authCode == "" || refreshTokenExpired) {
        println("no authcode or expired tokens")
        getAuthCode
        getTokens
    } else {
        println("all is fine, refreshing tokens")
        refreshTokens
    }

    // ---

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
        var ret: Song = null
        try {
            spotifyAPI
                .startResumeUsersPlayback
                .build
                .execute
            ret = getCurrentPlayback
        } catch {
            case e: Throwable => {
                println("No active playback device. Start music on your phone first.")
                ret = Song("Title", "Artist", "Album", "images/sound-waves.png")
            }
        }

        ret
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
