package moe.seiga.hypersonic.service.api;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.gnome.gio.Settings;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.SSLHandshakeException;

@Slf4j
@RegisteredType(name = "ServerState")
public class ServerState extends GObject {

    private Settings settings;
    private PasswordStore passwordStore;

    @Getter
    private ConnectionState connectionState = ConnectionState.NOT_CONNECTED;

    @Getter
    private String serverUrl;

    @Getter
    private String username;

    @Getter
    private String serverVersion;

    private CompletableFuture<Void> pendingPing;

    public ServerState(Settings settings) {
        this.settings = settings;
        this.passwordStore = new PasswordStore();
        this.serverUrl = settings.getString("server-url");
        this.username = settings.getString("username");

        if (hasSavedCredentials()) {
            setConnectionState(ConnectionState.CONNECTING);
            autoLogin();
        }
    }

    public void setConnectionState(ConnectionState state) {
        if (this.connectionState != state) {
            log.info("Connection state: {} -> {}", this.connectionState, state);
            this.connectionState = state;
            notify("connection-state");
        }
    }

    public void setServerUrl(String url) {
        if (!java.util.Objects.equals(this.serverUrl, url)) {
            this.serverUrl = url;
            notify("server-url");
        }
    }

    public void setUsername(String name) {
        if (!java.util.Objects.equals(this.username, name)) {
            this.username = name;
            notify("username");
        }
    }

    public void setServerVersion(String version) {
        if (!java.util.Objects.equals(this.serverVersion, version)) {
            this.serverVersion = version;
            notify("server-version");
        }
    }

    private boolean hasSavedCredentials() {
        return serverUrl != null && !serverUrl.isBlank()
                && username != null && !username.isBlank();
    }

    public void startConnection(String url, String username, String password) {
        settings.setString("server-url", url);
        settings.setString("username", username);
        setServerUrl(url);
        setUsername(username);
        passwordStore.store(password);

        setConnectionState(ConnectionState.CONNECTING);
        new Thread(() -> pingServer(url, username, password)).start();
    }

    private void autoLogin() {
        new Thread(() -> {
            String password = passwordStore.lookup();
            if (password == null || password.isBlank()) {
                setConnectionState(ConnectionState.INVALID_CREDENTIALS);
                return;
            }
            pingServer(serverUrl, username, password);
        }).start();
    }

    private void pingServer(String url, String user, String pass) {
        try {
            var api = SubsonicApi.create(url, user, pass);
            api.ping().get(15, TimeUnit.SECONDS);
            serverVersion = "";
            setConnectionState(ConnectionState.CONNECTED);
        } catch (ExecutionException e) {
            var cause = e.getCause();
            if (cause instanceof SSLHandshakeException) {
                log.warn("SSL error: {}", cause.getMessage());
                setConnectionState(ConnectionState.SSL_ERROR);
            } else {
                String msg = cause != null ? cause.getMessage() : "";
                log.warn("Ping failed: {}", msg, cause);
                if (msg.contains("40") || msg.contains("41") || msg.contains("50")
                        || msg.contains("401") || msg.contains("403")) {
                    setConnectionState(ConnectionState.INVALID_CREDENTIALS);
                } else {
                    setConnectionState(ConnectionState.SERVER_UNREACHABLE);
                }
            }
        } catch (TimeoutException e) {
            log.warn("Ping timed out");
            setConnectionState(ConnectionState.SERVER_UNREACHABLE);
        } catch (Exception e) {
            log.error("Connection failed: {}", e.getMessage(), e);
            setConnectionState(ConnectionState.SERVER_UNREACHABLE);
        }
    }

    @Override
    protected void dispose() {
        settings = null;
        super.dispose();
    }
}