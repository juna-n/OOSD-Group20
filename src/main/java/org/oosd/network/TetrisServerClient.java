package org.oosd.network;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/*
talks to TetrisServer over a TCP socket
protocol (from the server and the course connection guide):
  1. open a new connection to localhost:3000
  2. send one line: the PureGame as JSON
  3. read one line back: the OpMove as JSON
  4. the server closes the connection, so every request uses a fresh socket

blocking calls, so this must only ever be used from a background thread,
timeouts make sure a stuck server can't hold that thread forever
*/
public class TetrisServerClient {

    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_PORT = 3000;

    //localhost answers in well under a millisecond, these only matter when something is wrong
    private static final int CONNECT_TIMEOUT_MS = 500;
    private static final int READ_TIMEOUT_MS = 2000;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final String host;
    private final int port;

    public TetrisServerClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public TetrisServerClient(String host, int port) {
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;
    }

    public String address() {
        return host + ":" + port;
    }

    /*
    sends the game state and waits for the server's move
    throws ServerUnavailableException if the server isn't running, and a
    plain IOException if it was reached but answered badly or too slowly
    */
    public OpMove requestMove(PureGame game) throws IOException {
        String request = MAPPER.writeValueAsString(game);

        try (Socket socket = connect()) {
            socket.setSoTimeout(READ_TIMEOUT_MS);

            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            out.println(request);
            //PrintWriter hides write errors, so ask it explicitly
            if (out.checkError()) {
                throw new IOException("Could not send the game state to TetrisServer");
            }

            String response = in.readLine();
            if (response == null || response.isBlank()) {
                throw new IOException("TetrisServer closed the connection without answering");
            }
            return MAPPER.readValue(response, OpMove.class);
        }
    }

    private Socket connect() throws ServerUnavailableException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            return socket;
        } catch (IOException e) {
            //connection refused, or on Windows a timeout, both mean nobody is listening
            try {
                socket.close();
            } catch (IOException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw new ServerUnavailableException(address(), e);
        }
    }
}
