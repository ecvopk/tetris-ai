package org.spbstu.aleksandrov.controller;

import org.spbstu.aleksandrov.model.GameSession;
import org.spbstu.aleksandrov.solver.Solver;
import org.spbstu.aleksandrov.util.MovementListener;

import java.util.Deque;

import static org.spbstu.aleksandrov.model.Tetromino.Movement;

// Work in progress
public class Player extends RobotInput {

    private int pressedKey = 0;
    private int k;
    private final Solver solver;
    private Deque<Movement> solution;
    private boolean takingAction;
    private GameSession gameSession;
    private MovementListener listener;

    public Player(Solver solver, GameSession game) {
        this.solver = solver;
        this.gameSession = game;
        listener = new MovementListener(this);
        game.getFallingTetromino().setListener(listener);
    }

    @Override
    public boolean takeAction() {

        if (solution == null || k >= solution.size()) {
            solution = solver.getMovements();
            k = 0;
            takingAction = true;
        }
        if (solution == null || k >= solution.size()) return true;
        Movement movement = solution.pop();

        switch (movement) {
            case RIGHT:
                pressedKey = Keys.RIGHT;
                break;
            case LEFT:
                pressedKey = Keys.LEFT;
                break;
            case DOWN:
                pressedKey = 0;
                break;
            case ROT_L:
                pressedKey = Keys.Z;
                break;
            case ROT_R:
                pressedKey = Keys.UP;
                break;
        }
        if (movement != Movement.DOWN) k++;
        return true;
    }

    @Override
    public boolean isKeyPressed(int key) {
        return key == pressedKey;
    }
}
