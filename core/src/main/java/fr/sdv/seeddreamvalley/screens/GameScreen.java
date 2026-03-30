package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.settings.GameSettings;
import fr.sdv.seeddreamvalley.utils.Constants;

public class GameScreen extends ScreenAdapter {

    // ── Référence principale ─────────────────────────────────────────
    private final Main game;

    // ── Rendu ────────────────────────────────────────────────────────
    private final OrthographicCamera camera;
    private final FitViewport        viewport;

    // ── Map (décommentée quand GameMap sera prête) ───────────────────
    // private GameMap map;

    // ── Joueur (placeholder avant le vrai sprite) ────────────────────
    private float playerX;
    private float playerY;
    private static final float MOVE_SPEED = 80f;

    // ────────────────────────────────────────────────────────────────
    public GameScreen(Main game) {
        this.game = game;

        // Caméra + viewport
        camera   = new OrthographicCamera();
        viewport = new FitViewport(
            Constants.VIEWPORT_WIDTH  * Constants.TILE_SIZE,
            Constants.VIEWPORT_HEIGHT * Constants.TILE_SIZE,
            camera
        );

        // Applique le zoom configuré dans le menu
        camera.zoom = GameSettings.get().zoom;

        // Map (décommentée quand GameMap sera prête)
        // map = new GameMap();

        // Spawn au centre de la map
        playerX = (Constants.MAP_WIDTH  / 2f) * Constants.TILE_SIZE;
        playerY = (Constants.MAP_HEIGHT / 2f) * Constants.TILE_SIZE;
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        handleInput(delta);
        updateCamera();

        // Clear
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Rendu map (décommenté quand GameMap sera prête)
        // map.render(camera);
    }

    // ── Déplacement avec les touches configurées ─────────────────────
    private void handleInput(float delta) {
        GameSettings s = GameSettings.get();
        float speed    = MOVE_SPEED * delta;

        if (Gdx.input.isKeyPressed(s.keyUp))    playerY += speed;
        if (Gdx.input.isKeyPressed(s.keyDown))  playerY -= speed;
        if (Gdx.input.isKeyPressed(s.keyLeft))  playerX -= speed;
        if (Gdx.input.isKeyPressed(s.keyRight)) playerX += speed;

        // Clamp dans les limites de la map
        float mapPixelW = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
        float mapPixelH = Constants.MAP_HEIGHT * Constants.TILE_SIZE;
        playerX = Math.max(0, Math.min(playerX, mapPixelW));
        playerY = Math.max(0, Math.min(playerY, mapPixelH));
    }

    // ── Caméra centrée sur le joueur + zoom du settings ──────────────
    private void updateCamera() {
        camera.zoom = GameSettings.get().zoom;
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        // map.dispose(); // décommenté quand GameMap sera prête
    }
}