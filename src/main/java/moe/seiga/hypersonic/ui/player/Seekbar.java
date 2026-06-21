package moe.seiga.hypersonic.ui.player;

import org.gnome.gtk.Box;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.gnome.gtk.Scale;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/player/seekbar.ui", name = "Seekbar")
public class Seekbar extends Box {

    @GtkChild
    public Scale seekbar;

    @GtkChild
    public Label elapsed;

    @GtkChild
    public Label duration;

    @GtkChild(name = "quality_grade")
    public Image qualityGrade;

    @GtkChild(name = "format_desc")
    public Label formatDesc;

    @GtkChild
    public Label bitrate;

    public Seekbar() {
        super();
    }

    public Seekbar(MemorySegment address) {
        super(address);
    }
}
