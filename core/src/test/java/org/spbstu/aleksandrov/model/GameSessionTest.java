package org.spbstu.aleksandrov.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameSessionTest {

    @Test
    public void generateNewTetrominoTest() {
        GameSession game = new GameSession(0);
        Tetromino.Type[] bucket = game.getBucket().clone();
        for (Tetromino.Type type : bucket) {
            assertEquals(type, game.getFallingTetromino().getType());
            game.generateNewTetromino();
        }
    }

    @Test
    public void updateFallingProjectionTest() {
        GameSession game = new GameSession(0);

        int x = game.getFallingProjection().getRotationPoint().getX();
        int y = game.getFallingProjection().getRotationPoint().getY();

        game.getFallingTetromino().move(Tetromino.Movement.RIGHT);
        game.updateFallingProjection();
        assertEquals(x + 1, game.getFallingProjection().getRotationPoint().getX());
        assertEquals(y, game.getFallingProjection().getRotationPoint().getY());

        game.getFallingTetromino().move(0, -5);
        assertEquals(y, game.getFallingProjection().getRotationPoint().getY());
        assertEquals(x + 1, game.getFallingProjection().getRotationPoint().getX());
    }

    @Test
    public void stepTest() {
        GameSession game = new GameSession(0);
        int y = game.getFallingTetromino().getRotationPoint().getY();
        Tetromino.Type nextType = game.getBucket()[1];
        game.step();
        assertEquals(y - 1, game.getFallingTetromino().getRotationPoint().getY());

        // Tetromino will reach floor after 17 times game.step() and will be stacked after the 18th game.step(),
        // a new tetromino will be generated
        for (int i = 0; i < 18; i++) game.step();
        assertEquals(nextType, game.getFallingTetromino().getType());
    }

    @Test
    public void hardDropTest() {
        GameSession game = new GameSession(0);
        Tetromino projection = game.getFallingProjection().clone();

        // Drop from spawn state, drop height = 18
        game.hardDrop();
        assertEquals(18, game.getHighScore());

        // Check that the tetromino was stacked
        assertFalse(game.getGameField().areCellsEmpty(projection.getCoordinates()));

        // Check that new tetromino was generated
        assertEquals(game.getBucket()[1], game.getFallingTetromino().getType());

        // Move the tetromino 4 (length I) cells to the right so that we are guaranteed to have 18 space cells below.
        game.getFallingTetromino().move(4, 0);

        // Wait until the tetromino reaches the floor
        for (int i = 0; i < 18; i++) game.step();

        projection = game.getFallingProjection().clone();

        // Drop the tetromino, drop height = 0
        game.hardDrop();
        assertEquals(18, game.getHighScore());
        assertFalse(game.getGameField().areCellsEmpty(projection.getCoordinates()));
        assertEquals(game.getBucket()[2], game.getFallingTetromino().getType());
    }
}
