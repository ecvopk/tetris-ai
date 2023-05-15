package org.spbstu.aleksandrov;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spbstu.aleksandrov.model.GameSession;
import org.spbstu.aleksandrov.model.Tetromino;
import org.spbstu.aleksandrov.network.Environment;
import org.spbstu.aleksandrov.network.GameState;
import org.spbstu.aleksandrov.network.util.GameStateUtil;
import org.spbstu.aleksandrov.network.util.NetworkUtil;
import org.spbstu.aleksandrov.solver.Solver;
import org.spbstu.aleksandrov.view.GameRenderer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.spbstu.aleksandrov.model.Tetromino.Movement.ROT_L;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.ROT_R;

public class TetrisDl4j extends Game {

    public static int DELAY = 0;
    private static final Logger LOG = LoggerFactory.getLogger(TetrisDl4j.class);

    private static void evaluateNetwork(GameSession game, String randomNetworkName) {
        final MultiLayerNetwork multiLayerNetwork = NetworkUtil.loadNetwork(randomNetworkName);
        int highscore = 0;
        for (int i = 0; i < 100; i++) {
            int score = 0;
            while (!game.isGameOver()) {
                try {
                    final GameState state = game.buildStateObservation();
                    final INDArray output = multiLayerNetwork.output(state.getMatrix(), false);
                    double[] data = output.data().asDouble();
                    int maxValueIndex = GameStateUtil.getMaxValueIndex(data);

                    Solver solver = new Solver(game);
                    Solver.Position[][][] p = solver.getLockPositions();
                    Solver.Position[] linearP = new Solver.Position[22*10*4];

                    for (int k = 0; k < 22; k++)
                        for (int j = 0; j < 10; j++)
                            for (int m = 0; m < 4; m++)
                                linearP[k + 10 * (j + 4 * m)] = p[k][j][m];

                    Solver.Position position = linearP[maxValueIndex];
                    if (position == null) {
                        game.hardDrop();
                        continue;
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

                    score = game.getScore();

                    NetworkUtil.waitMs(DELAY);
                } catch (final Exception e) {
                    LOG.error(e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("./log/log.txt", true));
                writer.write("" + i + "," + game.getScore() + "," + game.getTCounter() + "," + game.getLinesCleared() + "\n");
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (score > highscore) {
                highscore = score;
            }
            game.initializeGame();
        }
        LOG.info("Finished evaluation of the network, highscore was '{}'", highscore);
    }

    public static class NNThread extends Thread {

        final GameSession game;

        public NNThread(GameSession game) {
            this.game = game;
        }

        public void run() {
            // Give a name to the network we are about to train
            final String randomNetworkName = "nn/network-" + System.currentTimeMillis() + ".zip";

            // Create our training environment
            final Environment mdp = new Environment(game);
            final QLearningDiscreteDense<GameState> dql = new QLearningDiscreteDense<>(
                    mdp,
                    NetworkUtil.buildDQNFactory(),
                    NetworkUtil.buildConfig()
            );

            // Start the training
            dql.train();
            mdp.close();

            // Save network
            try {
                dql.getNeuralNet().save(randomNetworkName);
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }

            // Reset the game
            game.reset();
//            game = new GameSession(0); //game.reset(); // game.initializeGame();

            // Evaluate just trained network
            evaluateNetwork(game, randomNetworkName);
        }
    }

    public TetrisDl4j(boolean test, String path) {

    }

    private GameRenderer renderer;

    @Override
    public void create() {
        GameSession game = new GameSession(0);
        renderer = new GameRenderer(game, null);
        setScreen(renderer);
        NNThread nnThread = new NNThread(game);
        nnThread.start();
    }

    @Override
    public void render() {
        renderer.render(Gdx.graphics.getDeltaTime());
    }
}
