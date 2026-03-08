package moe.seiga.hypersonic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.navigation.settings.SettingWindow;
import moe.seiga.hypersonic.player.controller.Player;
import moe.seiga.hypersonic.player.controller.Song;
import moe.seiga.hypersonic.service.api.ConnectionState;
import org.gnome.gdk.Display;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Variant;
import org.gnome.gtk.IconTheme;

import java.util.List;

@SuppressWarnings("java:S110")
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Application extends org.gnome.adw.Application {

    @Getter
    public Player player;

    @Override
    public void activate() {
        Display display = Display.getDefault();
        if (display != null) {
            IconTheme theme = IconTheme.getForDisplay(display);
            theme.addResourcePath("/moe/seiga/Hypersonic/icons");
        } else {
            log.error("Display.getDefault() returned null inside activate()!");
        }
        present();
    }

    private void present() {
        Window win;
        List<org.gnome.gtk.Window> windows = super.getWindows();
        if (!windows.isEmpty()) {
            win = (Window) windows.getFirst();
        } else {
            win = new Window(this);
        }

        win.present();
    }

    @Override
    public void open(File[] files, @NonNull String hint) {
        present();
    }

    public void preferencesActivated(Variant parameter) {
        Window win = (Window) getActiveWindow();
        SettingWindow settingWindow = new SettingWindow();
        settingWindow.present(win);
    }

    public void quitActivated(Variant parameter) {
        super.quit();
    }

    @Override
    public void startup() {
        super.startup();

        var preferences = new SimpleAction("preferences", null);
        preferences.onActivate(this::preferencesActivated);
        addAction(preferences);

        var quit = new SimpleAction("quit", null);
        quit.onActivate(this::quitActivated);
        addAction(quit);

        String[] quitAccels = new String[]{"<Ctrl>q"};
        setAccelsForAction("app.quit", quitAccels);
    }

    public Application() {
        setApplicationId("moe.seiga.Hypersonic");
        setFlags(ApplicationFlags.HANDLES_OPEN);
        setResourceBasePath("/moe/seiga/Hypersonic");

        this.player = new Player();

        if (!ConnectionState.INSTANCE.isConnected()) {
            ConnectionState.INSTANCE.connect("http://demo.subsonic.org", "guest", "guest");
        }
        ConnectionState.INSTANCE.getApi().getRandomSongs(1).thenAccept(
                randomSongsResponse -> {
                    var song = new Song(randomSongsResponse.getRandomSongs().getSong().getFirst());
                    player.setCurrentSong(song);
                    log.debug("Loaded song: {}", song.getTitle());
                }
        ).exceptionally(throwable -> {
            log.error("Error loading songs:", throwable);
            return null;
        });
    }
}