package moe.seiga.hypersonic.player.controller;

import org.javagi.gobject.annotations.RegisteredType;

@RegisteredType(name = "PlaybackState")
public enum PlaybackState {
    STOPPED,
    PLAYING,
    PAUSED
}
