package moe.seiga.hypersonic;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import moe.seiga.Config;
import moe.seiga.hypersonic.ui.SettingWindow;
import moe.seiga.hypersonic.playback.PlaybackViewModel;
import moe.seiga.hypersonic.connection.ConnectionViewModel;
import moe.seiga.hypersonic.ui.Window;
import org.gnome.gdk.Display;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.gio.Settings;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Variant;
import org.gnome.gtk.IconTheme;

import java.util.List;

@SuppressWarnings("java:S110")
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Application extends org.gnome.adw.Application {

    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    private Settings settings;

    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    private PlaybackViewModel playbackViewModel;

    @SuppressWarnings("FieldMayBeFinal")
    @Getter
    private ConnectionViewModel connectionViewModel;

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
        SettingWindow settingWindow = new SettingWindow(settings);
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
        setApplicationId(Config.APPLICATION_ID);
        setFlags(ApplicationFlags.HANDLES_OPEN);
        setResourceBasePath("/moe/seiga/Hypersonic");

        this.settings = new Settings(Config.APPLICATION_ID);
        this.playbackViewModel = new PlaybackViewModel();
        this.connectionViewModel = new ConnectionViewModel(settings);
    }
}