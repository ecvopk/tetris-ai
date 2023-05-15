package org.spbstu.aleksandrov.view;

import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.spbstu.aleksandrov.model.GameField;
import org.spbstu.aleksandrov.model.GameSession;
import org.spbstu.aleksandrov.model.Tetromino;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.spbstu.aleksandrov.Tetris;
import org.spbstu.aleksandrov.solver.Solver;

import static org.spbstu.aleksandrov.Tetris.HINTS;

@SuppressWarnings("ConstantConditions")
public class GameRenderer implements Screen {

    private final GlyphLayout layout;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private GameSession game;
    private GameField.CellType[][] gameField;
    private final Viewport viewport;
    private final boolean DEBUG = Tetris.DEBUG;
    private BitmapFont mainFont;
    private BitmapFont subFont;
    private final long fadeStart;
    private final Solver solver;

    // TODO can Tetromino has Color field when com.badlogic.gdx.graphics.Color is definitely the View?
    private final Color[] colorArray = {
            Color.ORANGE, Color.BLUE, Color.GREEN, Color.RED,
            Color.PURPLE, Color.CYAN, Color.YELLOW
    };

    public GameRenderer(GameSession newGame, Solver solver) {
        this.solver = solver;
        fadeStart = TimeUtils.millis();
        layout = new GlyphLayout();
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera(55, 25);
        camera.position.set(5 /*center of field*/, 10 /*center of field*/, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        viewport = new FillViewport(camera.viewportWidth, camera.viewportHeight, camera);//new FitViewport(camera.viewportWidth, camera.viewportHeight, camera);
        shapeRenderer.setProjectionMatrix(camera.combined);
        game = newGame;
        gameField = game.getGameField().getGameField();
        createFont();
    }

    private void createFont() {
        FileHandle fontFile = Gdx.files.internal("OpenSans-Regular.ttf");
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(fontFile);
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 120;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        mainFont = generator.generateFont(parameter);
        mainFont.setColor(Color.BLACK);
        mainFont.setUseIntegerPositions(false);
        mainFont.getData().setScale(0.015f);
        subFont = generator.generateFont(parameter);
        subFont.setColor(Color.BLACK);
        subFont.setUseIntegerPositions(false);
        subFont.getData().setScale(0.006f);
        generator.dispose();
    }

    @Override
    public void render(float v) {

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(1, 1, 1, 1);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderField();

        if (HINTS && solver != null) {
//            for (Tetromino tetromino : solver.getBestLocation()) {
//                if (tetromino != null) {
//                    renderTetromino(tetromino, Color.WHITE);
//                    Color color = colorArray[tetromino.getColor().ordinal()].cpy();
//                    color.a = 0.25f;
//                    renderTetromino(tetromino, color);
//                }
//            }
        }
        renderStackedBlocks();
        renderTetromino(game.getFallingProjection(), Color.LIGHT_GRAY);
        renderTetromino(game.getFallingTetromino(), colorArray[game.getFallingTetromino().getColor().ordinal()]);
        renderTetromino(game.getNextTetromino(), colorArray[game.getNextTetromino().getColor().ordinal()]);
        if (DEBUG) renderBucket();
        if (game.isGameOver()) renderGameOverMenu();
        shapeRenderer.end();

        String string =
                "Score: " + game.getScore()
                        + System.getProperty("line.separator")
                        + "Highscore: " + game.getHighScore()
                        + System.getProperty("line.separator")
                        + "Next up"
                        + System.getProperty("line.separator")
                        + System.getProperty("line.separator")
                        + "Level: " + game.getLevel()
                        + System.getProperty("line.separator")
                        + "Lines cleared: " + game.getLinesCleared()
                        + System.getProperty("line.separator")
                        + "Ts passed: " + game.getTCounter();
        if (DEBUG) string += System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Game is over: " + game.isGameOver();

        batch.begin();
        mainFont.draw(batch, string, 11.5f, 19f);
        if (DEBUG) mainFont.draw(batch, "bucket index: " + game.getCounter(), -20f, 19f);
        if (game.isGameOver()) gameOverText();
        batch.end();
    }

    public void renderField() {

        shapeRenderer.setColor(Color.LIGHT_GRAY);
        shapeRenderer.rect(-0.5f, -0.5f, 10.9f, 20.9f);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(-0.4f, -0.4f, 10.7f, 20.7f);

        Color color;
        float y = 0;
        float x;
        int floor;
        if (DEBUG) floor = 22;
        else floor = 20;

        for (int i = 0; i < floor; i++) {
            x = 0;
            for (int j = 0; j < 10; j++) {
                int index = gameField[i][j].ordinal();
                if (index == 7) {
                    color = Color.WHITE;
                    drawSquare(x, y, color);
                }
                x++;
            }
            y++;
        }
    }

    //TODO merge with renderField method
    public void renderStackedBlocks() {

        Color color;
        float y = 0;
        float x;

        for (int i = 0; i < 20; i++) {
            x = 0;
            for (int j = 0; j < 10; j++) {
                int index = gameField[i][j].ordinal();
                if (index != 7) {
                    color = colorArray[index];
                    drawSquare(x, y, color);
                }
                x++;
            }
            y++;
        }
    }

    public void renderTetromino(Tetromino tetromino, Color color) {
        for (Tetromino.Coordinate coordinate : tetromino.getCoordinates()) {
            if (tetromino == game.getNextTetromino()) {
                drawSquare(coordinate.getX() + 10, coordinate.getY() - 10, color);
            } else {
                if (coordinate.getY() < 20 || DEBUG) drawSquare(coordinate.getX(), coordinate.getY(), color);
            }
        }
        if (DEBUG) renderRotationPoint();
    }

    public void renderRotationPoint() {
        Tetromino.Coordinate rotationPoint = game.getFallingTetromino().getRotationPoint();
        Color color = Color.RED;
        shapeRenderer.setColor(color);
        if (game.getFallingTetromino().getType() == Tetromino.Type.I)
            shapeRenderer.rect(rotationPoint.getX() + 0.9f, rotationPoint.getY() - 0.1f, 0.1f, 0.1f);
        else shapeRenderer.rect(rotationPoint.getX() + 0.4f, rotationPoint.getY() + 0.4f, 0.1f, 0.1f);
    }

    public void drawSquare(float x, float y, Color color) {

        float lineThickness = 0.1f;
        float squareSize = 1.1f;

        if (color.equals(Color.WHITE))
            shapeRenderer.setColor(Color.GRAY);
        else shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.rect(x - lineThickness, y - lineThickness, squareSize, squareSize);
        // Render white rect for transparent squares
        if (color.a != 1) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(x, y, squareSize - 2 * lineThickness, squareSize - 2 * lineThickness);
        }
        shapeRenderer.setColor(color);
        shapeRenderer.rect(x, y, squareSize - 2 * lineThickness, squareSize - 2 * lineThickness);
    }

