package org.oosd.network;

import java.io.IOException;

/*
thrown when nothing is listening at the server address, i.e. TetrisServer
hasn't been started, kept separate from other IOExceptions so the player
panel can show a specific "start the server" warning instead of a generic error
*/
public class ServerUnavailableException extends IOException {

    //exceptions are Serializable, this pins the format so the compiler doesn't warn
    private static final long serialVersionUID = 1L;

    private final String address;

    public ServerUnavailableException(String address, Throwable cause) {
        super("TetrisServer is not running on " + address, cause);
        this.address = address;
    }

    public String address() {
        return address;
    }
}
