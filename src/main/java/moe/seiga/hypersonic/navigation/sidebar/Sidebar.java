package moe.seiga.hypersonic.navigation.sidebar;

import org.gnome.gtk.Box;
import org.gnome.gtk.ListBox;
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

    @GtkChild(name = "sidebar_list")
    public ListBox sidebarList;

    public Sidebar() {
        super();
    }

    public Sidebar(MemorySegment address) {
        super(address);
    }

    @InstanceInit
    @SuppressWarnings("unused")
    public void init() {
        sidebarList.setHeaderFunc((row, before) -> {
            row.setHeader(null);
            if (before != null && row.hasCssClass("section-start")) {
                row.setHeader(new Separator(Orientation.HORIZONTAL));
            }
        });
    }

    private final List<PlaylistItem> items = new ArrayList<>();

    public void addItem(String iconName, String label) {
        var item = new PlaylistItem(iconName, label);
        sidebarList.append(item);
        items.add(item);
    }

    public void clearItems() {
        for (var item : items) {
            sidebarList.remove(item);
        }
        items.clear();
    }

    public void onItemSelected(java.util.function.Consumer<PlaylistItem> callback) {
        sidebarList.connect("selected-rows-changed", (Runnable) () -> {
            var selected = sidebarList.getSelectedRow();
            if (selected instanceof PlaylistItem pi) {
                callback.accept(pi);
            }
        });
    }
}
