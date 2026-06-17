package moe.seiga.hypersonic.navigation.settings;

import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.gnome.adw.ComboRow;
import org.gnome.adw.PreferencesDialog;
import org.gnome.gobject.GObject;
import org.gnome.gobject.ParamSpec;
import org.gnome.gtk.CustomFilter;
import org.gnome.gtk.CustomFilterFunc;
import org.gnome.gtk.FontDialogButton;
import org.gnome.pango.FontDescription;
import org.gnome.pango.FontFace;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@GtkTemplate(ui = "/moe/seiga/Hypersonic/components/settings/settings.ui", name = "Settings")
@SuppressWarnings({"java:S110", "java:S1192"})
@EqualsAndHashCode(callSuper = true)
public class SettingWindow extends PreferencesDialog {

    @GtkChild(name = "font")
    FontDialogButton font;

    @GtkChild(name = "transition")
    ComboRow transition;

    org.gnome.gio.Settings settings;

    private static final List<String> TRANSITION_IDS = Arrays.asList(
            "none",
            "crossfade",
            "slide-left-right"
    );

    public SettingWindow() {
        super();
        settings = new org.gnome.gio.Settings("moe.seiga.Hypersonic");
    }

    @InstanceInit
    @SuppressWarnings("unused")
    public void init() {
        handleFontSettings();

        handleTransitionConfig();

        var fontDialog = font.getDialog();
        CustomFilterFunc monospaceFilterFunc = (GObject item) -> {
            if (item instanceof FontFace face) {
                return face.getFamily().isMonospace();
            }
            log.warn("{} is not a FontFace", item.getClass().getName());
            return false;
        };
        var monospaceFilter = new CustomFilter(monospaceFilterFunc);
        if (fontDialog != null) {
            fontDialog.setFilter(monospaceFilter);
        }

        setSearchEnabled(true);
    }

    private void handleTransitionConfig() {
        String transitionString = settings.getString("transition");
        int initialIndex = TRANSITION_IDS.indexOf(transitionString);
        if (initialIndex == -1) initialIndex = 0;
        transition.setSelected(initialIndex);
        transition.onNotify(
                "selected", (ParamSpec ps) -> {
                    if (ps != null && !"selected".equals(ps.getName())) return;
                    int sel = transition.getSelected();
                    if (sel >= 0 && sel < TRANSITION_IDS.size()) {
                        String newVal = TRANSITION_IDS.get(sel);
                        if (!newVal.equals(settings.getString("transition"))) {
                            settings.setString("transition", newVal);
                        }
                    }
                }
        );
    }

    private void handleFontSettings() {
        String fontString = settings.getString("font");
        FontDescription fontDesc = FontDescription.fromString(fontString);
        font.setFontDesc(fontDesc);
        font.onNotify(
                "font-desc", (ParamSpec ps) -> {
                    if (ps != null && !"font-desc".equals(ps.getName())) return;
                    String newFontString = Objects.requireNonNull(font.getFontDesc()).toString();
                    if (!newFontString.equals(settings.getString("font"))) {
                        settings.setString("font", newFontString);
                    }
                }
        );
    }
}
