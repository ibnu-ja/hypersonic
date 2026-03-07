package moe.seiga.hypersonic;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gio.Settings;
import org.gnome.gio.SettingsBindFlags;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkTemplate;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/window.ui", name = "MainWindow")
@SuppressWarnings({"java:S110", "java:S112", "java:S125"})
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Window extends ApplicationWindow {

    protected Settings settings;

    public Window(Application app) {
        log.trace("MainWindow constructor");
        setApplication(app);
    }

    @InstanceInit
    @SuppressWarnings("unused")
    public void init() {
        log.trace("MainWindow init");
        settings = new Settings("moe.seiga.Hypersonic");

        settings.bind("window-width", this, "default-width", SettingsBindFlags.DEFAULT);
        settings.bind("window-height", this, "default-height", SettingsBindFlags.DEFAULT);
        settings.bind("is-maximized", this, "maximized", SettingsBindFlags.DEFAULT);
        settings.bind("is-fullscreen", this, "fullscreened", SettingsBindFlags.DEFAULT);
    }
}
