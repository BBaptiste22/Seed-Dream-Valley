package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.player.Player;
import fr.sdv.seeddreamvalley.world.CoinParticle;
import fr.sdv.seeddreamvalley.world.Plot;

import java.util.ArrayList;
import java.util.Iterator;
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

    // piece
    private int coins = 0;
    private Texture coinTexture;
    private TextureRegion coinRegion;
    private final List<CoinParticle> particles = new ArrayList<>();
    private BitmapFont font;
    private GlyphLayout layout;

    private boolean bridgeUnlocked = false;
    private float popupTimer = 0f;
    private static final float POPUP_DURATION = 3f;

    // camera
    private OrthographicCamera hudCamera;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        map         = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        batch       = new SpriteBatch();
        shape       = new ShapeRenderer();

        camera   = new OrthographicCamera();
        viewport = new ScreenViewport(camera);

        // Caméra fixe
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // chargement piece
        coinTexture = new Texture(Gdx.files.internal("piece.png"));
        coinRegion  = new TextureRegion(coinTexture);

        font = new BitmapFont();
        font.getData().setScale(1.4f);
        font.setColor(Color.YELLOW);
        layout = new GlyphLayout();

        initPlots();

        player = new Player(496, 224);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
    }

    @Override
    public void render(float delta) {
        // collision
        float oldX = player.getX();
        float oldY = player.getY();
        player.update(delta);
        if (isCellBlocked(player.getX(), oldY))  player.setX(oldX);
        if (isCellBlocked(player.getX(), player.getY())) player.setY(oldY);

        // update parcelles
        for (Plot p : plots) p.update(delta);

        // update particules
        Iterator<CoinParticle> it = particles.iterator();
        while (it.hasNext()) {
            CoinParticle cp = it.next();
            cp.update(delta);
            if (cp.isDead()) it.remove();
        }

        // déblocage pont
        if (!bridgeUnlocked && coins >= 25) {
            bridgeUnlocked = true;
            popupTimer = POPUP_DURATION;
            map.getLayers().get("Barrière_pont").setVisible(false);
        }

        // timer popup
        if (popupTimer > 0) popupTimer -= delta;

        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        drawPlots();

        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (CoinParticle cp : particles) cp.draw(batch);
        player.draw(batch);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        drawHUD();
        drawPopup(); // ← ajouté

        if (Gdx.input.isButtonJustPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
            handlePlotClick();
        }
    }

    // piece + compteur
    private void drawHUD() {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        
        int frameW = coinTexture.getWidth() / 6;
        batch.draw(coinTexture,
            12, Gdx.graphics.getHeight() - 36,
            24, 24,           // taille affichée
            0, 0,             // position dans la texture
            frameW, coinTexture.getHeight(), // taille d'une frame
            false, false);

        
        String text = "x " + coins;
        layout.setText(font, text);
        font.draw(batch, text, 42, Gdx.graphics.getHeight() - 10);

        batch.end();
    }

    // clic 
    private void handlePlotClick() {
        Vector3 m = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(m);

        int clickTx  = (int)(m.x / 16);
        int clickTy  = (int)(m.y / 16);
        int playerTx = (int)(player.getX() / 16);
        int playerTy = (int)(player.getY() / 16);

        int distX = Math.abs(clickTx - playerTx);
        int distY = Math.abs(clickTy - playerTy);

        if (distX <= 2 && distY <= 2) {
            for (Plot p : plots) {
                if (p.tileX == clickTx && p.tileY == clickTy) {

                    if (p.getStage() == Plot.STAGE_GROWN) {
                        // Récolte 
                        if (p.harvest()) {
                            coins++;

                            float px = p.tileX * 16;
                            float py = p.tileY * 16;
                            particles.add(new CoinParticle(px + 8, py, coinRegion));
                        }
                    } else if (p.getStage() == Plot.STAGE_EMPTY) {
                        // Planter 
                        p.plant();
                    }
                    break;
                }
            }
        }
    }

    // Collision 
    private boolean isCellBlocked(float x, float y) {
        float hitW = 6f, hitH = 4f, offX = 6f, offY = 2f;
        float[][] points = {
            {x + offX,        y + offY},
            {x + offX + hitW, y + offY},
            {x + offX,        y + offY + hitH},
            {x + offX + hitW, y + offY + hitH}
        };

        for (float[] pt : points) {
            int tx = (int)(pt[0] / 16);
            int ty = (int)(pt[1] / 16);

            // Exclure le layer "Barrière_pont" des collisions si le pont est déverrouillé
            // Quand le pont est déverrouillé, retirer aussi le layer "Pont" pour le rendre totalement traversable
            String[] layers = bridgeUnlocked ? 
                new String[]{"Colision"} : 
                new String[]{"Colision", "Pont", "Barrière_pont"};
                
            for (String name : layers) {
                TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(name);
                if (layer == null) continue;
                TiledMapTileLayer.Cell cell = layer.getCell(tx, ty);
                if (cell != null && cell.getTile() != null) {
                    int id = cell.getTile().getId();
                    if (id == 13 || id == 21 || id == 24 || id == 22 || id == 120) continue;
                    return true;
                }
            }
        }
        return false;
    }

    // Parcelles 
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
    // Changement de couleur
    private void drawPlots() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        for (Plot p : plots) {
            if (p.getStage() != Plot.STAGE_EMPTY) {
                shape.begin(ShapeRenderer.ShapeType.Filled);
                shape.setColor(p.getFillColor());
                shape.rect(p.tileX * 16, p.tileY * 16, 16, 16);
                shape.end();
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawPopup() {
        if (popupTimer <= 0) return;

        float alpha = Math.min(1f, popupTimer); // fade out sur la dernière seconde

        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);

        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float bw = 260f, bh = 50f;
        float bx = sw / 2f - bw / 2f;
        float by = sh / 2f - bh / 2f;

        // Fond de la popup
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shape.setProjectionMatrix(hudCamera.combined);
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0.15f, 0.10f, 0.05f, 0.85f * alpha);
        shape.rect(bx, by, bw, bh);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0.95f, 0.78f, 0.35f, alpha);
        shape.rect(bx, by, bw, bh);
        shape.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Texte
        String msg = "🌉 Parcelle débloquée !";
        font.getData().setScale(1.4f);
        font.setColor(1f, 0.9f, 0.3f, alpha);
        layout.setText(font, msg);
        batch.begin();
        font.draw(batch, msg, sw / 2f - layout.width / 2f, by + bh / 2f + layout.height / 2f);
        batch.end();

        // Remet la couleur normale
        font.setColor(Color.YELLOW);
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        shape.dispose();
        batch.dispose();
        player.dispose();
        coinTexture.dispose();
        font.dispose();
    }
}