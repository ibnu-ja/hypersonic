package moe.seiga.hypersonic.service.api

import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import ru.stersh.subsonic.api.AuthType
import ru.stersh.subsonic.api.SubsonicApi as RawSubsonicApi
import java.lang.IllegalStateException

@Suppress("unused")
object ServerConnection {
    private var client: HttpClient? = null
    private var _api: SubsonicApi? = null

    val api: SubsonicApi
        get() = _api ?: throw IllegalStateException("Subsonic API is not connected. Call connect() first.")

    val isConnected: Boolean
        get() = _api != null


    fun connect(url: String, user: String, pass: String) {
        client?.close()

        val newClient = HttpClient(Apache5) {
            engine {
                followRedirects = true
                socketTimeout = 10_000
                connectTimeout = 10_000
                connectionRequestTimeout = 20_000
            }
        }

        val rawApi = RawSubsonicApi(
            baseUrl = url,
            username = user,
            password = pass,
            apiVersion = "1.16.1",
            clientId = "SubsonicKotlinApi",
            authType = AuthType.Token(),
            baseClient = newClient
        )

        _api = SubsonicApi(rawApi)
        client = newClient
    }

    fun disconnect() {
        client?.close()
        client = null
        _api = null
    }
}
