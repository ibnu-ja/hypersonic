package io.ibnuja.hypersonic.playback;

import io.ibnuja.hypersonic.model.Song;
import io.ibnuja.hypersonic.service.api.ConnectionState;
import io.ibnuja.hypersonic.service.audio.GstBackend;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.gstreamer.gst.State;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.Property;
import org.javagi.gobject.annotations.RegisteredType;

@Slf4j
@RegisteredType(name = "PlayerState")
@EqualsAndHashCode(callSuper = true)
public class PlayerState extends GObject {

    private boolean playing;
    private Song currentSong;

    private final GstBackend backend;

    public PlayerState(GstBackend backend) {
        this.backend = backend;

        this.backend.onNotify("state", _ -> {
            State state = backend.getState();
            boolean realState = (state == State.PLAYING);

            if (this.playing != realState) {
                this.playing = realState;
                log.info("State {} playing state {}", realState, this.playing);
                notify("playing");
            }
        });

        this.backend.connect("eos", (GstBackend.EosSignal) () -> {
            log.info("Song finished, playing next...");
        });
    }

    @SuppressWarnings("unused")
    @Property(name = "playing")
    public boolean isPlaying() {
        return playing;
    }

    @Property(name = "current-song")
    public Song getCurrentSong() {
        return currentSong;
    }

    public void togglePlay() {
        if (this.playing) {
            backend.pause();
        } else {
            backend.play();
        }
    }

    public void playSong(Song song) {
        log.debug("playSong {}", song);
        this.currentSong = song;
        notify("current-song");

        backend.setUrl(ConnectionState.INSTANCE.getApi().streamUrl(song.getId()));
        backend.play();
    }
}
