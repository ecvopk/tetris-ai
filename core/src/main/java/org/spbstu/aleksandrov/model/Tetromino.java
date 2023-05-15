package org.spbstu.aleksandrov.model;

import org.spbstu.aleksandrov.util.MovementListener;

import java.util.ArrayList;
import java.util.List;

import static org.spbstu.aleksandrov.model.Tetromino.Type.*;

public class Tetromino implements Cloneable {

    private MovementListener listener;
    private final Type type;
    private Color color;
    private Coordinate rotationPoint;
    private final ArrayList<Coordinate> coordinates = new ArrayList<>(4);

    /*
     * 0 = spawn state;
     * 1 = state resulting from a clockwise rotation ("right") from spawn;
     * 2 = state resulting from 2 successive rotations in either direction from spawn;
     * 3 = state resulting from a counter-clockwise ("left") rotation from spawn.
     */
    private int state = 0;

    public Tetromino(Type type) {
        this.type = type;

        switch (type) {
            case L:
                this.color = Color.ORANGE;
                rotationPoint = new Coordinate(4, 20);
                coordinates.add(0, new Coordinate(3, 20));
                coordinates.add(1, rotationPoint);
                coordinates.add(2, new Coordinate(5, 20));
                coordinates.add(3, new Coordinate(5, 21));

                break;
            case J:
                this.color = Color.BLUE;
                rotationPoint = new Coordinate(4, 20);
                coordinates.add(0, new Coordinate(3, 21));
                coordinates.add(1, new Coordinate(3, 20));
                coordinates.add(2, rotationPoint);
                coordinates.add(3, new Coordinate(5, 20));
                break;
            case S:
                this.color = Color.GREEN;
                rotationPoint = new Coordinate(4, 20);
                coordinates.add(0, new Coordinate(3, 20));
                coordinates.add(1, rotationPoint);
                coordinates.add(2, new Coordinate(4, 21));
                coordinates.add(3, new Coordinate(5, 21));
                break;
            case Z:
                this.color = Color.RED;
                rotationPoint = new Coordinate(4, 20);
                coordinates.add(0, new Coordinate(3, 21));
                coordinates.add(1, new Coordinate(4, 21));
                coordinates.add(2, rotationPoint);
                coordinates.add(3, new Coordinate(5, 20));
                break;
            case T:
                this.color = Color.PURPLE;
                rotationPoint = new Coordinate(4, 20);
                coordinates.add(0, new Coordinate(3, 20));
                coordinates.add(1, rotationPoint);
                coordinates.add(2, new Coordinate(5, 20));
                coordinates.add(3, new Coordinate(4, 21));
                break;
            case I:
                this.color = Color.CYAN;
                rotationPoint = new Coordinate(4, 20);
                coordinates.add(0, new Coordinate(3, 20));
                coordinates.add(1, new Coordinate(4, 20));
                coordinates.add(2, new Coordinate(5, 20));
                coordinates.add(3, new Coordinate(6, 20));
                break;
            case O:
                this.color = Color.YELLOW;
                rotationPoint = new Coordinate(4, 21);
                coordinates.add(0, new Coordinate(4, 20));
                coordinates.add(1, rotationPoint);
                coordinates.add(2, new Coordinate(5, 20));
                coordinates.add(3, new Coordinate(5, 21));
                break;
        }
    }

    public void rotate(Movement move) {

        int direction = 0;
        if (move == Movement.ROT_L) direction = 1;
        else if (move == Movement.ROT_R) direction = -1;

        // direction = 1 is rotation on left (3 -> 2; 2 -> 1; 1 -> 0; 0 -> 3)
        // direction = -1 is rotation on right (0 -> 1; 1 -> 2; 2 -> 3; 3 -> 0)
        state = (state + (4 - direction) % 4) % 4;

        if (this.type == Type.O) return;

        double rotX;
        double rotY;

        if (this.type == I) {
            rotX = rotationPoint.x + 0.5;
            rotY = rotationPoint.y - 0.5;
        } else {
            rotX = rotationPoint.x;
            rotY = rotationPoint.y;
        }

        for (Coordinate coordinate : coordinates) {

            int oldX = coordinate.x;
            int oldY = coordinate.y;

            // rotate (x, y) around (x0, y0) by angle a = 90 * direction
            // x' = x0 + (x - x0)cos(a) - (y - y0)sin(a)
            // y' = y0 + (x - x0)sin(a) - (y - y0)cos(a)
            // cos(90) = cos(-90) = 0;
            // sin(90) = 1
            // sin(-90) = 1

            coordinate.x = (int) ((rotY - oldY) * direction + rotX);
            coordinate.y = (int) ((oldX - rotX) * direction + rotY);
        }
    }

    public void rotate(Movement direction, int times) {
        while (times != 0) {
            this.rotate(direction);
            times--;
        }
    }

    public void move(int deltaX, int deltaY) {
        for (Coordinate coordinate : coordinates) {
            coordinate.x += deltaX;
            coordinate.y += deltaY;
        }
        if (type == I) {
            rotationPoint.x += deltaX;
            rotationPoint.y += deltaY;
        }
    }

    public void move(Movement movement) {
        switch (movement) {
            case LEFT:
                move(-1, 0);
                break;
            case RIGHT:
                move(1, 0);
                break;
            case DOWN:
                move(0, -1);
                break;
        }
        if (listener != null) listener.takeAction(movement);
    }

    public void move(Coordinate vector) {
        this.move(vector.getX(), vector.getY());
    }

    public void place(int x, int y) {
        move(x - rotationPoint.getX(), y - rotationPoint.getY());
    }

    public boolean areCoordinatesPossible() {
        for (Coordinate coordinate : coordinates) {
            if (coordinate.x < 0 || coordinate.x > 9 || coordinate.y > 21 || coordinate.y < 0) return false;
        }
        return true;
    }

    public enum Type {
        L, J, S, Z, T, I, O
    }

    public enum Color {
        ORANGE, BLUE, GREEN, RED, PURPLE, CYAN, YELLOW
    }

    public enum Movement {
        DOWN, RIGHT, LEFT, ROT_R, ROT_L;

        Movement negation() {
            switch (this) {
                case LEFT:
                    return RIGHT;
                case RIGHT:
                    return LEFT;
                case ROT_L:
                    return ROT_R;
                case ROT_R:
                    return ROT_L;
            }
            return DOWN;
        }

        private static final List<Movement> VALUES = List.of(values());

        public static Movement getMovementByIndex(final int index) {
            return VALUES.get(index);
        }
    }

    public static class Coordinate {
        private int x;
        private int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void negation() {
            this.x = -this.x;
            this.y = -this.y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Coordinate deepClone() {
            return new Coordinate(this.getX(), this.getY());
        }
    }

    public ArrayList<Coordinate> getCoordinates() {
        return coordinates;
    }

    public Color getColor() {
        return color;
    }

    public Type getType() {
        return type;
    }

    public int getState() {
        return state;
    }

    public Coordinate getRotationPoint() {
        return rotationPoint;
    }

    public void notifyListener(Movement move) {
        if (listener != null) listener.takeAction(move);
    }

    public void setListener(MovementListener listener) {
        this.listener = listener;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Tetromino clone() {
        Tetromino result = new Tetromino(type);
        result.state = this.state;
        for (int i = 0; i < 4; i++) {
            result.coordinates.get(i).setX(this.coordinates.get(i).getX());
            result.coordinates.get(i).setY(this.coordinates.get(i).getY());
        }
        if (this.type == I) result.rotationPoint = this.rotationPoint.deepClone();
        return result;
    }
}