    public void renderGameOverMenu() {
        shapeRenderer.setColor(new Color(1, 1, 1, 0.75f));
        shapeRenderer.rect(-0.1f, -0.1f, 10.2f, 20.2f);
    }

    public void gameOverText() {
        String string = "Game Over";
        String press = "Press SPACE to play again";
        layout.setText(mainFont, string);
        mainFont.draw(batch, string, 5 - layout.width / 2f, 15 - layout.height / 2f);
        layout.setText(subFont, press);
        subFont.setColor(0, 0, 0,
                0.5f * (float) Math.sin(TimeUtils.timeSinceMillis(fadeStart) / 200f) + 0.5f);
        subFont.draw(batch, press, 5 - layout.width / 2f, 13 - layout.height / 2f);
    }

    public void renderBucket() {
        Tetromino.Type[] bucket = game.getBucket();
        int y = 2;
        for (Tetromino.Type i : bucket) {
            Tetromino tetromino = new Tetromino(i);
            tetromino.move(new Tetromino.Coordinate(-10, -y));
            renderTetromino(tetromino, colorArray[tetromino.getColor().ordinal()]);
            y += 3;
        }
    }

    public void update(GameSession newGame) {
        game = newGame;
        gameField = game.getGameField().getGameField();
    }

    @Override
    public void resize(int i, int i1) {
        viewport.update(i, i1);
        camera.update();
    }

    @Override
    public void show() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
    }
}
