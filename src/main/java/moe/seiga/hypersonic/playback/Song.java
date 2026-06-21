package moe.seiga.hypersonic.playback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import moe.seiga.hypersonic.connection.ServerConnection;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@RegisteredType(name = "Song")
@AllArgsConstructor
public class Song extends GObject {
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String artist;
    @Getter
    @Setter
    private String album;
    @Getter
    @Setter
    private int duration;

    @Getter
    @Setter
    private String coverArtId;

    public String getUrl() {
        if (id == null) return null;

        return ServerConnection.INSTANCE.getApi().streamUrl(id);
    }
}
