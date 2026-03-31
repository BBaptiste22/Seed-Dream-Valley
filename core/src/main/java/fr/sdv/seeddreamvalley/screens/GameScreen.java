package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.utils.Constants;
import fr.sdv.seeddreamvalley.world.Plot;
import fr.sdv.seeddreamvalley.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends ScreenAdapter {

    // ── Référence principale ─────────────────────────────────────────
    private final Main game;

    // ── Rendu ────────────────────────────────────────────────────────
    private OrthographicCamera camera;
    private final ScreenViewport viewport;
    private ShapeRenderer shape;
    private SpriteBatch batch;

    // ── Map Tiled ────────────────────────────────────────────────────
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // ── Joueur ───────────────────────────────────────────────────────
    private Player player;

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
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public void show() {
        map         = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        shape       = new ShapeRenderer();
        batch       = new SpriteBatch();

        initPlots();

        float mapPixelW = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
        float mapPixelH = Constants.MAP_HEIGHT * Constants.TILE_SIZE;

        player = new Player(mapPixelW / 2f, mapPixelH / 2f);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
        camera.zoom = 1f;
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();
    }

    // ────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        player.update(delta);
        updateNearestPlot();
        updateCamera();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        drawPlots();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        batch.end();

        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            handlePlotClick();
        }
    }

    // ── Définition des parcelles ─────────────────────────────────────
    private void initPlots() {
        com.badlogic.gdx.maps.tiled.TiledMapTileLayer layer =
            (com.badlogic.gdx.maps.tiled.TiledMapTileLayer) map.getLayers().get("Layer_1");

        int[] parcelTileIds = {21};

        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell == null) continue;

                int tileId = cell.getTile().getId();
                for (int id : parcelTileIds) {
                    if (tileId == id) {
                        plots.add(new Plot(x, y));
                        break;
                    }
                }
            }
        }
    }

    // ── Clic sur une parcelle ────────────────────────────────────────
    private void handlePlotClick() {
        int ts = Constants.TILE_SIZE;

        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouse);

        int clickTileX = (int)(mouse.x / ts);
        int clickTileY = (int)(mouse.y / ts);

        int playerTileX = (int)(player.getX() / ts);
        int playerTileY = (int)(player.getY() / ts);

        int dx = Math.abs(clickTileX - playerTileX);
        int dy = Math.abs(clickTileY - playerTileY);
        if (dx > 3 || dy > 3) return;

        for (Plot plot : plots) {
            if (plot.tileX == clickTileX && plot.tileY == clickTileY) {
                plot.clicked = !plot.clicked;
                return;
            }
        }
    }

    // ── Parcelle la plus proche ──────────────────────────────────────
    private void updateNearestPlot() {
        int ts = Constants.TILE_SIZE;

        int playerTileX = (int)(player.getX() / ts);
        int playerTileY = (int)(player.getY() / ts);

        nearestPlot = null;
        float bestDist = Float.MAX_VALUE;

        for (Plot plot : plots) {
            int dx = Math.abs(plot.tileX - playerTileX);
            int dy = Math.abs(plot.tileY - playerTileY);

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

    // ── Dessine les parcelles ────────────────────────────────────────
    private void drawPlots() {
        int ts = Constants.TILE_SIZE;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shape.setProjectionMatrix(camera.combined);

        for (Plot plot : plots) {
            if (plot.clicked) {
                shape.begin(ShapeRenderer.ShapeType.Filled);
                shape.setColor(0.55f, 0.30f, 0.10f, 0.75f);
                shape.rect(plot.pixelX(ts), plot.pixelY(ts), ts, ts);
                shape.end();
            }
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ── Caméra centrée sur le joueur ─────────────────────────────────
    private void updateCamera() {
        camera.position.set(player.getX(), player.getY(), 0);
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
        batch.dispose();
        player.dispose();
    }
}