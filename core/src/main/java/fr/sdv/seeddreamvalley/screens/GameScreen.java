package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.settings.GameSettings;
import fr.sdv.seeddreamvalley.utils.Constants;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class GameScreen extends ScreenAdapter {
    private ShapeRenderer shape;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // ── Référence principale ─────────────────────────────────────────
    private final Main game;

    // ── Rendu ────────────────────────────────────────────────────────
    private OrthographicCamera camera;
    private final ScreenViewport viewport;

    // ── Map (décommentée quand GameMap sera prête) ───────────────────
    // private GameMap map;

    // ── Joueur (placeholder avant le vrai sprite) ────────────────────
    private float playerX;
    private float playerY;
    private static final float MOVE_SPEED = 80f;

    // ────────────────────────────────────────────────────────────────
    public GameScreen(Main game) {
        this.game = game;

        float mapPixelW = Constants.MAP_WIDTH  * Constants.TILE_SIZE; // 1024
        float mapPixelH = Constants.MAP_HEIGHT * Constants.TILE_SIZE; // 1024

        camera = new OrthographicCamera();
        camera.setToOrtho(false, mapPixelW, mapPixelH); // caméra calée sur la map
        viewport = new ScreenViewport(camera);

        playerX = mapPixelW / 2f;
        playerY = mapPixelH / 2f;
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        shape = new ShapeRenderer();

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        camera.zoom = 1f; // zoom neutre, la map remplit exactement
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateCamera();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply(); // <-- important : applique le viewport avant de rendre
        mapRenderer.setView(camera);
        mapRenderer.render();
        drawPlayer();
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

   private void fitCameraToMap() {
        float mapPixelW = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
        float mapPixelH = Constants.MAP_HEIGHT * Constants.TILE_SIZE;

        float screenW = Gdx.graphics.getWidth();
        float screenH = Gdx.graphics.getHeight();

        float zoomX = mapPixelW / screenW;
        float zoomY = mapPixelH / screenH;
        float fitZoom = Math.max(zoomX, zoomY);

        // Sauvegarde le zoom calculé dans les settings
        GameSettings.get().zoom = fitZoom;

        camera.zoom = fitZoom;
        camera.position.set(mapPixelW / 2f, mapPixelH / 2f, 0);
        camera.update();
    }

    // ── Caméra centrée sur le joueur + zoom du settings ──────────────
    private void updateCamera() {
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
        map.dispose();
        mapRenderer.dispose();
        shape.dispose();
    }

    private void drawPlayer() {
        shape.setProjectionMatrix(camera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);

        // Corps
        shape.setColor(0.2f, 0.6f, 1f, 1f);
        shape.rect(playerX - 8, playerY, 16, 20);

        // Tête
        shape.setColor(0.95f, 0.75f, 0.55f, 1f);
        shape.circle(playerX, playerY + 26, 10, 16);

        shape.end();
    }
}