package moe.seiga.hypersonic;

import lombok.extern.slf4j.Slf4j;
import moe.seiga.Config;
import moe.seiga.hypersonic.navigation.connection.WelcomePage;
import moe.seiga.hypersonic.navigation.sidebar.SidebarItem;
import moe.seiga.hypersonic.player.Bar;
import moe.seiga.hypersonic.player.PlaybackControls;
import moe.seiga.hypersonic.player.Seekbar;
import moe.seiga.hypersonic.player.controller.PlaybackState;
import moe.seiga.hypersonic.player.controller.RepeatMode;
import moe.seiga.hypersonic.service.api.ConnectionState;
import moe.seiga.hypersonic.service.api.ServerState;
import org.freedesktop.gstreamer.gst.Gst;
import org.gnome.gdkpixbuf.Pixbuf;
import org.gnome.gio.Resource;
import org.javagi.base.GErrorException;
import org.javagi.base.Out;
import org.javagi.gobject.types.Types;
import org.javagi.gtk.types.TemplateTypes;
import org.javagi.util.Intl;



@Slf4j
@SuppressWarnings({"java:S1118", "java:S125"})
public class Main {

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

        Intl.bindtextdomain(Config.GETTEXT_PACKAGE, Config.LOCALE_DIR);
        Intl.textdomain(Config.GETTEXT_PACKAGE);

        // Load GResource first so templates can read from it
        Resource resource = Resource.load(Config.RESOURCE_DIR + Config.RESOURCE_FILENAME);
        resource.resourcesRegister();

        // Register Template Classes
        TemplateTypes.register(Bar.class);
        TemplateTypes.register(PlaybackControls.class);
        TemplateTypes.register(Seekbar.class);
        TemplateTypes.register(WelcomePage.class);
        TemplateTypes.register(SidebarItem.class);

        Types.register(PlaybackState.class);
        Types.register(RepeatMode.class);
        Types.register(ConnectionState.class);
        Types.register(ServerState.class);

        new Application().run(args);
    }
}
