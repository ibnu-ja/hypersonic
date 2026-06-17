package moe.seiga.hypersonic.navigation.connection;

import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.service.api.ConnectionState;
import moe.seiga.hypersonic.service.api.ServerState;
import org.gnome.adw.EntryRow;
import org.gnome.adw.PasswordEntryRow;
import org.gnome.adw.StatusPage;
import org.gnome.gtk.Box;
import org.gnome.gtk.Button;
import org.gnome.gtk.PasswordEntry;
import org.gnome.gtk.Spinner;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

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

    public void setup(ServerState ss) {
        this.serverState = ss;

        ss.onNotify("connection-state", pspec -> {
            var state = ss.getConnectionState();
            switch (state) {
                case CONNECTING -> {
                    connect_button.setSensitive(false);
                    spinner.setVisible(true);
                    spinner.start();
                }
                case CONNECTED, NOT_CONNECTED -> {
                    connect_button.setSensitive(true);
                    spinner.setVisible(false);
                    spinner.stop();
                }
                case INVALID_CREDENTIALS, SERVER_UNREACHABLE, SSL_ERROR -> {
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
        log.info("Connecting to {} as {}", url, user);
        serverState.startConnection(url, user, pass);
    }
}
