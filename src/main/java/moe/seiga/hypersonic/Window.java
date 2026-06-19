package moe.seiga.hypersonic;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.navigation.connection.WelcomePage;
import moe.seiga.hypersonic.navigation.sidebar.Sidebar;
import moe.seiga.hypersonic.player.Bar;
import moe.seiga.hypersonic.service.api.ConnectionState;
import moe.seiga.hypersonic.service.api.ServerState;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.WindowTitle;
import org.gnome.gio.Settings;
import org.gnome.gio.SettingsBindFlags;
import org.gnome.glib.GLib;
import org.gnome.gtk.Label;
import org.gnome.gtk.ListBoxRow;
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

    @GtkChild(name = "content_title")
    public WindowTitle contentTitle;

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

            sidebar.sidebar_list.connect("selected-rows-changed", (Runnable) () -> {
            log.debug("changed sidebar item");
            var row = sidebar.sidebar_list.getSelectedRow();
            if (row == null) return;
            var label = findLabel(row);
            if (label != null) {
                var pageName = mapSidebarToPage(label);
                if (pageName != null) {
                    contentStack.setVisibleChildName(pageName);
                    contentTitle.setTitle(label);
                    settings.setString("last-page", pageName);
                }
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

    private String findLabel(ListBoxRow row) {
        var child = row.getChild();
        if (child instanceof org.gnome.gtk.Box box) {
            for (var c = box.getFirstChild(); c != null; c = c.getNextSibling()) {
                if (c instanceof Label l) return l.getText();
            }
        }
        return null;
    }

    private void selectSidebarRow(String pageName) {
        for (int i = 0; ; i++) {
            var row = sidebar.sidebar_list.getRowAtIndex(i);
            if (row == null) break;
            if (row.getSelectable()) {
                var label = findLabel(row);
                if (label != null && pageName.equals(mapSidebarToPage(label))) {
                    sidebar.sidebar_list.selectRow(row);
                    return;
                }
            }
        }
    }

    private String mapSidebarToPage(String label) {
        return switch (label) {
            case "Home" -> "home_page";
            case "Player" -> "player_page";
            case "All", "Random", "Favorites", "Recently Played", "Recently Added", "Artist", "Most Played", "Tracks" -> "testing_page";
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
