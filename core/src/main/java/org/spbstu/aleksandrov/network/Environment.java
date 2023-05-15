package org.spbstu.aleksandrov.network;

import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.spbstu.aleksandrov.model.GameSession;
import org.spbstu.aleksandrov.model.Tetromino;
import org.spbstu.aleksandrov.network.util.NetworkUtil;
import org.spbstu.aleksandrov.solver.Solver;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.spbstu.aleksandrov.TetrisDl4j.DELAY;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.ROT_L;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.ROT_R;

public class Environment implements MDP<GameState, Integer, DiscreteSpace> {

    private final DiscreteSpace actionSpace = new DiscreteSpace(22*10*4);
    private final GameSession game;
    private int frames = 0;
    public Environment(final GameSession game) {
        this.game = game;
    }

    @Override
    public ObservationSpace<GameState> getObservationSpace() {
        return new GameObservationSpace();
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return actionSpace;
    }

    @Override
    public GameState reset() {
        return game.initializeGame();
    }

    @Override
    public void close() {}

    @Override
    public StepReply<GameState> step(final Integer actionIndex) {

        int lastScore = game.getScore();

        Solver solver = new Solver(game);
        Solver.Position[][][] p = solver.getLockPositions();
        Solver.Position[] linearP = new Solver.Position[22*10*4];

        for (int i = 0; i < 22; i++)
            for (int j = 0; j < 10; j++)
                for (int m = 0; m < 4; m++)
                    linearP[i + 10 * (j + 4 * m)] = p[i][j][m];

        Solver.Position position = linearP[actionIndex];

        if (position == null) {
            final GameState observation = game.buildStateObservation();
            return new StepReply<>(
                    observation,
                    -1,
                    isDone(),
                    "TetrisDl4j"
            );
        }

        Deque<Tetromino.Movement> movements = new ArrayDeque<>();
        while (position.getMovement() != null) {
            movements.addFirst(position.getMovement());
            position = position.getPredecessor();
        }

        for (Tetromino.Movement movement : movements) {

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
        game.hardDrop();

        int currentScore = game.getScore();

        NetworkUtil.waitMs(DELAY);

        double reward = (currentScore - lastScore) / 100d;

        // Get current state
        final GameState observation = game.buildStateObservation();

        return new StepReply<>(
                observation,
                reward,
                isDone(),
                "TetrisDl4j"
        );
    }

    @Override
    public boolean isDone() {
        return game.isGameOver();
    }

    @Override
    public MDP<GameState, Integer, DiscreteSpace> newInstance() {
        game.initializeGame();
        return new Environment(game);
    }
}
