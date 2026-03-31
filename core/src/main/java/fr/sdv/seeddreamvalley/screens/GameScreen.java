package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.player.Player;
import fr.sdv.seeddreamvalley.utils.Constants;
import fr.sdv.seeddreamvalley.world.Plot;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends ScreenAdapter {

    private final Main game;
    private OrthographicCamera camera;
    private ScreenViewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shape;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    private Player player;
    private final List<Plot> plots = new ArrayList<>();

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        
        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        // 1. Initialisation des parcelles de terre
        initPlots();

        // 2. Spawn du joueur (Position sécurisée : x=100, y=100 pour éviter les murs du bord)
        player = new Player(496, 224);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
    }

    @Override
    public void render(float delta) {
        // --- LOGIQUE DE MOUVEMENT ET COLLISION ---
        float oldX = player.getX();
        float oldY = player.getY();

        player.update(delta);

        // Test de collision sur l'axe X
        if (isCellBlocked(player.getX(), oldY)) {
            player.setX(oldX);
        }
        // Test de collision sur l'axe Y
        if (isCellBlocked(player.getX(), player.getY())) {
            player.setY(oldY);
        }

        // --- CAMERA ---
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        // --- RENDU ---
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render(); // Dessine Base, Colision et Pont

        drawPlots(); // Dessine les parcelles sélectionnées

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.draw(batch);
        batch.end();

        // --- INPUT CLIC ---
        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            handlePlotClick();
        }
    }

    /**
     * Système de collision par détection de tuiles
     */
    private boolean isCellBlocked(float x, float y) {
        // On définit un petit carré de détection au niveau des pieds (8x4 pixels)
        float hitW = 6f;
        float hitH = 4f;
        float offX = 6f; // Centre par rapport au sprite de 18px
        float offY = 2f; // Tout en bas du sprite

        float[][] points = {
            {x + offX, y + offY}, 
            {x + offX + hitW, y + offY},
            {x + offX, y + offY + hitH},
            {x + offX + hitW, y + offY + hitH}
        };

        for (float[] p : points) {
            int tx = (int) (p[0] / 16); // On force 16 pixels par tuile
            int ty = (int) (p[1] / 16);

            String[] collisionLayers = {"Colision", "Pont"};
            for (String layerName : collisionLayers) {
                TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(layerName);
                if (layer != null) {
                    TiledMapTileLayer.Cell cell = layer.getCell(tx, ty);
                    if (cell != null && cell.getTile() != null) {
                        int id = cell.getTile().getId();

                        // --- LISTE BLANCHE : IDs qui ne doivent JAMAIS bloquer ---
                        // 13: Herbe, 21: Terre, 24: Chemin, 22: Bordure, 108: Ton ID problématique
                        if (id == 13 || id == 21 || id == 24 || id == 22) {
                            continue; 
                        }

                        // Si ce n'est pas un ID de sol, c'est un mur/barrière
                        return true; 
                    }
                }
            }
        }
        return false;
    }

    private void initPlots() {
        TiledMapTileLayer base = (TiledMapTileLayer) map.getLayers().get("Base");
        if (base == null) return;

        // On cherche les parcelles (ID 21) sur le calque Base
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                TiledMapTileLayer.Cell c = base.getCell(x, y);
                if (c != null && c.getTile() != null && c.getTile().getId() == 21) {
                    plots.add(new Plot(x, y));
                }
            }
        }
    }

    private void handlePlotClick() {
        // 1. Récupérer la position de la souris dans le monde
        Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(m);
        
        // 2. Convertir en coordonnées de tuiles
        int clickTx = (int)(m.x / 16);
        int clickTy = (int)(m.y / 16);
        
        // 3. Récupérer la position du joueur en tuiles
        int playerTx = (int)(player.getX() / 16);
        int playerTy = (int)(player.getY() / 16);
        
        // 4. Calculer la distance (portée de 2 cases)
        int distX = Math.abs(clickTx - playerTx);
        int distY = Math.abs(clickTy - playerTy);
        
        // 5. Si le clic est à 2 cases ou moins (verticalement, horizontalement ou diagonale)
        if (distX <= 2 && distY <= 2) {
            for (Plot p : plots) {
                if (p.tileX == clickTx && p.tileY == clickTy) {
                    p.clicked = !p.clicked;
                    break; // On a trouvé la parcelle, on peut sortir de la boucle
                }
            }
        }
    }

    private void drawPlots() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(camera.combined);
        for (Plot p : plots) {
            if (p.clicked) {
                shape.begin(ShapeRenderer.ShapeType.Filled);
                shape.setColor(0.5f, 0.3f, 0.1f, 0.5f);
                shape.rect(p.tileX * 16, p.tileY * 16, 16, 16);
                shape.end();
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void dispose() {
        map.dispose(); mapRenderer.dispose(); shape.dispose(); batch.dispose(); player.dispose();
    }
}