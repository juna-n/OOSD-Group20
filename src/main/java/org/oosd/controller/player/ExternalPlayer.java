package org.oosd.controller.player;

import org.oosd.model.BoardSnapshot;
import org.oosd.model.Move;
import org.oosd.model.PlayerType;
import org.oosd.network.OpMove;
import org.oosd.network.PureGame;
import org.oosd.network.ServerUnavailableException;
import org.oosd.network.TetrisServerClient;

import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/*
a player controlled by TetrisServer
same threading and move execution as the AI (AutomatedPlayer), only the
decision is made by asking the server over the network instead

server not running: decide() throws, the warning appears in the side panel,
the piece just falls on its own (no control), and AutomatedPlayer asks
again every second, so starting the server mid-game takes over within a second
*/
public final class ExternalPlayer extends AutomatedPlayer {

    private final TetrisServerClient client;

    public ExternalPlayer(ExecutorService worker, Executor uiExecutor, TetrisServerClient client) {
        super(worker, uiExecutor);
        this.client = Objects.requireNonNull(client, "client");
    }

    @Override
    public PlayerType type() {
        return PlayerType.EXTERNAL;
    }

    //worker thread: blocking network call
    @Override
    protected Optional<Move> decide(BoardSnapshot snapshot) throws Exception {
        OpMove answer = client.requestMove(PureGame.from(snapshot));
        return answer.toMove(snapshot.current().rotation());
    }

    @Override
    protected String describeFailure(Exception failure) {
        if (failure instanceof ServerUnavailableException unavailable) {
            return "No connection to TetrisServer on " + unavailable.address()
                    + ". Start it with: java -jar TetrisServer.jar";
        }
        if (failure instanceof SocketTimeoutException) {
            return "TetrisServer is not responding";
        }
        return "TetrisServer error: " + super.describeFailure(failure);
    }
}
