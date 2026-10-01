package org.oosd.model;

//everything a GameState can announce to its listeners
public enum GameEvent {
    PIECE_MOVED,     //moved left or right by a player
    PIECE_ROTATED,
    PIECE_LOCKED,    //landed and became part of the board
    LINES_CLEARED,   //GameState.lastClear() says how many
    LEVEL_UP,
    PIECE_SPAWNED,   //a new piece entered the field, AI players start thinking here
    PAUSE_CHANGED,
    GAME_OVER
}
