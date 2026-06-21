package moe.seiga.hypersonic.service.audio;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.gstreamer.gst.*;
import org.gnome.glib.GError;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.javagi.base.Out;
import org.javagi.gobject.annotations.RegisteredType;
import org.javagi.gobject.annotations.Signal;

import java.util.Set;

@Slf4j
@RegisteredType(name = "GstBackend")
public class GstBackend extends GObject {

    private final Element playbin;
    @Getter
    private State state = State.NULL;

    @Getter
    private String url;

    @Getter
    private long positionNanos = 0;

    @Getter
    private long durationNanos = 0;

    private double volume = 1.0;

    private boolean isTimerRunning = false;

    @Signal(name = "eos")
    public interface EosSignal {
        void run();
    }

    @Signal(name = "position-updated")
    public interface PositionUpdatedSignal {
        void run(long nanos);
    }

    @Signal(name = "duration-changed")
    public interface DurationChangedSignal {
        void run(long nanos);
    }

    @Signal(name = "state-changed")
    public interface StateChangedSignal {
        void run(int stateOrdinal);
    }

    @Signal(name = "error")
    public interface ErrorSignal {
        void run(String message);
    }

    public GstBackend() {
        playbin = ElementFactory.make("playbin", "audio-player");
        if (playbin == null) {
            throw new IllegalStateException("Failed to create playbin element");
        }

        Element fakesink = ElementFactory.make("fakesink", "video-fakesink");
        if (fakesink != null) {
            playbin.set("video-sink", fakesink);
        }

        playbin.set("flags", 0x0002);

        setupBus();
    }

    public void setUrl(String streamUrl) {
        this.url = streamUrl;
        if (playbin != null) {
            playbin.set("uri", streamUrl);
        }
        notify("url");
    }

    @SuppressWarnings("unused")
    public void setState(State value) {}

    public void play() {
        if (playbin != null) {
            playbin.setState(State.PLAYING);
        }
    }

    public void pause() {
        if (playbin != null) {
            playbin.setState(State.PAUSED);
        }
    }

    public void stop() {
        if (playbin != null) {
            playbin.setState(State.NULL);
        }
    }

    private void setupBus() {
        Bus bus = playbin.getBus();
        if (bus == null) {
            log.error("Failed to get bus from playbin");
            return;
        }

        bus.addSignalWatch();

        bus.onMessage(null, (Message msg) -> {
            if (msg == null) return;

            Set<MessageType> msgTypes = msg.readType();

            if (msgTypes.contains(MessageType.EOS)) {
                log.debug("EOS received");
                emit("eos");
                GLib.idleAdd(GLib.PRIORITY_DEFAULT_IDLE, () -> false);
            }
            else if (msgTypes.contains(MessageType.ERROR)) {
                Out<GError> errorOut = new Out<>();
                Out<String> debugOut = new Out<>();
                msg.parseError(errorOut, debugOut);
                String errorMsg = errorOut.get() != null ? errorOut.get().readMessage() : "Unknown Error";
                log.error("GStreamer Error: {} - Debug: {}", errorMsg, debugOut.get());
                emit("error", errorMsg);
                GLib.idleAdd(GLib.PRIORITY_DEFAULT_IDLE, () -> false);
            }
            else if (msgTypes.contains(MessageType.STATE_CHANGED)) {
                if (msg.readSrc().equals(playbin)) {
                    Out<State> oldState = new Out<>();
                    Out<State> newState = new Out<>();
                    Out<State> pendingState = new Out<>();
                    msg.parseStateChanged(oldState, newState, pendingState);

                    State current = newState.get();
                    if (this.state != current) {
                        log.debug("Backend state changed: {} -> {}", this.state, current);
                        this.state = current;
                        notify("state");
                        assert current != null;
                        emit("state-changed", current.ordinal());

                        if (current == State.PLAYING && !isTimerRunning) {
                            isTimerRunning = true;
                            GLib.timeoutAdd(GLib.PRIORITY_DEFAULT, 250, (org.gnome.glib.SourceFunc) this::pollPosition);
                        }
                    }
                }
            }
            else if (msgTypes.contains(MessageType.DURATION_CHANGED)) {
                Out<Long> durOut = new Out<>();
                if (playbin.queryDuration(Format.TIME, durOut)) {
                    long dur = durOut.get();
                    if (this.durationNanos != dur) {
                        this.durationNanos = dur;
                        notify("duration-nanos");
                        emit("duration-changed", dur);
                    }
                }
            }
        });
    }

    private boolean pollPosition() {
        if (this.state != State.PLAYING) {
            this.isTimerRunning = false;
            return false;
        }
        Out<Long> posOut = new Out<>();
        if (playbin.queryPosition(Format.TIME, posOut)) {
            long pos = posOut.get();
            if (this.positionNanos != pos) {
                this.positionNanos = pos;
                notify("position-nanos");
                emit("position-updated", pos);
            }
        }
        return true;
    }

    public void seek(long nanos) {
        if (playbin != null) {
            playbin.seekSimple(Format.TIME, java.util.EnumSet.of(SeekFlags.FLUSH, SeekFlags.KEY_UNIT), nanos);
        }
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double value) {
        if (this.volume != value) {
            this.volume = value;
            if (playbin != null) {
                playbin.set("volume", value);
            }
            notify("volume");
        }
    }

    @Override
    public void dispose() {
        if (playbin != null) {
            playbin.setState(State.NULL);
        }
        super.dispose();
    }
}
