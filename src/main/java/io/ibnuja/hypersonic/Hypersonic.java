package io.ibnuja.hypersonic;

import io.ibnuja.hypersonic.model.Song;
import io.ibnuja.hypersonic.navigation.selection.SelectionToolbarWidget;
import io.ibnuja.hypersonic.navigation.settings.SettingWindow;
import io.ibnuja.hypersonic.navigation.sidebar.SidebarRow;
import io.ibnuja.hypersonic.playback.ControlsWidget;
import io.ibnuja.hypersonic.playback.InfoWidget;
import io.ibnuja.hypersonic.playback.PlaybackWidget;
import io.ibnuja.hypersonic.playback.PlayerState;
import io.ibnuja.hypersonic.service.api.ConnectionState;
import io.ibnuja.hypersonic.service.audio.GstBackend;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.gstreamer.gst.Gst;
import org.gnome.gdk.Display;
import org.gnome.gdkpixbuf.Pixbuf;
import org.gnome.gio.ApplicationFlags;
import org.gnome.gio.File;
import org.gnome.gio.Resource;
import org.gnome.gio.SimpleAction;
import org.gnome.glib.Variant;
import org.gnome.gtk.IconTheme;
import org.gnome.gtk.Window;
import org.javagi.base.GErrorException;
import org.javagi.base.Out;
import org.javagi.gtk.types.TemplateTypes;
import org.javagi.util.Intl;

import java.util.List;

import static io.ibnuja.Config.*;

@Slf4j
@SuppressWarnings({"java:S1118", "java:S125"})
public class Hypersonic {

    @SuppressWarnings({"java:S1444", "java:S1104", "java:S1135"})
    static void main(String[] args) throws GErrorException {
        LoggingBootstrap.init();
        Out<String[]> gstArgs = new Out<>(args);
        Gst.init(gstArgs);

        Pixbuf.getFormats().forEach(pixbufFormat -> {
            assert pixbufFormat != null;
            log.debug(
                    "pixbuf Format supported: {}, {} ",
                    pixbufFormat.getName(),
                    pixbufFormat.getDescription()
            );
        });

        Intl.bindtextdomain(APPLICATION_ID, LOCALE_DIR);
        Intl.textdomain(APPLICATION_ID);
        // Register Template Classes
        TemplateTypes.register(PlaybackWidget.class);
        TemplateTypes.register(InfoWidget.class);
        TemplateTypes.register(ControlsWidget.class);
        TemplateTypes.register(SelectionToolbarWidget.class);
        TemplateTypes.register(SidebarRow.class);

        Resource resource = Resource.load(RESOURCE_DIR + RESOURCE_FILENAME);

        resource.resourcesRegister();

        new Application().run(gstArgs.get());

        ConnectionState.INSTANCE.disconnect();
    }

    @SuppressWarnings("java:S110")
    @EqualsAndHashCode(callSuper = true)
    public static class Application extends org.gnome.adw.Application {

        protected PlayerState playerState;

        protected final GstBackend backend = new GstBackend();

        @Override
        public void activate() {
            Display display = Display.getDefault();
            if (display != null) {
                IconTheme theme = IconTheme.getForDisplay(display);
                theme.addResourcePath("/io/ibnuja/Hypersonic/icons");
            } else {
                log.error("Display.getDefault() returned null inside activate()!");
            }
            MainWindow win;
            List<Window> windows = super.getWindows();
            if (!windows.isEmpty()) {
                win = (MainWindow) windows.getFirst();
            } else {
                win = new MainWindow(this);
            }

            win.present();
        }

        @Override
        public void open(File[] files, @NonNull String hint) {
            MainWindow win;
            List<Window> windows = super.getWindows();
            if (!windows.isEmpty())
                win = (MainWindow) windows.getFirst();
            else
                win = new MainWindow(this);
            win.present();
        }

        public void preferencesActivated(Variant parameter) {
            MainWindow win = (MainWindow) getActiveWindow();
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
            setApplicationId(APPLICATION_ID);
            setFlags(ApplicationFlags.HANDLES_OPEN);
            if (!ConnectionState.INSTANCE.isConnected()) {
                ConnectionState.INSTANCE.connect("http://demo.subsonic.org", "guest", "guest");
            }
            this.playerState = new PlayerState(backend);
            ConnectionState.INSTANCE.getApi().getRandomSongs(1).thenAccept(
                    randomSongsResponse -> {
                        var song = randomSongsResponse.getRandomSongs().getSong().getFirst();
                        log.debug("Loaded song: {}", song);
                        playerState.playSong(new Song(song));
                    }
            ).exceptionally(throwable -> {
                log.error("Error loading songs:", throwable);
                return null;
            });
        }
    }
}
