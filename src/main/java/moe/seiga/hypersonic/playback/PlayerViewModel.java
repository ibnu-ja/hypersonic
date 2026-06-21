package moe.seiga.hypersonic.playback;

import lombok.Getter;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@RegisteredType(name = "PlayerViewModel")
public class PlayerViewModel extends GObject {

    @Getter
    private Song currentSong;

    @Getter
    private PlaybackState state = PlaybackState.STOPPED;

    @Getter
    private double position = 0.0; // in seconds

    @Getter
    private double duration = 0.0; // in seconds

    @Getter
    private double volume = 1.0;

    public void setCurrentSong(Song song) {
        if (this.currentSong != song) {
            this.currentSong = song;
            notify("current-song");
            notify("title");
            notify("artist");
            notify("album");
        }
    }

    public void setState(PlaybackState state) {
        if (this.state != state) {
            this.state = state;
            notify("state");
            notify("playing");
        }
    }

    public void setPosition(double position) {
        if (this.position != position) {
            this.position = position;
            notify("position");
        }
    }

    public void setPositionNanos(long nanos) {
        setPosition(nanos / 1e9);
    }

    public void setDuration(double duration) {
        if (this.duration != duration) {
            this.duration = duration;
            notify("duration");
        }
    }

    public void setDurationNanos(long nanos) {
        setDuration(nanos / 1e9);
    }

    public void setVolume(double volume) {
        if (this.volume != volume) {
            this.volume = volume;
            notify("volume");
        }
    }

    public String getTitle() {
        return currentSong != null ? currentSong.getTitle() : "No Song";
    }

    public String getArtist() {
        return currentSong != null ? currentSong.getArtist() : "Unknown Artist";
    }

    public String getAlbum() {
        return currentSong != null ? currentSong.getAlbum() : "";
    }

    public boolean isPlaying() {
        return state == PlaybackState.PLAYING;
    }
}
