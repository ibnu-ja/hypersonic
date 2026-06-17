package moe.seiga.hypersonic;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.player.Bar;
import org.gnome.adw.ApplicationWindow;
import org.gnome.gio.Settings;
import org.gnome.gio.SettingsBindFlags;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/window.ui", name = "Window")
@SuppressWarnings({"java:S110", "java:S112", "java:S125"})
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Window extends ApplicationWindow {

    private Settings settings;

    public Settings getAppSettings() {
        return settings;
    }

    @GtkChild(name = "player_bar")
    public Bar playerBar;

    public Window(Application app) {
        super();
        setApplication(app);
        settings = app.getSettings();

        settings.bind("window-width", this, "default-width", SettingsBindFlags.DEFAULT);
        settings.bind("window-height", this, "default-height", SettingsBindFlags.DEFAULT);
        settings.bind("is-maximized", this, "maximized", SettingsBindFlags.DEFAULT);
        settings.bind("is-fullscreen", this, "fullscreened", SettingsBindFlags.DEFAULT);

        playerBar.setup(app.getPlayer());
    }

    @InstanceInit
    @SuppressWarnings("unused")
    public void init() {
    }
}
