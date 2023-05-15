package org.spbstu.aleksandrov.solver;

import org.spbstu.aleksandrov.Tetris;
import org.spbstu.aleksandrov.model.GameField;
import org.spbstu.aleksandrov.model.Tetromino;

import java.util.Arrays;

import static java.lang.Math.abs;
import static org.spbstu.aleksandrov.model.GameField.CellType.SPACE;

public class EvaluationCounter {

    // Коэффициенты весовой функции.
    private static final double[] p = {
            0.286127095297893900, 1.701233676909959200, 0.711304230768307700, 0.910665415998680400,
            1.879338064244357000, 2.168463848297177000, -0.265587111961757270, 0.289886584949610500,
            0.362361055261181730, -0.028668795795469625, 0.874179981113233100, -0.507409683144361900,
            -2.148676202831281000, -1.187558540281141700, -2.645656132241128000, 0.242043416268706620,
            0.287838126164431440
    };

    public static double evaluate(int removedLines, GameField gameField, Tetromino[] solutions) {

        double result = removedLines * p[0] +
                countLockHeight(solutions) +
                countWells(gameField.getGameField()) +
                countHoles(gameField.getGameField()) +
                countColumnTransitions(gameField.getGameField()) +
                countRowTransitions(gameField.getGameField()) +
                countColumnHeights(gameField.getGameField()) +
                countSolidCells(gameField.getGameField());
        if (Tetris.SURVIVAL) {
            if (removedLines > 0) result -= 1.0E2 * removedLines;
        } else {
            if (removedLines == 4) result -= 1.0E9;
        }
        return result;
    }

    // Общая высота блокировки – сумма высот над полом игрового поля, где заблокировано тетромино.
    // Высота блокировки отдельной фигуры — это вертикальное расстояние, на которое она может упасть при
    // сохранении ориентации, если удалить все занятые квадраты игрового поля.
    private static double countLockHeight(Tetromino[] solutions) {
        int[] lockHeights = new int[solutions.length];
        Arrays.fill(lockHeights, 22);
        int i = 0;
        for (Tetromino solution : solutions) {
            if (solution != null) {
                for (Tetromino.Coordinate coordinate : solution.getCoordinates()) {
                    int y = coordinate.getY();
                    if (y < lockHeights[i]) lockHeights[i] = y;
                }
            }
            i++;
        }
        double result = 0;
        for (int lockHeight : lockHeights) result += lockHeight * p[1];
        return result;
    }

    // Общее количество ячеек-колодцев – количество ячеек внутри колодцев.
    // Ячейка-колодец — это пустая ячейка, расположенная над всеми занятыми ячейками в столбце так, что её левый
    // и правый сосед являются занятыми ячейками; при определении колодцев стенки игрового поля считаются занятыми
    // ячейками.
    // Общее количество глубоких колодцев – количество колодцев, содержащих три или более ячеек-колодцев.
    private static double countWells(GameField.CellType[][] gameField) {
        int wellCells = 0; // Общее количество ячеек-колодцев
        int deepWells = 0; // Общее количество глубоких колодцев
        columns:
        for (int j = 0; j < 10; j++) {
            // rows:
            for (int i = 21; i >= 0; i--) { // index fix, vrode done

                if (gameField[i][j] != SPACE) continue columns;

                if (isWell(i, j, gameField)) {
                    wellCells++;
                    int deep = 1;
                    int k = i - 1;
                    while (k >= 0 && isWell(k, j, gameField)) {
                        wellCells++;
                        deep++;
                        k--;
                    }
                    if (deep >= 3) deepWells++;
                    continue columns;
                }
            }
        }
        return wellCells * p[2] + deepWells * p[3];
    }

    private static boolean isWell(int i, int j, GameField.CellType[][] gameField) {
        boolean isWell;
        if (gameField[i][j] != SPACE) return false;
        if (j == 0) {
            isWell = gameField[i][j + 1] != SPACE;
        } else if (j == 9) {
            isWell = gameField[i][j - 1] != SPACE;
        } else {
            isWell = gameField[i][j - 1] != SPACE && gameField[i][j + 1] != SPACE;
        }
        return isWell;
    }

