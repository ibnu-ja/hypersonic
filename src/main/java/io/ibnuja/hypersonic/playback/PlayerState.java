package io.ibnuja.hypersonic.playback;

import io.ibnuja.hypersonic.model.Song;
import io.ibnuja.hypersonic.service.audio.GstBackend;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@Slf4j
@RegisteredType(name = "PlayerState")
@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
public class PlayerState extends GObject {

    @Getter
    private boolean playing;

    @Getter
    private Song currentSong;

    public void setPlaying(boolean playing) {
        log.debug("setPlaying {}", playing);
        this.playing = playing;
        notify("playing");
    }

    public void togglePlay() {
        setPlaying(!playing);
    }

    public void setCurrentSong(Song song) {
        log.debug("setCurrentSong {}", song);
        this.currentSong = song;
        notify("current-song");
    }

    public void playSong(Song song) {
        log.debug("playSong {}", song);
        setCurrentSong(song);
        setPlaying(true);
    }

    public void setup(GstBackend backend) {
        backend.onNotify("url", _ -> {
            String song = backend.getUrl();
            if (song != null) {
                log.error("TOLOL {}", song);
            }
        });
    }
}
