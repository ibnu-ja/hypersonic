package io.ibnuja.hypersonic.playback;

import io.ibnuja.hypersonic.model.Song;
import io.ibnuja.hypersonic.service.api.ConnectionState;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.gnome.gdk.Texture;
import org.gnome.gdkpixbuf.PixbufLoader;
import org.gnome.glib.GLib;
import org.gnome.gobject.ParamSpec;
import org.gnome.gtk.Button;
import org.gnome.gtk.Image;
import org.gnome.gtk.Label;
import org.javagi.gobject.annotations.InstanceInit;
import org.javagi.gtk.annotations.GtkChild;
import org.javagi.gtk.annotations.GtkTemplate;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SuppressWarnings("java:S110")
@EqualsAndHashCode(callSuper = true)
@GtkTemplate(name = "PlaybackInfoWidget", ui = "/io/ibnuja/Hypersonic/components/playback/playback_info.ui")
public class InfoWidget extends Button {

    @GtkChild(name = "current_song_info")
    public Label currentSongInfo;

    @GtkChild(name = "playing_image")
    public Image playingImage;

    @SuppressWarnings("unused")
    public InfoWidget(MemorySegment address) {
        super(address);
    }

    @InstanceInit
    public void init() {
        playingImage.setVisible(false);
    }

    public void setup(PlayerState vm) {
        vm.onNotify("current-song", (ParamSpec _) -> {
            Song song = vm.getCurrentSong();
            if (song != null) {
                currentSongInfo.setUseMarkup(true);
                String markup = "<b>" + escape(song.getTitle()) + "</b>\n" + escape(song.getArtist());
                currentSongInfo.setUseMarkup(true);
                currentSongInfo.setLabel(markup);
                currentSongInfo.setVisible(true);
                if (song.getCoverArt() != null) {
                    String coverArtUrl = ConnectionState.INSTANCE.getApi().getCoverArtUrl(song.getCoverArt(), 60, true);
                    loadImageFromUrl(coverArtUrl);
                    playingImage.setVisible(true);
                }
            } else {
                currentSongInfo.setVisible(false);
                currentSongInfo.setUseMarkup(false);
                playingImage.setVisible(false);
            }
        });
    }

    private String escape(String text) {
        return text == null ? "" : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void loadImageFromUrl(String url) {
        CompletableFuture.runAsync(() -> {
            try {
                byte[] imageData = downloadImage(url);
                var loader = new PixbufLoader();
                loader.write(imageData);
                loader.close();
                var pixbuf = loader.getPixbuf();
                GLib.idleAdd(GLib.PRIORITY_DEFAULT_IDLE, () -> {
                    //FIXME remove deprecation
                    playingImage.setFromPaintable(Texture.forPixbuf(pixbuf));
                    playingImage.setPixelSize(60);
                    return false;
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while loading cover art", e);
            } catch (Exception e) {
                log.error("Failed to load cover art", e);
            }
        });
    }

    private byte[] downloadImage(String url) throws InterruptedException, IOException {
        try (var client = HttpClient.newHttpClient()) {
            HttpResponse<byte[]> response;
            var request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .build();
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            return response.body();
        }
    }
}
