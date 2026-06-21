package moe.seiga.hypersonic.connection

import java.lang.IllegalStateException

@Suppress("unused")
object ServerConnection {
    private var _api: SubsonicApi? = null

    val api: SubsonicApi
        get() = _api ?: throw IllegalStateException("Subsonic API is not connected")

    fun setApi(subsonicApi: SubsonicApi) {
        _api?.close()
        _api = subsonicApi
    }

    fun clear() {
        _api?.close()
        _api = null
    }
}
