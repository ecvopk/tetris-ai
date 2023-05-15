package org.spbstu.aleksandrov.controller;

import org.spbstu.aleksandrov.model.GameSession;
import org.spbstu.aleksandrov.solver.Solver;

import java.util.Deque;

import static org.spbstu.aleksandrov.model.Tetromino.Movement;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.*;

public class Robot extends RobotInput {

    private GameSession game;
    private final Solver solver;

    // Delay for movement demonstration
    @SuppressWarnings("FieldCanBeLocal")
    private final int DELAY = 50;

    public Robot(Solver solver, GameSession game) {
        this.solver = solver;
        this.game = game;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean takeAction() {

        Deque<Movement> movements = solver.getMovements();

        for (Movement movement : movements) {

            if (movement == ROT_L || movement == ROT_R)
                game.getGameField().rotateOnField(movement, game.getFallingTetromino());
            else game.getFallingTetromino().move(movement);

            if (DELAY != 0) {
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        game.updateFallingProjection();
        return game.hardDrop();
    }

    public void update(GameSession game) {
        this.game = game;
    }
}
