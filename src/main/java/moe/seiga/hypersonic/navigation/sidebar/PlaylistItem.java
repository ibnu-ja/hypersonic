package moe.seiga.hypersonic.navigation.sidebar;

import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.ListBoxRow;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/components/sidebar/playlist-item.ui", name = "PlaylistItem")
public class PlaylistItem extends ListBoxRow {

    @GtkChild
    public Image row_icon;

    @GtkChild
    public Label row_label;

    public PlaylistItem() {
        super();
    }

    public PlaylistItem(MemorySegment address) {
        super(address);
    }

    public PlaylistItem(String iconName, String label) {
        this();
        row_icon.setFromIconName(iconName);
        row_icon.setVisible(iconName != null);
        row_label.setText(label);
    }

    public void setIcon(String iconName) {
        row_icon.setFromIconName(iconName);
        row_icon.setVisible(iconName != null);
    }

    public void setLabel(String text) {
        row_label.setText(text);
    }

    public String getLabel() {
        return row_label.getText();
    }
}
