package io.ibnuja.hypersonic.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@EqualsAndHashCode(callSuper = true)
@RegisteredType(name = "Song")
@Data
public class Song extends GObject {

    private String id;
    private String title;
    private String artist;
    private String artistId;
    private String album;
    private String albumId;
    private String coverArt;

    public Song(ru.stersh.subsonic.api.model.Song song) {
        this.id = song.getId();
        this.title = song.getTitle();
        this.artist = song.getArtist();
        this.artistId = song.getArtistId();
        this.album = song.getAlbum();
        this.albumId = song.getAlbumId();
        this.coverArt = song.getCoverArt();
    }
}
