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
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.game.Game;
import fr.sdv.seeddreamvalley.world.CoinParticle;
import fr.sdv.seeddreamvalley.world.Plot;

/**
 * Écran de jeu — responsable uniquement du rendu.
 * La logique est déléguée à {@link Game}.
 */
public class GameScreen extends ScreenAdapter {

    private final Main             game;
    private OrthographicCamera     camera;
    private ScreenViewport         viewport;
    private OrthogonalTiledMapRenderer mapRenderer;

    // ── Rendu ────────────────────────────────────────────────────────
    private SpriteBatch        batch;
    private ShapeRenderer      shape;
    private BitmapFont         font;
    private GlyphLayout        layout;
    private Texture            coinTexture;
    private TextureRegion      coinRegion;
    private OrthographicCamera hudCamera;

    // ── Logique ──────────────────────────────────────────────────────
    private Game gameLogic;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Rendu
        batch       = new SpriteBatch();
        shape       = new ShapeRenderer();
        font        = new BitmapFont();
        layout      = new GlyphLayout();
        coinTexture = new Texture(Gdx.files.internal("piece.png"));
        coinRegion  = new TextureRegion(coinTexture);

        font.getData().setScale(1.4f);
        font.setColor(Color.YELLOW);

        camera   = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Logique
        gameLogic   = new Game(coinRegion);
        mapRenderer = new OrthogonalTiledMapRenderer(gameLogic.getMap());
    }

    @Override
    public void render(float delta) {
        // ── Logique ──────────────────────────────────────────────────
        gameLogic.update(delta, camera);

        // ── Caméra ───────────────────────────────────────────────────
        camera.position.set(
            gameLogic.getPlayer().getX(),
            gameLogic.getPlayer().getY(), 0);
        camera.update();

        // ── Clear ────────────────────────────────────────────────────
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ── Map ──────────────────────────────────────────────────────
        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        // ── Parcelles ────────────────────────────────────────────────
        drawPlots();

        // ── Particules + joueur ──────────────────────────────────────
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (CoinParticle cp : gameLogic.getParticles()) cp.draw(batch);
        gameLogic.getPlayer().draw(batch);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // ── HUD ──────────────────────────────────────────────────────
        drawHUD();
        drawPopup();
    }

    /** Dessine les parcelles colorées. */
    private void drawPlots() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape.setProjectionMatrix(camera.combined);
        for (Plot p : gameLogic.getPlots()) {
            if (p.getStage() != Plot.STAGE_EMPTY) {
                shape.begin(ShapeRenderer.ShapeType.Filled);
                shape.setColor(p.getFillColor());
                shape.rect(p.tileX * 16, p.tileY * 16, 16, 16);
                shape.end();
            }
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** Dessine le HUD pièces. */
    private void drawHUD() {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        int frameW = coinTexture.getWidth() / 6;
        batch.draw(coinTexture,
            12, Gdx.graphics.getHeight() - 36,
            24, 24, 0, 0,
            frameW, coinTexture.getHeight(),
            false, false);

        String text = "x " + gameLogic.getCoins();
        layout.setText(font, text);
        font.draw(batch, text, 42, Gdx.graphics.getHeight() - 10);
        batch.end();
    }

    /** Dessine la popup de déblocage du pont. */
    private void drawPopup() {
        float popupTimer = gameLogic.getPopupTimer();
        if (popupTimer <= 0) return;

        float alpha = Math.min(1f, popupTimer);
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float bw = 260f, bh = 50f;
        float bx = sw / 2f - bw / 2f;
        float by = sh / 2f - bh / 2f;

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

        String msg = "Pont debloque !";
        font.getData().setScale(1.4f);
        font.setColor(1f, 0.9f, 0.3f, alpha);
        layout.setText(font, msg);
        batch.begin();
        font.draw(batch, msg,
            sw / 2f - layout.width / 2f,
            by + bh / 2f + layout.height / 2f);
        batch.end();
        font.setColor(Color.YELLOW);
    }

    @Override
    public void resize(int w, int h) {
        viewport.update(w, h);
        hudCamera.setToOrtho(false, w, h);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        batch.dispose();
        shape.dispose();
        font.dispose();
        coinTexture.dispose();
        gameLogic.dispose();
    }
}