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
import org.gnome.gtk.Revealer;
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

    @GtkChild(name = "content_stack")
    public Stack contentStack;

    @GtkChild(name = "player_bar")
    public Bar playerBar;

    @GtkChild(name = "player_bar_revealer")
    public Revealer playerBarRevealer;

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

        sidebar.sidebarList.connect("selected-rows-changed", (Runnable) () -> {
            log.debug("changed sidebar item");
            var row = sidebar.sidebarList.getSelectedRow();
            if (row == null) return;
            var name = row.getName();
            var pageName = mapSidebarNameToPage(name);
            if (pageName != null) {
                contentStack.setVisibleChildName(pageName);
                settings.setString("last-page", pageName);
            }
        });

        ss.onNotify("connection-state", pspec -> {
            var state = ss.getConnectionState();
            switch (state) {
                case NOT_CONNECTED, INVALID_CREDENTIALS, SERVER_UNREACHABLE, SSL_ERROR ->
                        mainStack.setVisibleChildName("welcome");
                case CONNECTING -> mainStack.setVisibleChildName("loading");
                case CONNECTED -> {
                    mainStack.setVisibleChildName("content");
                    populateSidebar(ss);
                    contentStack.onNotify("visible-child-name", _ -> {
                        playerBarRevealer.setRevealChild(!"player_page".equals(contentStack.getVisibleChildName()));
                    });
                    playerBar.setup(app.getPlayer());
                    var last = settings.getString("last-page");
                    if (!last.isBlank()) {
                        contentStack.setVisibleChildName(last);
                        selectSidebarRow(last);
                    }
                }
            }
        });

        if (ss.getConnectionState() == ConnectionState.CONNECTING) {
            mainStack.setVisibleChildName("loading");
        }
    }

    private void selectSidebarRow(String pageName) {
        for (int i = 0; ; i++) {
            var row = sidebar.sidebarList.getRowAtIndex(i);
            if (row == null) break;
            if (row.getSelectable()) {
                var name = row.getName();
                if (pageName.equals(mapSidebarNameToPage(name))) {
                    sidebar.sidebarList.selectRow(row);
                    return;
                }
            }
        }
    }

    private String mapSidebarNameToPage(String name) {
        if (name == null) return null;
        return switch (name) {
            case "home" -> "home_page";
            case "player" -> "player_page";
            case "album_all", "album_random", "album_fav", "album_recently_added",
                 "album_recently_played", "album_most_played", "artists", "tracks" -> "testing_page";
            default -> null;
        };
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
