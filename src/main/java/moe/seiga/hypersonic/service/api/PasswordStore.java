package moe.seiga.hypersonic.service.api;

import lombok.extern.slf4j.Slf4j;
import moe.seiga.Config;
import org.gnome.gio.Cancellable;
import org.gnome.glib.HashTable;
import org.gnome.secret.Schema;
import org.gnome.secret.SchemaAttributeType;
import org.gnome.secret.SchemaFlags;
import org.gnome.secret.Secret;

import java.util.EnumSet;

@Slf4j
public final class PasswordStore {

    private static final String SCHEMA_NAME = Config.APPLICATION_ID + ".Password";

    public void store(String password) {
        try {
            Secret.passwordStoreSync(
                    createSchema(), createAttributes(),
                    null, "Hypersonic Subsonic", password, new Cancellable()
            );
        } catch (Exception e) {
            log.error("libsecret store failed", e);
        }
    }

    public String lookup() {
        try {
            return Secret.passwordLookupSync(
                    createSchema(), createAttributes(), new Cancellable()
            );
        } catch (Exception e) {
            log.error("libsecret lookup failed", e);
            return null;
        }
    }

    public void clear() {
        try {
            Secret.passwordClearSync(
                    createSchema(), createAttributes(), new Cancellable()
            );
        } catch (Exception e) {
            log.error("libsecret clear failed", e);
        }
    }

    private Schema createSchema() {
        var attrs = new HashTable<String, SchemaAttributeType>(null, null, null, null);
        attrs.insert("type", SchemaAttributeType.STRING);
        return new Schema(SCHEMA_NAME, EnumSet.of(SchemaFlags.NONE), attrs);
    }

    private HashTable<String, String> createAttributes() {
        var attrs = new HashTable<String, String>(null, null, null, null);
        attrs.insert("type", "subsonic");
        return attrs;
    }
}
