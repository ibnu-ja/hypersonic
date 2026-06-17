package moe.seiga.hypersonic.service.api;

import org.javagi.gobject.annotations.RegisteredType;

@RegisteredType(name = "ConnectionState")
public enum ConnectionState {
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    INVALID_CREDENTIALS,
    SERVER_UNREACHABLE,
    SSL_ERROR
}