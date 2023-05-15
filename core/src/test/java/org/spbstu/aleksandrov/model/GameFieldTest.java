package org.spbstu.aleksandrov.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.*;
import static org.spbstu.aleksandrov.model.Tetromino.Type.*;

public class GameFieldTest {

    @Test
    public void stackTetrominoAndAreCellsEmptyTest() {
        GameField field = new GameField();
        Tetromino t = new Tetromino(Tetromino.Type.T);
        field.stackTetromino(t);
        assertFalse(field.areCellsEmpty(t.getCoordinates()));
        t.move(RIGHT);
        assertFalse(field.areCellsEmpty(t.getCoordinates()));
        t.move(RIGHT);
        assertFalse(field.areCellsEmpty(t.getCoordinates()));
        t.move(RIGHT);
        assertTrue(field.areCellsEmpty(t.getCoordinates()));
    }

    @Test
    public void rotateOnFieldTest() {

        // Example source: https://tetris.fandom.com/wiki/SRS#:~:text=A%20wall%20kick%20example%3A
        // * - stacked blocks, x - tetromino to rotate, r - rotation point of figure
        // 9 | | | | |*|*| |
        // 8 | | | |x| |r|*|
        // 7 | | | |x|r|x| |
        // 6 | | |*|*| | | |
        // 5 | |*|r| | | | |
        // 4 | | | |*| | | |
        // 3 | | |*|r|*| | |
        //    0 1 2 3 4 5 6
        // After ROT_L due to wall kick
        // 9 | | | | |*|*| |
        // 8 | | | | | |r|*|
        // 7 | | | | | | | |
        // 6 | | |*|*| |x| |
        // 5 | |*|r| | |r| |
        // 4 | | | |*|x|x| |
        // 3 | | |*|r|*| | |
        //    0 1 2 3 4 5 6

        GameField field = new GameField();
        Tetromino z = new Tetromino(Z);
        Tetromino s = new Tetromino(S);
        Tetromino t = new Tetromino(T);
        Tetromino j = new Tetromino(J);

        z.place(5, 8);
        s.place(2, 5);
        t.place(3, 3);
        j.place(4, 7);

        field.stackTetromino(z);
        field.stackTetromino(s);
        field.stackTetromino(t);

        field.rotateOnField(ROT_L, j);

        assertEquals(5, j.getRotationPoint().getX());
        assertEquals(5, j.getRotationPoint().getY());
        assertTrue(field.areCellsEmpty(j.getCoordinates()));
    }

    @Test
    public void checkLinesToClearAndCleanLinesTest() {

        // t, s, j - types of tetromino, r - rotation point
        // 7 | | |s| | | | | | | |
        // 6 | |t|r|s| |t| |j| | |
        // 5 |t|r|t|s|t|r|t|j|r|j|
        //    0 1 2 3 4 5 6 7 8 9

        GameField field = new GameField();

        Tetromino t = new Tetromino(T);
        Tetromino s = new Tetromino(S);
        Tetromino j = new Tetromino(J);

        t.place(1, 5);
        field.stackTetromino(t);
        s.rotate(ROT_R);
        s.place(2, 6);
        field.stackTetromino(s);
        t.place(5,5);
        field.stackTetromino(t);
        j.place(8,5);
        field.stackTetromino(j);

        assertEquals(1, field.checkLinesToClear(j.getCoordinates()));

        field.cleanLines();

        // After field.cleanLines();
        // 6 | | |s| | | | | | | |
        // 5 | |t|r|s| |t| |j| | |
        //    0 1 2 3 4 5 6 7 8 9

        assertEquals(0, field.checkLinesToClear(j.getCoordinates()));

        // 7 | |z| |s| |s| |s|l|l|
        // 6 |z|r|s|r|s|r|s|r|s|r|
        // 5 |z|t|r|s|s|t|s|j|s|l|
        //    0 1 2 3 4 5 6 7 8 9

        s.place(5, 6);
        field.stackTetromino(s);
        s.place(3, 6);
        field.stackTetromino(s);
        s.place(7, 6);
        field.stackTetromino(s);
        Tetromino z = new Tetromino(Z);
        z.rotate(ROT_L);
        z.place(1, 6);
        field.stackTetromino(z);
        Tetromino l = new Tetromino(L);
        l.rotate(ROT_L);
        l.place(9,6);
        field.stackTetromino(l);
        assertEquals(2, field.checkLinesToClear(l.getCoordinates()));
    }
}
