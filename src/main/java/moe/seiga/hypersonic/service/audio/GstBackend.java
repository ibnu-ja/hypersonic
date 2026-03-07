package moe.seiga.hypersonic.service.audio;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.gstreamer.gst.*;
import org.gnome.glib.GError;
import org.gnome.glib.GLib;
import org.gnome.gobject.GObject;
import org.javagi.base.Out;
import org.javagi.gobject.annotations.Property;
import org.javagi.gobject.annotations.RegisteredType;
import org.javagi.gobject.annotations.Signal;

import java.util.Set;

@Slf4j
@SuppressWarnings("unused")
@RegisteredType(name = "GstBackend")
public class GstBackend extends GObject {

    private final Element playbin;
    @Getter
    private State state = State.NULL;
    @Getter
    private String url;

    @Signal(name = "eos")
    public interface EosSignal {
        void run();
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

        playbin.set("flags", 0x0002); // GST_PLAY_FLAG_AUDIO

        setupBus();
    }

    public void setUrl(String streamUrl) {
        this.url = streamUrl;
        if (playbin != null) {
            playbin.set("uri", streamUrl);
        }
        notify("url");
    }

    @Property(name = "state")
    public int getStateValue() {
        return state.getValue(); //GObject int
    }

    @Property(name = "state")
    public void setStateValue(int value) {
    }

    public void setState(State state) {
        if (playbin != null) {
            playbin.setState(state);
        }
        //update state in setupBus()
    }

    public void play() {
        setState(State.PLAYING);
    }

    public void pause() {
        setState(State.PAUSED);
    }

    public void stop() {
        setState(State.NULL);
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
                //TODO Implement next track logic here
                emit("eos");
                GLib.idleAdd(GLib.PRIORITY_DEFAULT_IDLE, () -> false);
            }
            else if (msgTypes.contains(MessageType.ERROR)) {
                Out<GError> errorOut = new Out<>();
                Out<String> debugOut = new Out<>();
                msg.parseError(errorOut, debugOut);
                String errorMsg = errorOut.get() != null ? errorOut.get().readMessage() : "Unknown Error";
                log.error("GStreamer Error: {} - Debug: {}", errorMsg, debugOut.get());
                GLib.idleAdd(GLib.PRIORITY_DEFAULT_IDLE, () -> false);
            }
            else if (msgTypes.contains(MessageType.STATE_CHANGED)) {
                // Ensure the message comes from playbin, not a child element
                if (msg.readSrc().equals(playbin)) {
                    Out<State> oldState = new Out<>();
                    Out<State> newState = new Out<>();
                    Out<State> pendingState = new Out<>();
                    msg.parseStateChanged(oldState, newState, pendingState);

                    State current = newState.get();
                    if (this.state != current) {
                        log.debug("State changed: {} -> {}", this.state, current);
                        this.state = current;
                        notify("state");
                    }
                }
            }
        });
    }
}
