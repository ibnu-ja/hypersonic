package moe.seiga.hypersonic.service.api

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import ru.stersh.subsonic.api.SubsonicApi as RawSubsonicApi
import ru.stersh.subsonic.api.model.*
import java.util.concurrent.CompletableFuture

/**
 * JVM wrapper for [ru.stersh.subsonic.api.SubsonicApi].
 * Returns [CompletableFuture] for async usage in Java.
 */
@Suppress("unused")
class SubsonicApi(private val api: RawSubsonicApi) {

    // A scope for executing the coroutines.
    // SupervisorJob ensure one failure doesn't crash the whole scope.
    // Dispatchers.IO is optimized for network/disk operations.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun ping(): CompletableFuture<EmptyResponse> = scope.future {
        api.ping().data
    }

    fun getSong(id: String): CompletableFuture<SongResponse> = scope.future {
        api.getSong(id).data
    }

    @JvmOverloads
    fun getRandomSongs(
        size: Int? = null,
        genre: String? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        musicFolderId: String? = null
    ): CompletableFuture<RandomSongsResponse> = scope.future {
        api.getRandomSongs(size, genre, fromYear, toYear, musicFolderId).data
    }

    fun getArtist(id: String): CompletableFuture<ArtistResponse> = scope.future {
        api.getArtist(id).data
    }

    fun getArtists(): CompletableFuture<ArtistsResponse> = scope.future {
        api.getArtists().data
    }

    @JvmOverloads
    fun getAlbumList(
        type: ListType,
        size: Int? = null,
        offset: Int? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        genre: String? = null,
        musicFolderId: String? = null
    ): CompletableFuture<AlbumListResponse> = scope.future {
        api.getAlbumList(type, size, offset, fromYear, toYear, genre, musicFolderId).data
    }

    @JvmOverloads
    fun getAlbumList2(
        type: ListType,
        size: Int? = null,
        offset: Int? = null,
        fromYear: Int? = null,
        toYear: Int? = null,
        genre: String? = null,
        musicFolderId: String? = null
    ): CompletableFuture<AlbumList2Response> = scope.future {
        api.getAlbumList2(type, size, offset, fromYear, toYear, genre, musicFolderId).data
    }

    fun getAlbum(id: String): CompletableFuture<AlbumResponse> = scope.future {
        api.getAlbum(id).data
    }

    fun getPlaylist(id: String): CompletableFuture<PlaylistResponse> = scope.future {
        api.getPlaylist(id).data
    }

    fun getPlaylists(): CompletableFuture<PlaylistsResponse> = scope.future {
        api.getPlaylists().data
    }

    fun getPlayQueue(): CompletableFuture<PlayQueueResponse> = scope.future {
        api.getPlayQueue().data
    }

    @JvmOverloads
    fun savePlayQueue(
        id: List<String>,
        current: String? = null,
        position: Long? = null
    ): CompletableFuture<EmptyResponse> = scope.future {
        api.savePlayQueue(id, current, position).data
    }

    @JvmOverloads
    fun star(
        id: List<String>? = null,
        albumId: List<String>? = null,
        artistId: List<String>? = null
    ): CompletableFuture<EmptyResponse> = scope.future {
        api.star(id, albumId, artistId).data
    }

    @JvmOverloads
    fun unstar(
        id: List<String>? = null,
        albumId: List<String>? = null,
        artistId: List<String>? = null
    ): CompletableFuture<EmptyResponse> = scope.future {
        api.unstar(id, albumId, artistId).data
    }

    @JvmOverloads
    fun getStarred(
        musicFolderId: String? = null
    ): CompletableFuture<StarredResponse> = scope.future {
        api.getStarred(musicFolderId).data
    }

    @JvmOverloads
    fun getStarred2(
        musicFolderId: String? = null
    ): CompletableFuture<Starred2Response> = scope.future {
        api.getStarred2(musicFolderId).data
    }

    @JvmOverloads
    fun scrobble(
        id: String,
        time: Long? = null,
        submission: Boolean? = null,
    ): CompletableFuture<EmptyResponse> = scope.future {
        api.scrobble(id, time, submission).data
    }

    @JvmOverloads
    fun search3(
        query: String,
        songCount: Int? = null,
        songOffset: Int? = null,
        albumCount: Int? = null,
        albumOffset: Int? = null,
        artistCount: Int? = null,
        artistOffset: Int? = null
    ): CompletableFuture<SearchResult3Response> = scope.future {
        api.search3(
            query,
            songCount,
            songOffset,
            albumCount,
            albumOffset,
            artistCount,
            artistOffset
        ).data
    }

    @JvmOverloads
    fun getCoverArtUrl(
        id: String?,
        size: Int? = null,
        auth: Boolean = false
    ): String? {
        return api.getCoverArtUrl(id, size, auth)
    }

    fun downloadUrl(id: String): String {
        return api.downloadUrl(id)
    }

    fun streamUrl(id: String): String {
        return api.streamUrl(id)
    }

    @JvmOverloads
    fun avatarUrl(
        username: String,
        auth: Boolean = false
    ): String {
        return api.avatarUrl(username, auth)
    }
}
