package moe.seiga.hypersonic;

import moe.seiga.hypersonic.navigation.settings.SettingWindow;
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
import org.javagi.base.GErrorException;
import org.javagi.base.Out;
import org.javagi.util.Intl;

import java.util.List;

import static moe.seiga.Config.*;

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
        // TemplateTypes.register(PlaybackWidget.class);

        Resource resource = Resource.load(RESOURCE_DIR + RESOURCE_FILENAME);

        resource.resourcesRegister();

        new Application().run(gstArgs.get());
    }

    @SuppressWarnings("java:S110")
    @EqualsAndHashCode(callSuper = true)
    public static class Application extends org.gnome.adw.Application {

        @Override
        public void activate() {
            Display display = Display.getDefault();
            if (display != null) {
                IconTheme theme = IconTheme.getForDisplay(display);
                theme.addResourcePath("/moe/seiga/Hypersonic/icons");
            } else {
                log.error("Display.getDefault() returned null inside activate()!");
            }
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
            Window win;
            List<org.gnome.gtk.Window> windows = super.getWindows();
            if (!windows.isEmpty())
                win = (Window) windows.getFirst();
            else
                win = new Window(this);
            win.present();
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
            setApplicationId(APPLICATION_ID);
            setFlags(ApplicationFlags.HANDLES_OPEN);
        }
    }
}
