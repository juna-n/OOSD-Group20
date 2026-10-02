package org.oosd.network;

import org.oosd.model.BoardSnapshot;
import org.oosd.model.Tetromino;

/*
the request sent to TetrisServer, field names must match the server's own
PureGame class exactly because they become the JSON keys:
  {"width":10,"height":20,"cells":[[0,0,...],...],"currentShape":[[0,1,0],[1,1,1]],"nextShape":[[1,1,1,1]]}
cells is 0 for empty and 1 for filled, row 0 at the top, the falling piece is not included
shapes are tight grids with no empty border, the server rotates them itself
*/
public record PureGame(int width, int height, int[][] cells, int[][] currentShape, int[][] nextShape) {

    public static PureGame from(BoardSnapshot snapshot) {
        Tetromino current = snapshot.current();
        //the next piece is described in the orientation it will spawn in
        Tetromino next = Tetromino.spawn(snapshot.next(), snapshot.width());
        return new PureGame(
                snapshot.width(),
                snapshot.height(),
                snapshot.occupancy(),
                current.shapeMatrix(),
                next.shapeMatrix());
    }
}
