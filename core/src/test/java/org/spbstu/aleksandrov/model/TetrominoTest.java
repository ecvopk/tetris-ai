package org.spbstu.aleksandrov.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import org.spbstu.aleksandrov.model.Tetromino.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.*;

public class TetrominoTest {

    private static final List<Tetromino> tetrominoes = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        for (Tetromino.Type type : Tetromino.Type.values()) {
            tetrominoes.add(new Tetromino(type));
        }
    }

    @Test
    public void rotateTest() {
        for (Tetromino t : tetrominoes) {
            Coordinate cBefore = t.getRotationPoint().deepClone();
            t.rotate(ROT_L);
            Coordinate after = t.getRotationPoint();
            assertEquals(cBefore.getX(), after.getX());
            assertEquals(cBefore.getY(), after.getY());
            assertEquals(3, t.getState());
            t.rotate(ROT_R);
            assertEquals(0, t.getState());
            t.rotate(ROT_R, 4);
            assertEquals(0, t.getState());
        }
    }

    @Test
    public void areCoordinatesPossibleTest() {
        for (Tetromino t : tetrominoes) {
            assertTrue(t.areCoordinatesPossible());
            t.move(-10, 0);
            assertFalse(t.areCoordinatesPossible());
        }
    }

    @Test
    public void placeTest() {
        for (Tetromino t : tetrominoes) {
            t.place(1, 0);
            assertEquals(1, t.getRotationPoint().getX());
            assertEquals(0, t.getRotationPoint().getY());
        }
    }
}
