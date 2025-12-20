package com.boltstorms.phantomball.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.boltstorms.phantomball.PhantomBallGame;
import com.boltstorms.phantomball.util.Const;
import com.boltstorms.phantomball.util.PlayerProfile;

public class MenuScreen extends ScreenAdapter {

    private final PhantomBallGame game;

    private SpriteBatch batch;
    private BitmapFont font;

    private OrthographicCamera cam;
    private Viewport viewport;

    private String playerName = "Player";

    private Rectangle nameRect;
    private Rectangle startRect;

    private boolean consumedThisTouch = false;

    public MenuScreen(PhantomBallGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2.0f);

        cam = new OrthographicCamera();
        viewport = new FitViewport(Const.VIRTUAL_W, Const.VIRTUAL_H, cam);
        viewport.apply(true);

        batch.setProjectionMatrix(cam.combined);

        playerName = PlayerProfile.getPlayerName();

        rebuildLayout();
    }

    private void rebuildLayout() {
        float boxW = Const.VIRTUAL_W * 0.85f;
        float boxH = 120f;

        float x = (Const.VIRTUAL_W - boxW) * 0.5f;

        float nameY = Const.VIRTUAL_H * 0.55f;
        float startY = Const.VIRTUAL_H * 0.35f;

        nameRect = new Rectangle(x, nameY, boxW, boxH);
        startRect = new Rectangle(x, startY, boxW, boxH);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(cam.combined);
    }

    @Override
    public void render(float delta) {

        // Optional: Android back exits
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            Gdx.app.exit();
        }

        // Handle touch with viewport conversion
        if (Gdx.input.justTouched() && !consumedThisTouch) {
            consumedThisTouch = true;

            Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
            // convert screen -> world using viewport/camera
            viewport.unproject(touch);

            if (nameRect.contains(touch)) {
                openNameKeyboard();
            } else if (startRect.contains(touch)) {
                startGame();
            }
        }

        if (!Gdx.input.isTouched()) consumedThisTouch = false;

        // Clear
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        font.draw(batch, "PHANTOM BALL", Const.VIRTUAL_W * 0.5f - 160, Const.VIRTUAL_H - 120);

        // NAME box text
        font.draw(batch, "NAME:", nameRect.x + 28, nameRect.y + nameRect.height - 25);
        font.draw(batch, playerName, nameRect.x + 28, nameRect.y + 50);
        font.draw(batch, "(tap to edit)", nameRect.x + 28, nameRect.y + 24);

        // START box text
        font.draw(batch, "START", startRect.x + 28, startRect.y + 70);
        font.draw(batch, "(tap to play)", startRect.x + 28, startRect.y + 30);

        batch.end();
    }

    private void openNameKeyboard() {
        Gdx.input.getTextInput(new Input.TextInputListener() {
            @Override
            public void input(String text) {
                if (text == null) return;
                text = text.trim();
                playerName = text.isEmpty() ? "Player" : text;
                PlayerProfile.setPlayerName(playerName);
            }

            @Override
            public void canceled() {
                // keep current name
            }
        }, "Enter Name", playerName, "Player");
    }

    private void startGame() {
        PlayerProfile.setPlayerName(playerName);
        game.setScreen(new GameScreen(game));
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }
}
