package org.spbstu.aleksandrov;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import org.spbstu.aleksandrov.controller.Controller;
import org.spbstu.aleksandrov.controller.Player;
import org.spbstu.aleksandrov.controller.Robot;
import org.spbstu.aleksandrov.controller.RobotInput;
import org.spbstu.aleksandrov.model.GameSession;
import org.spbstu.aleksandrov.solver.Solver;
import org.spbstu.aleksandrov.view.GameRenderer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings({"ConstantConditions", "PointlessBooleanExpression"})
public class Tetris extends Game {

    private Controller controller;
    private GameSession game;
    private GameRenderer renderer;
    private Solver solver;
    private SolverThread solverThread;
    private RobotInput player;
    private Input original;
    private Preferences prefs;

    public final static int INITIAL_LEVEL = 1;
    public final static boolean DEBUG = false;

    public final static boolean SURVIVAL = false;

    // ROBOT has access to the gameSession, interacts with the game through the GameSession methods
    public final static boolean ROBOT = true;

    // PLAYER has no access to the gameSession, interacts with the game when the Controller.processInput() called
    // PLAYER must be false, as work in progress.
    public final static boolean PLAYER = false;

    public final static boolean HINTS = false || ROBOT || PLAYER; // Enable Solver
    public final static boolean AUTOPLAY = ROBOT || PLAYER;

    public final static boolean AUTO_RESET = true;

    int frames = 1;

    @Override
    public void create() {

        prefs = Gdx.app.getPreferences("Tetris_User_Data");
        game = new GameSession(0);
        game.setLevel(INITIAL_LEVEL);

        if (HINTS || AUTOPLAY) {
            solver = new Solver(game);
            solver.setSolving(HINTS || AUTOPLAY);
            solverThread = new SolverThread(solver);
            solverThread.start();
        }
        if (AUTOPLAY) {
            if (ROBOT) player = new Robot(solver, game);
            else player = new Player(solver, game);
            original = Gdx.input;
            Gdx.input = player;
        }

        renderer = new GameRenderer(game, solver);
        setScreen(renderer);
        controller = new Controller(game, PLAYER);
    }

    public static class SolverThread extends Thread {

        Solver solver;

        public SolverThread(Solver solver) {
            this.solver = solver;
        }

        public void run() {
            solver.setSolving(HINTS);
            solver.startSolving();
        }

        public void exit() {
            solver.setSolving(false);
        }
    }

    // This method invokes when application is closed using UI window close button, not Ctrl+F2
    @Override
    public void dispose() {
        if ((HINTS || AUTOPLAY) && solverThread.isAlive()) solverThread.exit();
        prefs.putInteger("HighScore", game.getHighScore());
        prefs.flush();
        super.dispose();
    }

    int playedGames = 0;

    @Override
    public void render() {
        // FPS = 60

        renderer.render(Gdx.graphics.getDeltaTime());
        if (game.isGameOver()) {
            if (Gdx.input == player) Gdx.input = original;
            if ((HINTS || AUTOPLAY) && solverThread.isAlive()) solverThread.exit();
            if (controller.waitForSpace() || AUTO_RESET) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter("./log/log.txt", true));
                    writer.write("" + ++playedGames + "," + game.getScore() + "," + game.getTCounter() + "," + game.getLinesCleared() + "\n");
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                game = new GameSession(prefs.getInteger("HighScore", 0));
                controller.update(game);
                renderer.update(game);
                if (HINTS || AUTOPLAY) {
                    solver.update(game);
                    solverThread = new SolverThread(solver);
                    solverThread.start();
                }
            }
        } else {
            controller.processInput();
            if ((!DEBUG || PLAYER) && !ROBOT) {
                if (game.getLevel() > 29) game.step();
                else if (frames >= GameSession.FRAMES_PER_STEP[game.getLevel() - 1]) {
                    game.step();
                    frames = 1;
                }
            }
        }
        frames++;
    }
}