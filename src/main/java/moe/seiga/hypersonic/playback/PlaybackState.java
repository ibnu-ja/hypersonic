package moe.seiga.hypersonic.playback;

import org.javagi.gobject.annotations.RegisteredType;

@RegisteredType(name = "PlaybackState")
public enum PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
}
