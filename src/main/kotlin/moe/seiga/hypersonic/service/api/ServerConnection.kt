package moe.seiga.hypersonic.service.api

import java.lang.IllegalStateException

@Suppress("unused")
object ServerConnection {
    private var _api: SubsonicApi? = null

    val api: SubsonicApi
        get() = _api ?: throw IllegalStateException("Subsonic API is not connected. Call connect() first.")

    val isConnected: Boolean
        get() = _api != null

    fun connect(url: String, user: String, pass: String) {
        _api?.close()
        _api = SubsonicApi.create(url, user, pass)
    }

    fun disconnect() {
        _api?.close()
        _api = null
    }
}
