package moe.seiga.hypersonic.navigation.connection;

import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.service.api.ServerState;
import org.gnome.adw.EntryRow;
import org.gnome.adw.PasswordEntryRow;
import org.gnome.gtk.Box;
import org.gnome.gtk.Button;
import org.gnome.gtk.Spinner;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;

@Slf4j
@GtkTemplate(ui = "/moe/seiga/Hypersonic/components/connection/welcome-page.ui", name = "WelcomePage")
public class WelcomePage extends Box {

    @GtkChild
    public EntryRow url_entry;

    @GtkChild
    public EntryRow username_entry;

    @GtkChild
    public PasswordEntryRow password_entry;

    @GtkChild
    public Button connect_button;

    @GtkChild
    public Spinner spinner;

    private ServerState serverState;

    public WelcomePage() {
        super();
    }

    public WelcomePage(MemorySegment address) {
        super(address);
    }

    public void setup(ServerState ss) {
        this.serverState = ss;

        url_entry.connect("changed", (Runnable) () -> {
            var text = url_entry.getText();
            //noinspection HttpUrlsUsage
            if (!text.isBlank() && (text.startsWith("http://") || text.startsWith("https://"))) {
                url_entry.removeCssClass("error");
                url_entry.setTooltipText(null);
            }
        });
        username_entry.connect("changed", (Runnable) () -> {
            var text = username_entry.getText();
            if (!text.isBlank()) {
                username_entry.removeCssClass("error");
                username_entry.setTooltipText(null);
            }
        });
        password_entry.connect("changed", (Runnable) () -> {
            var text = password_entry.getText();
            if (!text.isBlank()) {
                password_entry.removeCssClass("error");
                password_entry.setTooltipText(null);
            }
        });

        ss.onNotify("connection-state", _ -> {
            var state = ss.getConnectionState();
            switch (state) {
                case CONNECTING -> {
                    connect_button.setSensitive(false);
                    spinner.setVisible(true);
                    spinner.start();
                }
                case INVALID_CREDENTIALS, SERVER_UNREACHABLE, SSL_ERROR, CONNECTED, NOT_CONNECTED -> {
                    connect_button.setSensitive(true);
                    spinner.setVisible(false);
                    spinner.stop();
                }
            }
        });
    }

    @SuppressWarnings("unused")
    public void on_connect() {
        if (serverState == null) return;
        String url = url_entry.getText();
        String user = username_entry.getText();
        String pass = password_entry.getText();

        if (validateField(url_entry, url, true)) return;
        if (validateField(username_entry, user, false)) return;
        if (validateField(password_entry, pass, false)) return;

        log.info("Connecting to {} as {}", url, user);
        serverState.startConnection(url, user, pass);
    }

    private boolean validateField(EntryRow entry, String text, boolean isUrl) {
        if (text == null || text.isBlank()) {
            entry.addCssClass("error");
            entry.setTooltipText("Required");
            return true;
        }
        //noinspection HttpUrlsUsage
        if (isUrl && !text.startsWith("http://") && !text.startsWith("https://")) {
            entry.addCssClass("error");
            //noinspection HttpUrlsUsage
            entry.setTooltipText("Must start with http:// or https://");
            return true;
        }
        entry.removeCssClass("error");
        entry.setTooltipText(null);
        return false;
    }
}