    // Общее количество отверстий в столбцах – количество пустых ячеек, непосредственно над которыми есть
    // занятые ячейки. Пол игрового поля не сравнивается с ячейкой над ним. Пустые столбцы не содержат отверстий.
    //
    // Общее взвешенное количество отверстий в столбцах – сумма индексов строк отверстий в столбцах.
    // В этом случае строки индексируются сверху вниз, начиная с 1.
    // Идея в том, чтобы расположенным ниже в куче отверстиям давать больший штраф, потому что для заполнения
    // верхних отверстий требуется очистить меньшее количество строк.
    //
    // Общее количество глубин отверстий в столбцах – сумма вертикальных расстояний между каждым отверстием
    // и вершиной столбца, в котором оно находится. Вершина — это самая верхняя занятая ячейка в пределах столбца,
    // а глубина отверстия — это разность между индексом строки отверстия и индексом строки вершины.
    //
    // Минимальная глубина отверстий в столбцах – наименьшая глубина отверстий в столбцах.
    // Если отверстий нет, то по умолчанию параметр имеет значение высоты поля (20).
    //
    // Максимальная глубина отверстий в столбцах – наибольшая глубина отверстий в столбцах.
    // Если отверстий нет, то значение по умолчанию равно 0.
    private static double countHoles(GameField.CellType[][] gameField) {
        int holes = 0; // Общее количество отверстий в столбцах
        int weigth = 0; // Общее взвешенное количество отверстий в столбцах
        int totalDepth = 0; // Общее количество глубин отверстий в столбцах
        int minHoleDepth = 22; // Минимальная глубина отверстий в столбцах
        int maxHoleDepth = 0; // Максимальная глубина отверстий в столбцах
        for (int j = 0; j < 10; j++) {
            for (int i = 21; i >= 1; i--) {
                if (gameField[i - 1][j] == SPACE && gameField[i][j] != SPACE) {
                    holes++;
                    weigth += 22 - (i - 1);
                    int k = i - 2;
                    int depth = 1;
                    while (k >= 0 && gameField[k][j] == SPACE) {
                        weigth += 22 - k;
                        depth++;
                        k--;
                    }
                    totalDepth += depth;
                    if (depth < minHoleDepth) minHoleDepth = depth;
                    if (depth > maxHoleDepth) maxHoleDepth = depth;
                }
            }
        }
        return holes * p[4] + weigth * p[5] + totalDepth * p[6] + minHoleDepth * p[7] + maxHoleDepth * p[8];
    }

    // Общее количество переходов в столбцах – количество пустых ячеек, соседних с занятой ячейкой (или наоборот)
    // в пределах одного столбца.
    private static double countColumnTransitions(GameField.CellType[][] gameField) {
        int result = 0;
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 21; i++) {
                if (gameField[i][j] == SPACE ^ gameField[i + 1][j] == SPACE) result++;
            }
        }
        return result * p[9];
    }

    //Общее количество переходов в строках: переход в строках — это пустая ячейка, соседствующая с занятой ячейкой
    // (или наоборот) в пределах одного ряда. Пустые ячейки рядом со стенками игрового поля считаются переходами.
    // Общее количество высчитывается для всех строк игрового поля.
    // Однако совершенно пустые строки не учитываются в общем количестве переходов.
    private static double countRowTransitions(GameField.CellType[][] gameField) {
        int result = 0;
        for (int i = 0; i < 22; i++) {
            boolean lineIsEmpty = true;
            for (int j = 0; j < 10; j++) {
                if (gameField[i][j] != SPACE) lineIsEmpty = false;
                if (j == 0) {
                    if (gameField[i][j] == SPACE) result++;
                } else if (j == 9) {
                    if (gameField[i][j] == SPACE) result++;
                } else if (gameField[i][j] == SPACE ^ gameField[i][j + 1] == SPACE) result++;
            }
            if (lineIsEmpty) result -= 2;
        }
        return result * p[10];
    }

    // Общее количество высот столбцов – сумма вертикальных расстояний между вершиной каждого столбца и полом игрового
    // поля. Столбец, содержащий всего 1 занятую ячейку, имеет высоту 1, а полностью пустой столбец — высоту 0.
    //
    // Высота кучи – высота наибольшего столбца.
    //
    // Разброс высот столбцов – разность высот между самым высоким и самым низким столбцами.
    //
    // Дисперсия высот столбцов – сумма абсолютных по модулю разностей между высотами всех соседних столбцов.
    private static double countColumnHeights(GameField.CellType[][] gameField) {
        int totalHeight = 0; // Общее количество высот столбцов p[11]
        int maxHeight = 0; // Высота кучи
        int minHeight = 22;
        int height = 0;
        int lastHeight;
        int dispersion = 0; // Дисперсия высот столбцов
        int spread; // Разброс высот столбцов
        for (int j = 0; j < 10; j++) {
            int i = 21;
            lastHeight = height;
            while (i >= 0) {
                if (gameField[i][j] != SPACE) {
                    totalHeight += i + 1;
                    height = i + 1;
                    if (i + 1 > maxHeight) maxHeight = i + 1;
                    if (i + 1 < minHeight) minHeight = i + 1;
                    break;
                }
                i--;
            }
            if (j != 0) dispersion += abs(height - lastHeight);
        }
        spread = maxHeight - minHeight;
        return totalHeight * p[11] + spread * p[13] + maxHeight * p[12] + dispersion * p[16];
    }

    // Общее количество занятых ячеек – количество занятых ячеек на игровом поле.
    // Общее взвешенное количество занятых ячеек – сумма высот всех занятых ячеек. Строка над полом имеет высоту.
    private static double countSolidCells(GameField.CellType[][] gameField) {
        int counter = 0; // Общее количество занятых ячеек
        int weight = 0; // Общее взвешенное количество занятых ячеек
        for (int j = 0; j < 10; j++) {
            for (int i = 0; i < 22; i++) {
                if (gameField[i][j] != SPACE) {
                    counter++;
                    weight += i + 1;
                }
            }
        }
        return counter * p[14] + weight * p[15];
    }
}
