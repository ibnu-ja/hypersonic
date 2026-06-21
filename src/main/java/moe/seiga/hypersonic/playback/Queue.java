package moe.seiga.hypersonic.playback;

import lombok.Getter;
import org.gnome.gio.ListStore;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@RegisteredType(name = "Queue")
public class Queue extends GObject {

    @Getter
    private final ListStore<Song> songs;

    @Getter
    private int currentIndex = -1;

    @Getter
    private RepeatMode repeatMode = RepeatMode.NONE;

    public Queue() {
        this.songs = new ListStore<>();
    }

    public void setCurrentIndex(int index) {
        if (this.currentIndex != index) {
            this.currentIndex = index;
            notify("current-index");
            notify("current-song");
        }
    }

    public void setRepeatMode(RepeatMode mode) {
        if (this.repeatMode != mode) {
            this.repeatMode = mode;
            notify("repeat-mode");
        }
    }

    public Song getCurrentSong() {
        int n = (int) songs.getNItems();
        if (currentIndex >= 0 && currentIndex < n) {
            return songs.getItem(currentIndex);
        }
        return null;
    }

    public int getNSongs() {
        return (int) songs.getNItems();
    }

    public void addSong(Song song) {
        songs.append(song);
        notify("n-songs");
        if (currentIndex == -1) {
            setCurrentIndex(0);
        }
    }

    public void addSongs(Song[] newSongs) {
        for (Song song : newSongs) {
            songs.append(song);
        }
        notify("n-songs");
        if (currentIndex == -1 && newSongs.length > 0) {
            setCurrentIndex(0);
        }
    }

    public void removeAt(int index) {
        int n = (int) songs.getNItems();
        if (index >= 0 && index < n) {
            songs.remove(index);
            notify("n-songs");
            if (currentIndex >= index) {
                setCurrentIndex(Math.max(-1, currentIndex - 1));
            }
        }
    }

    public void clear() {
        songs.removeAll();
        notify("n-songs");
        setCurrentIndex(-1);
    }

    public Song playIndex(int index) {
        int n = (int) songs.getNItems();
        if (index >= 0 && index < n) {
            setCurrentIndex(index);
            return songs.getItem(index);
        }
        return null;
    }

    public Song next() {
        int n = (int) songs.getNItems();
        if (n == 0) return null;

        switch (repeatMode) {
            case NONE:
                if (currentIndex + 1 >= n) return null;
                setCurrentIndex(currentIndex + 1);
                return songs.getItem(currentIndex);
            case ALL:
                setCurrentIndex((currentIndex + 1) % n);
                return songs.getItem(currentIndex);
            case ONE:
                return getCurrentSong();
            default:
                return null;
        }
    }

    public Song previous() {
        int n = (int) songs.getNItems();
        if (n == 0) return null;

        switch (repeatMode) {
            case NONE:
                if (currentIndex > 0) {
                    setCurrentIndex(currentIndex - 1);
                }
                return songs.getItem(currentIndex);
            case ALL:
                setCurrentIndex((currentIndex - 1 + n) % n);
                return songs.getItem(currentIndex);
            case ONE:
                return getCurrentSong();
            default:
                return null;
        }
    }
}
