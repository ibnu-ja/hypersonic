package moe.seiga.hypersonic.player.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

import java.util.Objects;

@RegisteredType(name = "Song")
@AllArgsConstructor
public class Song extends GObject {
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

    public Song(ru.stersh.subsonic.api.model.Song song) {
        this.title = song.getTitle();
        this.artist = song.getArtist();
        this.album = song.getAlbum();
        this.duration = Objects.requireNonNullElse(song.getDuration(), 0);
    }


}

