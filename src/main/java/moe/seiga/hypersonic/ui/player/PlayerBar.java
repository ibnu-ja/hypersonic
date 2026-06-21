package moe.seiga.hypersonic.ui.player;

import lombok.extern.slf4j.Slf4j;
import moe.seiga.hypersonic.playback.PlaybackViewModel;
import moe.seiga.hypersonic.playback.Song;
import moe.seiga.hypersonic.playback.PlayerViewModel;
import org.gnome.gobject.ParamSpec;
import org.gnome.gtk.Box;
import org.gnome.gtk.Label;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.lang.foreign.MemorySegment;

@Slf4j
@GtkTemplate(ui = "/moe/seiga/Hypersonic/player/player-bar.ui", name = "PlayerBar")
public class PlayerBar extends Box {

    @GtkChild(name = "playback_controls")
    public PlaybackControls playbackControls;

    @GtkChild(name = "seekbar")
    public Seekbar seekbar;

    @GtkChild(name = "song_name")
    public Label songName;

    @GtkChild
    public Label artist;

    @GtkChild
    public Label album;

    public PlayerBar() {
        super();
    }

    public PlayerBar(MemorySegment address) {
        super(address);
    }

    public void setup(PlaybackViewModel vm) {
        PlayerViewModel ps = vm.getPlayerViewModel();

        ps.onNotify("current-song", (ParamSpec _) -> {
            Song song = ps.getCurrentSong();
            if (song != null) {
                artist.setText(song.getArtist());
                songName.setText(song.getTitle());
                // if (song.getCoverArt() != null) {
                //     String coverArtUrl = ConnectionState.INSTANCE.getApi().getCoverArtUrl(song.getCoverArt(), 60, true);
                //     loadImageFromUrl(coverArtUrl);
                //     playingImage.setVisible(true);
                // }
            } else {
                songName.setText("No Song Playing");
                artist.setText("No artst");
                // currentSongInfo.setVisible(false);
                // currentSongInfo.setUseMarkup(false);
                // playingImage.setVisible(false);
            }
        });

        playbackControls.setup(vm);

    }
}
