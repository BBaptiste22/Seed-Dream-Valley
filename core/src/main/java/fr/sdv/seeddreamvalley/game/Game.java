package fr.sdv.seeddreamvalley.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import fr.sdv.seeddreamvalley.player.Player;
import fr.sdv.seeddreamvalley.world.CoinParticle;
import fr.sdv.seeddreamvalley.world.Plot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Logique principale du jeu.
 * Gère le joueur, les parcelles, les pièces et le pont.
 */
public class Game {

    // ── Constantes ───────────────────────────────────────────────────
    private static final int   BRIDGE_COST    = 25;
    private static final float POPUP_DURATION = 3f;
    private static final int   TILE_SIZE      = 16;
    private static final int   INTERACT_RANGE = 2;

    // ── Map ──────────────────────────────────────────────────────────
    private final TiledMap map;

    // ── Entités ──────────────────────────────────────────────────────
    private final Player              player;
    private final List<Plot>          plots     = new ArrayList<>();
    private final List<CoinParticle>  particles = new ArrayList<>();

    // ── État ─────────────────────────────────────────────────────────
    private int     coins          = 0;
    private boolean bridgeUnlocked = false;
    private float   popupTimer     = 0f;

    /**
     * Initialise la logique du jeu.
     * @param coinRegion texture de la pièce pour les particules
     */
    public Game(com.badlogic.gdx.graphics.g2d.TextureRegion coinRegion) {
        map    = new TmxMapLoader().load("map.tmx");
        player = new Player(496, 224);
        initPlots();
        this.coinRegion = coinRegion;
    }

    // Stocké pour créer les particules
    private final com.badlogic.gdx.graphics.g2d.TextureRegion coinRegion;

    /**
     * Met à jour toute la logique du jeu.
     * @param delta temps écoulé depuis la dernière frame
     * @param camera caméra du monde (pour convertir les clics)
     */
    public void update(float delta, OrthographicCamera camera) {
        updatePlayer(delta);
        updatePlots(delta);
        updateParticles(delta);
        updateBridge();
        if (popupTimer > 0) popupTimer -= delta;

        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            handlePlotClick(camera);
        }
    }

    // ── Mise à jour joueur + collision ───────────────────────────────
    private void updatePlayer(float delta) {
        float oldX = player.getX();
        float oldY = player.getY();
        player.update(delta);

        // Test collision axe X → reset X
        if (isCellBlocked(player.getX(), oldY))
            player.setX(oldX);

        // Test collision axe Y → reset Y
        if (isCellBlocked(player.getX(), player.getY()))
            player.setY(oldY);
    }

    /** Met à jour toutes les parcelles. */
    private void updatePlots(float delta) {
        for (Plot p : plots) p.update(delta);
    }

    /** Met à jour et supprime les particules mortes. */
    private void updateParticles(float delta) {
        Iterator<CoinParticle> it = particles.iterator();
        while (it.hasNext()) {
            CoinParticle cp = it.next();
            cp.update(delta);
            if (cp.isDead()) it.remove();
        }
    }

    /** Débloque le pont si assez de pièces. */
    private void updateBridge() {
        if (!bridgeUnlocked && coins >= BRIDGE_COST) {
            bridgeUnlocked = true;
            popupTimer = POPUP_DURATION;
            map.getLayers().get("Barrière_pont").setVisible(false);
        }
    }

    /** Gère le clic sur une parcelle : planter ou récolter. */
    private void handlePlotClick(OrthographicCamera camera) {
        Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(m);

        int clickTx  = (int)(m.x / TILE_SIZE);
        int clickTy  = (int)(m.y / TILE_SIZE);
        int playerTx = (int)(player.getX() / TILE_SIZE);
        int playerTy = (int)(player.getY() / TILE_SIZE);

        if (Math.abs(clickTx - playerTx) > INTERACT_RANGE) return;
        if (Math.abs(clickTy - playerTy) > INTERACT_RANGE) return;

        for (Plot p : plots) {
            if (p.tileX != clickTx || p.tileY != clickTy) continue;

            if (p.isGrown() && p.harvest()){
                coins++;
                particles.add(new CoinParticle(
                    p.tileX * TILE_SIZE + 8f,
                    p.tileY * TILE_SIZE,
                    coinRegion
                ));
            } else if (p.isEmpty()){
                p.plant();
            }
            break;
        }
    }

    /** Détecte si une position est bloquée. */
    private boolean isCellBlocked(float x, float y) {
        float hitW = 6f, hitH = 4f, offX = 6f, offY = 2f;
        float[][] points = {
            {x + offX,        y + offY},
            {x + offX + hitW, y + offY},
            {x + offX,        y + offY + hitH},
            {x + offX + hitW, y + offY + hitH}
        };

        String[] layers = bridgeUnlocked
            ? new String[]{"Colision"}
            : new String[]{"Colision", "Pont", "Barrière_pont"};

        for (float[] pt : points) {
            int tx = (int)(pt[0] / TILE_SIZE);
            int ty = (int)(pt[1] / TILE_SIZE);
            for (String name : layers) {
                TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(name);
                if (layer == null) continue;
                TiledMapTileLayer.Cell cell = layer.getCell(tx, ty);
                if (cell == null || cell.getTile() == null) continue;
                int id = cell.getTile().getId();
                if (id == 13 || id == 21 || id == 24 || id == 22 || id == 120) continue;
                return true;
            }
        }
        return false;
    }

    /** Charge les parcelles depuis le layer "Base". */
    private void initPlots() {
        TiledMapTileLayer base = (TiledMapTileLayer) map.getLayers().get("Base");
        if (base == null) return;
        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                TiledMapTileLayer.Cell c = base.getCell(x, y);
                if (c != null && c.getTile() != null && c.getTile().getId() == 21)
                    plots.add(new Plot(x, y));
            }
        }
    }

    // ── Getters pour le rendu ─────────────────────────────────────────
    public Player             getPlayer()    { return player; }
    public List<Plot>         getPlots()     { return plots; }
    public List<CoinParticle> getParticles() { return particles; }
    public TiledMap           getMap()       { return map; }
    public int                getCoins()     { return coins; }
    public float              getPopupTimer(){ return popupTimer; }

    /** Libère les ressources. */
    public void dispose() {
        map.dispose();
        player.dispose();
    }
}