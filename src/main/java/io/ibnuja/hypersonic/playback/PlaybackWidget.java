package io.ibnuja.hypersonic.playback;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.gstreamer.gst.Bus;
import org.freedesktop.gstreamer.gst.Message;
import org.freedesktop.gstreamer.gst.MessageType;
import org.gnome.gtk.Box;
import org.gnome.gtk.Label;
import org.gnome.gtk.Scale;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;
import java.util.Set;

@SuppressWarnings("java:S110")
@EqualsAndHashCode(callSuper = true)
@Slf4j
@GtkTemplate(name = "PlaybackWidget", ui = "/io/ibnuja/Hypersonic/components/playback/playback_widget.ui")
public class PlaybackWidget extends Box {

    @GtkChild
    public ControlsWidget controls;

    @GtkChild(name = "now_playing")
    public InfoWidget nowPlaying;

    @GtkChild(name = "seek_bar")
    public Scale seekBar;

    @GtkChild(name = "track_duration")
    public Label trackDuration;

    @GtkChild(name = "track_position")
    public Label trackPosition;

    @GtkChild(name = "volume_slider")
    public Scale volumeSlider;

    public PlaybackWidget(MemorySegment address) {
        super(address);
    }

    public void setup(PlayerState vm) {
        controls.setup(vm);
        nowPlaying.setup(vm);
    }
}
