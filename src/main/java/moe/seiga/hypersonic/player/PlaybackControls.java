package moe.seiga.hypersonic.player;

import moe.seiga.hypersonic.player.controller.PlaybackState;
import moe.seiga.hypersonic.player.controller.Player;
import org.gnome.glib.Type;
import org.gnome.gobject.ParamSpec;
import org.gnome.gtk.Box;
import org.gnome.gtk.Button;
import org.javagi.gtk.annotations.GtkChild;
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

    @GtkChild(name = "prev_btn")
    public Button previousButton;

    @GtkChild(name = "play_pause_btn")
    public Button playPauseButton;

    @GtkChild(name = "next_btn")
    public Button nextButton;

    public void setup(Player vm) {
        // State changes → update button
        vm.onNotify("state", pspec -> {
            assert pspec != null;
            PlaybackState state = getStateFromParamSpec(vm, pspec);
            updatePlayPauseButton(state);
        });

        // Button click → action method
        playPauseButton.onClicked(() -> {
            vm.togglePlayback();
        });

        nextButton.onClicked(() -> {
            // TODO: implement next
        });

        previousButton.onClicked(() -> {
            // TODO: implement previous
        });
        updatePlayPauseButton(vm.getState());
    }

    private PlaybackState getStateFromParamSpec(Player player, ParamSpec pspec) {
        String propertyName = pspec.getName();
        var value = player.getProperty(propertyName);
        return (PlaybackState) value;
    }

    private void updatePlayPauseButton(PlaybackState state) {
        switch (state) {
            case STOPPED, PAUSED -> {
                playPauseButton.setIconName("media-playback-start-symbolic");
                playPauseButton.setTooltipText("Play");
            }
            case PLAYING -> {
                playPauseButton.setIconName("media-playback-pause-symbolic");
                playPauseButton.setTooltipText("Pause");
            }
        }
    }
}
