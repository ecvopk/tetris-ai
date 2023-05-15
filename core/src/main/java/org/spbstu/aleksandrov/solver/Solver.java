package org.spbstu.aleksandrov.solver;

import org.spbstu.aleksandrov.controller.Robot;
import org.spbstu.aleksandrov.controller.RobotInput;
import org.spbstu.aleksandrov.model.GameField;
import org.spbstu.aleksandrov.model.GameSession;

import org.spbstu.aleksandrov.model.Tetromino.Movement;
import org.spbstu.aleksandrov.model.Tetromino;

import java.util.*;

import static org.spbstu.aleksandrov.Tetris.*;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.ROT_L;
import static org.spbstu.aleksandrov.model.Tetromino.Movement.ROT_R;

public class Solver {

    private GameSession gameSession;
    private final RobotInput robot;
    private final int LIMIT = 2; // How many next tetrominoes will be processed
    private boolean solving = false;
    private int lastCounter = -1;
    private boolean ready;
    private Tetromino.Type[] bucket = new Tetromino.Type[7];
    private int level;

    // Delay for enumeration of locked positions demonstration
    private final int DELAY = 0;

    public Solver(GameSession gameSession) {
        this.gameSession = gameSession;
        this.robot = new Robot(this, gameSession);
    }

    public void startSolving() {
        while (solving) {
            if (gameSession.getCounter() != lastCounter && gameSession.isReady()) {
                ready = false;
                bestGrade = Double.MAX_VALUE;
                bucket = gameSession.getBucket();
                level = gameSession.getLevel();
                lastCounter = gameSession.getCounter();
                search(gameSession.getGameField(), 0, 0);
                if (ROBOT || PLAYER) ready = robot.takeAction();
                else ready = true;
            } else {
                ready = true;
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    double bestGrade;
    public final Position[][][] lockPositions = new Position[22][10][4];
    private final boolean[][][][] visited = new boolean[7][10][22][4];
    private final Position[] solution = new Position[LIMIT];
    private final Position[] bestSolution = new Position[LIMIT];

    // BFS
    private void search(GameField field, int counter, int clearedLines) {

        // gameCounter points at the nextTetromino + 1
        int index = lastCounter - 2 + counter;
        Tetromino initTetromino;

        if (counter == 0) initTetromino = gameSession.getFallingTetromino().clone();
        else if (counter == 1) initTetromino = gameSession.getNextTetromino().clone();
        else initTetromino = new Tetromino(bucket[index]);

        for (int i = counter; i < LIMIT; i++) {
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 22; y++) {
                    for (int r = 0; r < 4; r++) {
                        visited[i][x][y][r] = false;
                    }
                }
            }
        }

        Deque<Position> queue = new ArrayDeque<>();
        queue.add(new Position(null, initTetromino.clone(), null, 0));

        while (!queue.isEmpty()) {
            Position current = queue.pop();
            // Generate following positions
            for (Movement movement : Movement.values()) {
                Tetromino tetromino = current.getTetromino().clone();
                if (movement == ROT_L || movement == ROT_R)
                    gameSession.getGameField().rotateOnField(movement, tetromino);
                else tetromino.move(movement);
                boolean condition = field.areCellsEmpty(tetromino.getCoordinates());
                if (condition) {
                    // here only push to queue new positions
                    int x = tetromino.getCoordinates().get(0).getX();
                    int y = tetromino.getCoordinates().get(0).getY();
                    int state = tetromino.getState();
                    if (!visited[counter][x][y][state]) {
                        if (PLAYER) {
                            // restrictions associated with level (PLAYER plays)
                            int limit;
                            if (level <= 30) limit = GameSession.FRAMES_PER_STEP[level - 1];
                            else limit = 1;

                            if (current.getK() < limit) {
                                visited[counter][x][y][state] = true;
                                if (movement != Movement.DOWN)
                                    queue.add(new Position(current, tetromino.clone(), movement, current.getK() + 1));
                                else queue.add(new Position(current, tetromino.clone(), movement, 0));
                            } else {
                                if (movement == Movement.DOWN) {
                                    visited[counter][x][y][state] = true;
                                    lockPositions[y][x][state] = new Position(current, tetromino.clone(), movement, 0);
                                    queue.add(new Position(current, tetromino.clone(), movement, 0));
                                }
                            }
                        } else {
                            // no restrictions (ROBOT plays or HINTS enabled)
                            if (!visited[counter][x][y][state]) {
                                visited[counter][x][y][state] = true;
                                lockPositions[y][x][state] = new Position(current, tetromino.clone(), movement, 0);
                                queue.add(new Position(current, tetromino.clone(), movement, 0));
                            }
                        }
                    }
                }
            }

            // check if position is locked
            Tetromino tetromino = current.getTetromino();
            tetromino.move(0, -1);
            boolean locked = !field.areCellsEmpty(tetromino.getCoordinates());
            tetromino.move(0, 1);

            // process locked position, make deeper search
            if (locked) {
                GameField modified = field.clone();
                modified.stackTetromino(tetromino);
                int newClearedLines = clearedLines + modified.checkLinesToClear(tetromino.getCoordinates());
                modified.cleanLines();
                solution[counter] = current;
                if (counter + 1 < LIMIT && index < 6) search(modified, counter + 1, newClearedLines);
                else {

                    // Delay for enumeration of locked positions demonstration
                    try {
                        Thread.sleep(DELAY);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    double grade = EvaluationCounter.evaluate(newClearedLines, modified, getLocation());

                    if (grade < bestGrade) {
                        bestGrade = grade;
                        int i = 0;
                        for (Position p : solution) bestSolution[i++] = p;
                        // if we did not search for all the next LIMIT tetrominos (bucket ended) fill the last elements
                        // of solution with null
                        for (int j = LIMIT - 1; j > LIMIT - (LIMIT - (counter + 1)) - 1; j--) bestSolution[j] = null;
                    }
                }
            }
        }
    }

    public Position[][][] getLockPositions() {
        return lockPositions;
    }

    public void update(GameSession gameSession) {
        lastCounter = -1;
        this.gameSession = gameSession;
        robot.update(gameSession);
    }

    public static class Position {
        private final int k; // counter how many movements done after DOWN
        private final Position predecessor;
        private final Movement movement; // movement that lead to this position
        private final Tetromino tetromino;
        private final double grade;

        Position(Position predecessor, Tetromino tetromino, Movement movement, int k) {
            this.grade = 0;
            this.k = k;
            this.predecessor = predecessor;
            this.tetromino = tetromino;
            this.movement = movement;
        }

        Position(Position predecessor, Tetromino tetromino, Movement movement, int k, double grade) {
            this.grade = grade;
            this.k = k;
            this.predecessor = predecessor;
            this.tetromino = tetromino;
            this.movement = movement;
        }

        public Tetromino getTetromino() {
            return tetromino;
        }

        public Movement getMovement() {
            return movement;
        }

        public Position getPredecessor() {
            return predecessor;
        }

        public int getK() {
            return k;
        }

        public double getGrade() {
            return grade;
        }
    }

    public void setSolving(boolean solving) {
        this.solving = solving;
    }

    public Tetromino[] getLocation() {
        Tetromino[] result = new Tetromino[solution.length];
        int i = 0;
        for (Position p : solution) {
            if (p != null) result[i++] = p.getTetromino();
        }
        return result;
    }

    public Tetromino[] getBestLocation() {
        Tetromino[] result = new Tetromino[bestSolution.length];
        int i = 0;
        for (Position p : bestSolution) {
            if (p != null) result[i++] = p.getTetromino();
        }
        return result;
    }

    public Deque<Movement> getMovements() {
        Position position = bestSolution[0];
        Deque<Movement> movements = new ArrayDeque<>();
        while (position.getMovement() != null) {
            movements.addFirst(position.getMovement());
            position = position.getPredecessor();
        }
        ready = false;
        return movements;
    }
}
