package moe.seiga.hypersonic.player.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@Slf4j
@RegisteredType(name = "Player")
public class Player extends GObject {
    @Getter
    @Setter
    private PlaybackState state;
    @Getter
    private Song currentSong;
    @Getter
    private double position; // in seconds

    public void setCurrentSong(Song song) {
        this.currentSong = song;
        log.info("Current song: {}", this.currentSong.getTitle());
        notify("current-song");
    }

    public void setPosition(double position) {
        this.position = position;
        notify("position");
    }
}
