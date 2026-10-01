package org.oosd.network;

import org.oosd.model.Move;

import java.util.Optional;

/*
TetrisServer's answer, e.g. {"opX":3,"opRotate":1}
opRotate - clockwise turns to apply to the shape exactly as we sent it
opX      - left edge of the rotated shape's tight grid, which is the same
           thing as Tetromino.minCol() in our model
the server answers -1, -1 when it can't find anywhere to put the piece
*/
public record OpMove(int opX, int opRotate) {

    public boolean hasMove() {
        return opX >= 0 && opRotate >= 0;
    }

    /*
    converts to our own Move, rotations are counted from the orientation the
    piece had when the request was sent, so they're added to that rotation
    */
    public Optional<Move> toMove(int rotationWhenSent) {
        if (!hasMove()) {
            return Optional.empty();
        }
        return Optional.of(new Move(rotationWhenSent + opRotate, opX));
    }
}
