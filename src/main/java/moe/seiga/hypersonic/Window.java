package moe.seiga.hypersonic;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import dev.zt64.subsonic.api.model.Playlist;
import moe.seiga.hypersonic.navigation.connection.WelcomePage;
import moe.seiga.hypersonic.navigation.sidebar.SidebarItem;
import moe.seiga.hypersonic.player.Bar;
import moe.seiga.hypersonic.service.api.ConnectionState;
import moe.seiga.hypersonic.service.api.ServerState;
import org.gnome.adw.ApplicationWindow;
import org.gnome.adw.Sidebar;
import org.gnome.adw.SidebarSection;
import org.gnome.gio.Settings;
import org.gnome.gio.SettingsBindFlags;
import org.gnome.gtk.Revealer;
import org.gnome.gtk.Stack;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.util.List;

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

    @GtkChild(name = "playlist_sidebar_section")
    public SidebarSection playlistSidebarSection;

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

        sidebar.onNotify("selected-item", pspec -> {
            var item = sidebar.getSelectedItem();
            if (item instanceof SidebarItem myItem) {
                String page = myItem.getPageName();
                if (page != null && !page.equals(contentStack.getVisibleChildName())) {
                    contentStack.setVisibleChildName(page);
                    settings.setString("last-page", page);
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
                    ss.connect("playlists-changed", (ServerState.PlaylistsChanged) () -> populatePlaylistSidebarSection(ss.getPlaylists()));
                    populatePlaylistSidebarSection(ss.getPlaylists());
                    contentStack.onNotify("visible-child-name", _ -> {
                        String visibleTag = contentStack.getVisibleChildName();
                        if (visibleTag != null) {
                            playerBarRevealer.setRevealChild(!"player_page".equals(visibleTag));
                            settings.setString("last-page", visibleTag);
                            selectSidebar(visibleTag);
                        }
                    });
                    playerBar.setup(app.getPlayer());
                    var last = settings.getString("last-page");
                    if (!last.isBlank()) {
                        selectPage(last);
                    }
                }
            }
        });

        if (ss.getConnectionState() == ConnectionState.CONNECTING) {
            mainStack.setVisibleChildName("loading");
        }
    }

    private void selectSidebar(String pageName) {
        var model = sidebar.getItems();
        int nItems = model.getNItems();
        for (int i = 0; i < nItems; i++) {
            var item = model.getItem(i);
            if (item instanceof SidebarItem myItem && pageName.equals(myItem.getPageName())) {
                if (sidebar.getSelectedItem() != myItem) {
                    sidebar.setSelected(i);
                }
                break;
            }
        }
    }

    private void selectPage(String targetPageName) {
        selectSidebar(targetPageName);
        if (!targetPageName.equals(contentStack.getVisibleChildName())) {
            contentStack.setVisibleChildName(targetPageName);
        }
        settings.setString("last-page", targetPageName);
    }



    private void populatePlaylistSidebarSection(List<Playlist> playlists) {
        // todo: check if sidebar can support paginations
        // todo check if loading many playlist (100++ playlists) will break the app
        var count = 0;
        for (var p : playlists) {
            if (count >= 4) break;
            // todo when playlist page done, add property page_name
            var playlistItem = SidebarItem.builder().title(p.getName()).build();
            playlistSidebarSection.append(playlistItem);
            count++;
        }
    }
}
