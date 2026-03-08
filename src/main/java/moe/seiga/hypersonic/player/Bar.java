package moe.seiga.hypersonic.player;

import org.gnome.gtk.Box;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/components/player/bar.ui", name = "PlayerBar")
public class Bar extends Box {

    @GtkChild(name = "playback_controls")
    public PlaybackControls playbackControls;

    @GtkChild(name = "seekbar")
    public Seekbar seekbar;

    public Bar() {
        playbackControls = new PlaybackControls();
        seekbar = new Seekbar();
    }

    public Bar(MemorySegment address) {
        super(address);
    }

}
