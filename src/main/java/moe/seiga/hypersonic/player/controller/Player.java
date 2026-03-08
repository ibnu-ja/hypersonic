package moe.seiga.hypersonic.player.controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.service.audio.GstBackend;
import org.freedesktop.gstreamer.gst.State;
import org.gnome.gio.ListStore;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@Slf4j
@RegisteredType(name = "Player")
public class Player extends GObject {
    private final GstBackend backend;
    @Getter
    private final ListStore<Song> queue;
    @Getter
    private PlaybackState state = PlaybackState.STOPPED;
    @Getter
    private Song currentSong;
    @Getter
    private double position;

    public Player() {
        this.backend = new GstBackend();
        this.queue = new ListStore<>();
        setupBackendListeners();
    }

    private void setupBackendListeners() {
        backend.onNotify("state", pspec -> {
            updateStateFromBackend();
        });

        backend.connect("eos", (GstBackend.EosSignal) () -> {
            log.info("Song ended");
        });
    }

    private void updateStateFromBackend() {
        State gstState = backend.getState();

        PlaybackState newState = switch (gstState) {
            case PLAYING -> PlaybackState.PLAYING;
            case PAUSED -> PlaybackState.PAUSED;
            case NULL, READY -> PlaybackState.STOPPED;
            default -> this.state;
        };

        if (this.state != newState) {
            log.info("Player state changed: {} -> {}", this.state, newState);
            this.state = newState;
            notify("state");
        }
    }

    @SuppressWarnings("unused")
    public void setState(PlaybackState value) {}

    @SuppressWarnings("unused")
    public void setCurrentSong(Song value) {}

    public void setPosition(double value) {
        if (this.position != value) {
            this.position = value;
            notify("position");
        }
    }

    public void togglePlayback() {
        log.info("Toggle playback, current: {}", state);

        switch (state) {
            case STOPPED -> {
                if (currentSong != null) {
                    backend.setUrl(currentSong.getUrl());
                    log.info("url set: {}", currentSong.getUrl());
                    backend.play();
                } else if (queue.getNItems() > 0) {
                    playSongAt(0);
                } else {
                    log.info("No song to play");
                }
            }
            case PLAYING -> backend.pause();
            case PAUSED -> backend.play();
        }
    }

    public void playSong(Song song) {
        this.currentSong = song;
        backend.setUrl(song.getUrl());
        backend.play();
        notify("current-song");
        log.debug("Playing: {}", song.getTitle());
    }

    private void playSongAt(int index) {
        if (index >= 0 && index < queue.getNItems()) {
            Song song = queue.getItem(index);
            this.currentSong = song;
            backend.setUrl(song.getUrl());
            backend.play();
            notify("current-song");
        }
    }

    @Override
    public void dispose() {
        backend.stop();
        super.dispose();
    }
}
