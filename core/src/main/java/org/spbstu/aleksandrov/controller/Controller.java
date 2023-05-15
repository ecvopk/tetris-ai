package org.spbstu.aleksandrov.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import org.spbstu.aleksandrov.model.Tetromino;
import org.spbstu.aleksandrov.model.GameField;
import org.spbstu.aleksandrov.model.GameSession;

import static com.badlogic.gdx.Input.*;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.*;

public class Controller {

    private GameSession game;
    private GameField gameField;
    private Tetromino fallingTetromino;
    boolean rotateWasPressed = false;
    boolean hardDropWasPressed = false;
    private boolean leftWasPressed = false;
    private boolean rightWasPressed = false;
    private boolean spaceWasPressed = false;
    private long leftTime;
    private long rightTime;
    private long downTime;
    private long leftCoolDown = 200L;  // in milliseconds used for Delayed Auto Shift (DAS)
    private long rightCoolDown = 200L; // in milliseconds used for DAS
    private long downCoolDown = 50L;   // in milliseconds used for DAS
    private boolean doWork = false;

    public void processInput() {

        Tetromino.Movement rotate = null;
        boolean left = Gdx.input.isKeyPressed(Keys.LEFT);
        boolean hardDrop = Gdx.input.isKeyPressed(Keys.SPACE);
        boolean right = Gdx.input.isKeyPressed(Keys.RIGHT);
        boolean down = Gdx.input.isKeyPressed(Keys.DOWN);
        if (Gdx.input.isKeyPressed(Keys.UP)) rotate = ROT_R;
        if (Gdx.input.isKeyPressed(Keys.Z)) rotate = ROT_L;

        fallingTetromino = game.getFallingTetromino();

        if (left) {
            if (leftWasPressed) if (TimeUtils.timeSinceMillis(leftTime) > leftCoolDown) {
                doWork = true;
                leftCoolDown = 25L;
            }

            if (!leftWasPressed) doWork = true;

            if (doWork) {
                fallingTetromino.move(LEFT);
                if (!gameField.areCellsEmpty(fallingTetromino.getCoordinates()))
                    fallingTetromino.move(RIGHT);
                leftTime = TimeUtils.millis();
                doWork = false;
            }
        }

        leftWasPressed = left;
        if (!left) leftCoolDown = 200L;

        if (right) {
            if (rightWasPressed) if (TimeUtils.timeSinceMillis(rightTime) > rightCoolDown) {
                doWork = true;
                rightCoolDown = 25L;
            }
            if (!rightWasPressed) doWork = true;

            if (doWork) {
                fallingTetromino.move(RIGHT);
                if (!gameField.areCellsEmpty(fallingTetromino.getCoordinates()))
                    fallingTetromino.move(LEFT);
                rightTime = TimeUtils.millis();
                doWork = false;
            }
        }

        rightWasPressed = right;
        if (!right) rightCoolDown = 200L;

        if (down && TimeUtils.timeSinceMillis(downTime) > downCoolDown) {
            fallingTetromino.move(DOWN);
            game.addPoints(1);
            if (!gameField.areCellsEmpty(fallingTetromino.getCoordinates())) {
                fallingTetromino.move(0, 1);
                game.addPoints(-1);
            }
            downTime = TimeUtils.millis();
        }

        if (rotate != null && !rotateWasPressed) {
            gameField.rotateOnField(rotate, fallingTetromino);
            rotateWasPressed = true;
        }

        if (rotate == null) rotateWasPressed = false;

        if (hardDrop && !hardDropWasPressed) game.hardDrop();
        hardDropWasPressed = hardDrop;

        game.updateFallingProjection();
    }

    // Wait for SPACE to start new game
    public boolean waitForSpace() {
        if (hardDropWasPressed) {
            hardDropWasPressed = Gdx.input.isKeyPressed(Keys.SPACE);
            return false;
        } else {
            boolean result = spaceWasPressed && !Gdx.input.isKeyPressed(Keys.SPACE);
            spaceWasPressed = Gdx.input.isKeyPressed(Keys.SPACE);
            return result;
        }
    }

    public Controller(GameSession game, boolean disableDAS) {
        this.game = game;
        fallingTetromino = game.getFallingTetromino();
        gameField = game.getGameField();
        leftTime = TimeUtils.millis();
        rightTime = TimeUtils.millis();
        downTime = TimeUtils.millis();
        if (disableDAS) {
            leftCoolDown = Long.MAX_VALUE;
            rightCoolDown = Long.MAX_VALUE;
        }
    }

    public void update(GameSession game) {
        this.game = game;
        fallingTetromino = game.getFallingTetromino();
        gameField = game.getGameField();
        leftTime = TimeUtils.millis();
        rightTime = TimeUtils.millis();
        downTime = TimeUtils.millis();
        rotateWasPressed = false;
        rightWasPressed = false;
        leftWasPressed = false;
        doWork = false;
        hardDropWasPressed = false;
        rightCoolDown = 200L;
        leftCoolDown = 200L;
        downCoolDown = 50L;
    }
}
