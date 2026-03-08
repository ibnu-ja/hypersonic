package moe.seiga.hypersonic;

import lombok.extern.slf4j.Slf4j;
import moe.seiga.Config;
import moe.seiga.hypersonic.player.Bar;
import moe.seiga.hypersonic.player.PlaybackControls;
import moe.seiga.hypersonic.player.Seekbar;
import org.gnome.gio.Resource;
import org.javagi.base.GErrorException;
import org.javagi.gtk.types.TemplateTypes;
import org.javagi.util.Intl;


@Slf4j
@SuppressWarnings({"java:S1118", "java:S125"})
public class Main {

    @SuppressWarnings({"java:S1444", "java:S1104", "java:S1135"})
    static void main(String[] args) throws GErrorException {
        LoggingBootstrap.init();

        Intl.bindtextdomain(Config.GETTEXT_PACKAGE, Config.LOCALE_DIR);
        Intl.textdomain(Config.GETTEXT_PACKAGE);
        // Register Template Classes
        TemplateTypes.register(Bar.class);
        TemplateTypes.register(PlaybackControls.class);
        TemplateTypes.register(Seekbar.class);

        Resource resource = Resource.load(Config.RESOURCE_DIR + Config.RESOURCE_FILENAME);

        resource.resourcesRegister();

        new Application().run(args);
    }
}
