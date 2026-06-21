package moe.seiga.hypersonic.ui.player;

import moe.seiga.hypersonic.playback.PlaybackState;
import moe.seiga.hypersonic.playback.PlaybackViewModel;
import moe.seiga.hypersonic.playback.PlayerViewModel;
import org.gnome.gtk.Box;
import org.gnome.gtk.Button;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;

@GtkTemplate(ui = "/moe/seiga/Hypersonic/player/playback-controls.ui", name = "PlaybackControls")
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

    public void setup(PlaybackViewModel vm) {
        PlayerViewModel ps = vm.getPlayerViewModel();

        // State changes → update button
        ps.onNotify("state", pspec -> {
            assert pspec != null;
            updatePlayPauseButton(ps.getState());
        });

        // Button click → action method
        playPauseButton.onClicked(vm::togglePlayback);

        nextButton.onClicked(vm::next);

        previousButton.onClicked(vm::previous);

        updatePlayPauseButton(ps.getState());
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
