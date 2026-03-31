package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.settings.GameSettings;
import fr.sdv.seeddreamvalley.utils.Constants;
import fr.sdv.seeddreamvalley.world.Plot;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends ScreenAdapter {

    // ── Référence principale ─────────────────────────────────────────
    private final Main game;

    // ── Rendu ────────────────────────────────────────────────────────
    private OrthographicCamera camera;
    private final ScreenViewport viewport;
    private ShapeRenderer shape;

    // ── Map Tiled ────────────────────────────────────────────────────
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // ── Joueur ───────────────────────────────────────────────────────
    private float playerX;
    private float playerY;
    private static final float MOVE_SPEED = 80f;

    // ── Parcelles ────────────────────────────────────────────────────
    private final List<Plot> plots = new ArrayList<>();
    private Plot nearestPlot = null;

    // ────────────────────────────────────────────────────────────────
    public GameScreen(Main game) {
        this.game = game;

        float mapPixelW = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
        float mapPixelH = Constants.MAP_HEIGHT * Constants.TILE_SIZE;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, mapPixelW, mapPixelH);
        viewport = new ScreenViewport(camera);

        playerX = mapPixelW / 2f;
        playerY = mapPixelH / 2f;


    }

    // ── Définition des parcelles ─────────────────────────────────────
    // Ajuste startX/startY/cols/rows/step selon ta map.tmx
    private void initPlots() {
    com.badlogic.gdx.maps.tiled.TiledMapTileLayer layer =
        (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Layer_1");

    int[] parcelTileIds = {21};

    for (int x = 0; x < layer.getWidth(); x++) {
        for (int y = 0; y < layer.getHeight(); y++) {
            com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell cell = layer.getCell(x, y);
            if (cell == null) continue;

            int tileId = cell.getTile().getId();

            // DEBUG — affiche l'ID de chaque tuile détectée
            for (int id : parcelTileIds) {
                if (tileId == id) {
                    System.out.println("Parcelle trouvée : x=" + x + " y=" + y + " tileId=" + tileId);
                    plots.add(new Plot(x, y));
                    break;
                }
            }
        }
    }

    System.out.println("Total parcelles : " + plots.size());
}

    // ────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        map         = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        shape       = new ShapeRenderer();
        initPlots();
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        camera.zoom = 1f;
        camera.position.set(playerX, playerY, 0);
        camera.update();
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        handleInput(delta);
        updateNearestPlot();
        updateCamera();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        drawPlots();
        drawPlayer();
    }

    // ── Déplacement + clic ───────────────────────────────────────────
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

        // Clic gauche
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            handlePlotClick();
        }
    }

    // ── Clic sur une parcelle (max 3 tuiles du joueur) ───────────────
    private void handlePlotClick() {
        int ts = Constants.TILE_SIZE;

        // Convertit la souris en coordonnées monde
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);

        // Tuile cliquée
        int clickTileX = (int)(mouse.x / ts);
        int clickTileY = (int)(mouse.y / ts);

        // Tuile du joueur
        int playerTileX = (int)(playerX / ts);
        int playerTileY = (int)(playerY / ts);

        // Vérifie la distance max 3 tuiles
        int dx = Math.abs(clickTileX - playerTileX);
        int dy = Math.abs(clickTileY - playerTileY);
        if (dx > 3 || dy > 3) return;

        // Cherche la parcelle cliquée et toggle sa couleur
        for (Plot plot : plots) {
            if (plot.tileX == clickTileX && plot.tileY == clickTileY) {
                plot.clicked = !plot.clicked;
                return;
            }
        }
    }

    // ── Parcelle adjacente la plus proche (1 tuile de distance) ──────
    private void updateNearestPlot() {
        int ts = Constants.TILE_SIZE;

        int playerTileX = (int)(playerX / ts);
        int playerTileY = (int)(playerY / ts);

        nearestPlot = null;
        float bestDist = Float.MAX_VALUE;

        for (Plot plot : plots) {
            int dx = Math.abs(plot.tileX - playerTileX);
            int dy = Math.abs(plot.tileY - playerTileY);

            // Adjacent = à 1 tuile max, mais pas sur la même tuile
            boolean adjacent = (dx <= 1 && dy <= 1) && !(dx == 0 && dy == 0);

            if (adjacent) {
                float dist = dx * dx + dy * dy;
                if (dist < bestDist) {
                    bestDist    = dist;
                    nearestPlot = plot;
                }
            }
        }
    }

    // ── Dessine toutes les parcelles ─────────────────────────────────
    private void drawPlots() {
        int ts = Constants.TILE_SIZE;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.setProjectionMatrix(camera.combined);

        for (Plot plot : plots) {
            // Remplissage brun uniquement si cliqué
            if (plot.clicked) {
                float px = plot.pixelX(ts);
                float py = plot.pixelY(ts);

                shape.begin(ShapeRenderer.ShapeType.Filled);
                shape.setColor(0.55f, 0.30f, 0.10f, 0.75f);
                shape.rect(px, py, ts, ts);
                shape.end();
            }
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ── Caméra centrée sur le joueur ─────────────────────────────────
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

    // ── Joueur (placeholder) ─────────────────────────────────────────
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