package moe.seiga.hypersonic.player;

import org.gnome.gtk.Box;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/components/player/playback-controls.ui", name = "PlaybackControls")
public class PlaybackControls extends Box {

    public PlaybackControls() {
        super();
    }

    public PlaybackControls(MemorySegment address) {
        super(address);
    }

}
