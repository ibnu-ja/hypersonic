package moe.seiga.hypersonic.connection

import dev.zt64.subsonic.api.model.Album
import dev.zt64.subsonic.api.model.Playlist
import dev.zt64.subsonic.client.SubsonicAuth
import dev.zt64.subsonic.client.SubsonicClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import moe.seiga.hypersonic.playback.Song
import java.util.concurrent.CompletableFuture

class SubsonicApi(private val client: SubsonicClient) : AutoCloseable {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun ping(): CompletableFuture<*> = scope.future { client.ping() }

    fun getSong(id: String): CompletableFuture<Song> = scope.future {
        client.getSong(id).toSong()
    }

    fun getAlbum(id: String): CompletableFuture<Album> = scope.future { client.getAlbum(id) }
    fun getPlaylists(): CompletableFuture<List<Playlist>> = scope.future { client.getPlaylists() }
    fun getPlaylist(id: String): CompletableFuture<Playlist> = scope.future { client.getPlaylist(id) }
    fun getPlayQueue(): CompletableFuture<dev.zt64.subsonic.api.model.PlayQueue> =
        scope.future { client.getPlayQueue() }

    fun savePlayQueue(
        id: String? = null,
        currentIndex: Int? = null,
        position: Long? = null
    ): CompletableFuture<Unit> = scope.future { client.savePlayQueue(id, currentIndex, position) }

    fun scrobble(id: String): CompletableFuture<Unit> = scope.future { client.scrobble(id) }

    fun getRandomSongs(size: Int = 10): CompletableFuture<List<Song>> = scope.future {
        client.getRandomSongs(size).map { it.toSong() }
    }

    fun streamUrl(id: String): String = client.getStreamUrl(id)
    fun getCoverArtUrl(id: String, size: String? = null): String = client.getCoverArtUrl(id, size)

    override fun close() {
        client.close()
    }

    private fun dev.zt64.subsonic.api.model.Song.toSong(): Song {
        return Song(
            id,
            title,
            artistName,
            albumTitle ?: "",
            duration?.inWholeSeconds?.toInt() ?: 0,
            coverArtId ?: ""
        )
    }

    companion object {
        @JvmStatic
        fun create(baseUrl: String, username: String, password: String): SubsonicApi {
            val client = SubsonicClient(
                baseUrl = baseUrl,
                auth = SubsonicAuth.Token(username = username, password = password)
            )
            return SubsonicApi(client)
        }
    }
}