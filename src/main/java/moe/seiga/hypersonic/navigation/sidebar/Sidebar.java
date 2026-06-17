package moe.seiga.hypersonic.navigation.sidebar;

import org.gnome.gtk.Box;
import org.gnome.gtk.ListBox;
import org.gnome.gtk.ListBoxRow;
import org.gnome.gtk.ListBoxUpdateHeaderFunc;
import org.gnome.gtk.Orientation;
import org.gnome.gtk.Separator;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/components/sidebar/sidebar.ui", name = "Sidebar")
public class Sidebar extends Box {

    @GtkChild
    public ListBox sidebar_list;

    public Sidebar() {
        super();
    }

    public Sidebar(MemorySegment address) {
        super(address);
    }

    @InstanceInit
    @SuppressWarnings("unused")
    public void init() {
        sidebar_list.setHeaderFunc((ListBoxUpdateHeaderFunc) (row, before) -> {
            row.setHeader(null);
            if (before != null && row.hasCssClass("section-start")) {
                row.setHeader(new Separator(Orientation.HORIZONTAL));
            }
        });
    }

    private final List<PlaylistItem> items = new ArrayList<>();

    public PlaylistItem addItem(String iconName, String label) {
        var item = new PlaylistItem(iconName, label);
        sidebar_list.append(item);
        items.add(item);
        return item;
    }

    public void clearItems() {
        for (var item : items) {
            sidebar_list.remove(item);
        }
        items.clear();
    }

    public void onItemSelected(java.util.function.Consumer<PlaylistItem> callback) {
        sidebar_list.connect("selected-rows-changed", (Runnable) () -> {
            var selected = sidebar_list.getSelectedRow();
            if (selected instanceof PlaylistItem pi) {
                callback.accept(pi);
            }
        });
    }
}
