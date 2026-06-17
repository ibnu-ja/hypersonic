package moe.seiga.hypersonic;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.navigation.connection.WelcomePage;
import moe.seiga.hypersonic.navigation.sidebar.Sidebar;
import moe.seiga.hypersonic.player.Bar;
import moe.seiga.hypersonic.service.api.ConnectionState;
import moe.seiga.hypersonic.service.api.ServerState;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gio.Settings;
import org.gnome.gio.SettingsBindFlags;
import org.gnome.glib.GLib;
import org.gnome.gtk.Stack;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/window.ui", name = "Window")
@SuppressWarnings({"java:S110", "java:S112", "java:S125"})
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Window extends ApplicationWindow {

    private static Application initApp;

    private Settings settings;

    @GtkChild(name = "main_stack")
    public Stack mainStack;

    @GtkChild(name = "welcome_page")
    public WelcomePage welcomePage;

    @GtkChild(name = "sidebar")
    public Sidebar sidebar;

    @GtkChild(name = "player_bar")
    public Bar playerBar;

    public Window(Application app) {
        initApp = app;
        super();
    }

    @InstanceInit
    @SuppressWarnings("unused")
    public void init() {
        var app = initApp;
        initApp = null;
        setApplication(app);
        settings = app.getSettings();

        settings.bind("window-width", this, "default-width", SettingsBindFlags.DEFAULT);
        settings.bind("window-height", this, "default-height", SettingsBindFlags.DEFAULT);
        settings.bind("is-maximized", this, "maximized", SettingsBindFlags.DEFAULT);
        settings.bind("is-fullscreen", this, "fullscreened", SettingsBindFlags.DEFAULT);

        var ss = app.getServerState();
        welcomePage.setup(ss);

        ss.onNotify("connection-state", pspec -> {
            var state = ss.getConnectionState();
            switch (state) {
                case NOT_CONNECTED, INVALID_CREDENTIALS, SERVER_UNREACHABLE, SSL_ERROR ->
                    mainStack.setVisibleChildName("welcome");
                case CONNECTING -> mainStack.setVisibleChildName("loading");
                case CONNECTED -> {
                    mainStack.setVisibleChildName("content");
                    populateSidebar(ss);
                    playerBar.setup(app.getPlayer());
                }
            }
        });

        if (ss.getConnectionState() == ConnectionState.CONNECTING) {
            mainStack.setVisibleChildName("loading");
        }
    }

    private void populateSidebar(ServerState ss) {
        var api = ss.getApi();
        if (api == null) return;
        api.getPlaylists().whenComplete((playlists, ex) -> {
            if (ex != null) {
                log.warn("Failed to load playlists: {}", ex.getMessage());
                return;
            }
            GLib.idleAddOnce(() -> {
                var count = 0;
                for (var p : playlists) {
                    if (count >= 4) break;
                    sidebar.addItem(null, p.getName());
                    count++;
                }
            });
        });
    }
}
