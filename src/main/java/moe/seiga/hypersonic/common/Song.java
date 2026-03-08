package moe.seiga.hypersonic.common;

import lombok.Getter;
import org.gnome.gobject.GObject;

import java.lang.foreign.MemorySegment;

public class Song extends GObject {

    // Internal fields
    @Getter
    private final SongInfo info;

    @Getter
    private int queuePos;

    @Getter
    private boolean isPlaying;

    // Constructor
    public Song(SongInfo info) {
        super();
        this.info = info;
        this.queuePos = info.queueId() != null ? info.queueId() : 0;
        this.isPlaying = false;
    }

    public Song(MemorySegment address) {
        super(address);
        this.info = null;
        this.queuePos = 0;
        this.isPlaying = false;
    }

    public String getUri() {
        return info.uri();
    }

    public String getName() {
        return info.title();
    }

    public String getArtist() {
        if (info.artists() == null || info.artists().isEmpty()) {
            return null;
        }
        return String.join(", ", info.artists());
    }

    public long getDuration() {
        return info.duration() == null ? 0 : info.duration().getSeconds();
    }

    public int getQueueId() {
        return info.queueId() != null ? info.queueId() : 0;
    }

    public void setQueuePos(int newPos) {
        if (this.queuePos != newPos) {
            this.queuePos = newPos;
            notify("queue-pos");
        }
    }

    public String getAlbum() {
        return info.album();
    }

    // Additional methods

    public String getArtistStr() {
        return getArtist();
    }

    public String getAlbumTitle() {
        return info.album();
    }

    public void setIsPlaying(boolean val) {
        if (this.isPlaying != val) {
            this.isPlaying = val;
            notify("is-playing");
        }
    }
}
