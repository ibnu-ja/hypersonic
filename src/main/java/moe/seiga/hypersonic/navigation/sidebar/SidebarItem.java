package moe.seiga.hypersonic.navigation.sidebar;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.javagi.gtk.annotations.GtkTemplate;
import org.javagi.gobject.annotations.RegisteredType;
import org.jspecify.annotations.NonNull;

import java.lang.foreign.MemorySegment;
import java.util.Optional;

@Slf4j
@RegisteredType(name = "SidebarItem")
@GtkTemplate(ui = "/moe/seiga/Hypersonic/components/sidebar/sidebar-item.ui", name = "SidebarItem")
@SuppressWarnings({"java:S110", "java:S1192"})
@EqualsAndHashCode(callSuper = true)
public class SidebarItem extends org.gnome.adw.SidebarItem {
    @Getter
    @Setter
    private String pageName;

    public SidebarItem() {
        super();
    }

    public SidebarItem(MemorySegment address) {
        super(address);
    }

    public SidebarItem(String title, String iconName, String pageName) {
        super();
        Optional.ofNullable(title).ifPresent(this::setTitle);
        Optional.ofNullable(iconName).ifPresent(this::setIconName);
        Optional.ofNullable(pageName).ifPresent(this::setPageName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends org.gnome.adw.SidebarItem.Builder<Builder> {
        private String pageName;

        public Builder() {
            super();
        }

        public Builder title(String title) {
            setTitle(title);
            return this;
        }

        public Builder iconName(String iconName) {
            setIconName(iconName);
            return this;
        }

        public Builder pageName(String pageName) {
            this.pageName = pageName;
            return this;
        }

        @Override
        public @NonNull SidebarItem build() {
            try {
                var item = new SidebarItem();
                String[] names = getNames();
                org.gnome.gobject.Value[] values = getValues();
                for (int i = 0; i < names.length; i++) {
                    item.setProperty(names[i], values[i]);
                }
                if (pageName != null) {
                    item.setPageName(pageName);
                }
                return item;
            } finally {
                for (org.gnome.gobject.Value value : getValues()) {
                    value.unset();
                }
                getArena().close();
            }
        }
    }
}
