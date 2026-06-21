package moe.seiga.hypersonic.playback;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.service.audio.GstBackend;
import org.freedesktop.gstreamer.gst.State;
import org.gnome.gobject.GObject;
import org.javagi.gobject.annotations.RegisteredType;

@Slf4j
@RegisteredType(name = "PlaybackViewModel")
public class PlaybackViewModel extends GObject {

    @Getter
    private final GstBackend backend;

    @Getter
    private final Queue queue;

    @Getter
    private final PlayerViewModel playerViewModel;

    public PlaybackViewModel() {
        this.backend = new GstBackend();
        this.queue = new Queue();
        this.playerViewModel = new PlayerViewModel();
        setupListeners();
    }

    private void setupListeners() {
        // Backend state notify -> playerState state update
        backend.onNotify("state", pspec -> {
            updateStateFromBackend();
        });

        // Backend eos signal -> onEos
        backend.connect("eos", (GstBackend.EosSignal) this::onEos);

        // Backend position-nanos notify -> playerState position update
        backend.onNotify("position-nanos", pspec -> {
            playerViewModel.setPositionNanos(backend.getPositionNanos());
        });

        // Backend duration-nanos notify -> playerState duration update
        backend.onNotify("duration-nanos", pspec -> {
            playerViewModel.setDurationNanos(backend.getDurationNanos());
        });
    }

    private void updateStateFromBackend() {
        State gstState = backend.getState();
        PlaybackState newState = switch (gstState) {
            case PLAYING -> PlaybackState.PLAYING;
            case PAUSED -> PlaybackState.PAUSED;
            case NULL, READY -> PlaybackState.STOPPED;
            default -> playerViewModel.getState();
        };
        playerViewModel.setState(newState);
    }

    private void onEos() {
        log.info("EOS reached, playing next");
        Song next = queue.next();
        if (next != null) {
            backend.setUrl(next.getUrl());
            backend.play();
            playerViewModel.setCurrentSong(next);
        } else {
            backend.stop();
            playerViewModel.setState(PlaybackState.STOPPED);
            playerViewModel.setCurrentSong(null);
        }
    }

    public void togglePlayback() {
        log.info("Toggle playback, current state: {}", playerViewModel.getState());
        switch (playerViewModel.getState()) {
            case STOPPED -> {
                Song current = queue.getCurrentSong();
                if (current != null) {
                    backend.setUrl(current.getUrl());
                    backend.play();
                    playerViewModel.setCurrentSong(current);
                } else if (queue.getNSongs() > 0) {
                    Song first = queue.playIndex(0);
                    if (first != null) {
                        backend.setUrl(first.getUrl());
                        backend.play();
                        playerViewModel.setCurrentSong(first);
                    }
                } else {
                    log.info("Queue is empty");
                }
            }
            case PLAYING -> backend.pause();
            case PAUSED -> backend.play();
        }
    }

    public void playSong(Song song) {
        queue.clear();
        queue.addSong(song);
        backend.setUrl(song.getUrl());
        backend.play();
        playerViewModel.setCurrentSong(song);
        log.debug("Playing song: {}", song.getTitle());
    }

    public void next() {
        Song next = queue.next();
        if (next != null) {
            backend.setUrl(next.getUrl());
            backend.play();
            playerViewModel.setCurrentSong(next);
        }
    }

    public void previous() {
        Song prev = queue.previous();
        if (prev != null) {
            backend.setUrl(prev.getUrl());
            backend.play();
            playerViewModel.setCurrentSong(prev);
        }
    }

    @Override
    public void dispose() {
        backend.stop();
        super.dispose();
    }
}
