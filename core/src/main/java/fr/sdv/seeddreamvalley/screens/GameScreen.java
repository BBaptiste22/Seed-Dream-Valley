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
import com.badlogic.gdx.utils.viewport.FitViewport;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.game.Game;
import fr.sdv.seeddreamvalley.world.CoinParticle;
import fr.sdv.seeddreamvalley.world.Plot;

/**
 * Écran de jeu principal — responsable uniquement du rendu des éléments.
 * 
 * Cette classe gère l'affichage de la carte, des personnages, des particules, du HUD et des popups.
 * La logique du jeu est déléguée à la classe {@link Game}.
 * Elle étend {@link ScreenAdapter} de LibGDX pour gérer le cycle de vie de l'écran.
 * 
 * @author Seed Dream Valley
 * @see Game
 * @see MenuScreen
 */
public class GameScreen extends ScreenAdapter {

    // ── Dimensions virtuelles de la caméra ─────────────────────────────
    /** Largeur virtuelle de la caméra (en pixels). Augmente pour plus de zoom arrière. */
    private static final float VIEWPORT_WIDTH = 640f;
    
    /** Hauteur virtuelle de la caméra (en pixels). Augmente pour plus de zoom arrière. */
    private static final float VIEWPORT_HEIGHT = 360f;

    // ── Références principales ────────────────────────────────────────
    /** Référence à l'application principale. */
    private final Main game;

    // ── Caméra et viewport ─────────────────────────────────────────────
    /** Caméra orthographique suivant le joueur. */
    private OrthographicCamera camera;
    
    /** FitViewport pour adapter la map à l'écran avec zoom. */
    private FitViewport viewport;

    // ── Rendu ─────────────────────────────────────────────────────────
    /** Batch pour dessiner les sprites et textures. */
    private SpriteBatch batch;
    
    /** Renderer pour les formes géométriques (rectangles, lignes). */
    private ShapeRenderer shape;
    
    /** Police de caractères pour le texte. */
    private BitmapFont font;
    
    /** Layout pour calculer les dimensions du texte. */
    private GlyphLayout layout;
    
    /** Texture de la pièce de monnaie. */
    private Texture coinTexture;
    
    /** Région de texture pour la pièce (optimisation). */
    private TextureRegion coinRegion;
    
    /** Caméra orthographique pour l'affichage du HUD (écran fixe). */
    private OrthographicCamera hudCamera;

    // ── Logique ───────────────────────────────────────────────────────
    /** Instance de la logique du jeu. */
    private Game gameLogic;
    
    /** Renderer pour la carte tiled. */
    private OrthogonalTiledMapRenderer mapRenderer;

    /**
     * Constructeur du GameScreen.
     * 
     * @param game Référence à l'application principale
     */
    public GameScreen(Main game) {
        this.game = game;
    }

    /**
     * Initialise les ressources de l'écran.
     * 
     * Cette méthode est appelée une seule fois au chargement de l'écran.
     * Elle crée les objets de rendu (batch, shapes, font) et initialise le système de jeu.
     */
    @Override
    public void show() {
        // Initialisation du rendu
        batch       = new SpriteBatch();
        shape       = new ShapeRenderer();
        font        = new BitmapFont();
        layout      = new GlyphLayout();
        coinTexture = new Texture(Gdx.files.internal("piece.png"));
        coinRegion  = new TextureRegion(coinTexture);

        font.getData().setScale(1.4f);
        font.setColor(Color.YELLOW);

        // Initialisation de la caméra de jeu avec FitViewport
        // FitViewport zoom automatiquement la map pour remplir l'écran sans distorsion
        camera   = new OrthographicCamera();
        viewport = new FitViewport(VIEWPORT_WIDTH, VIEWPORT_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);

        // Initialisation de la caméra HUD (écran fixe)
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Initialisation de la logique du jeu
        gameLogic   = new Game(coinRegion);
        mapRenderer = new OrthogonalTiledMapRenderer(gameLogic.getMap());
    }

    /**
     * Met à jour et affiche l'écran à chaque frame.
     * 
     * @param delta Temps écoulé depuis la dernière frame (en secondes)
     */
    @Override
    public void render(float delta) {
        // Mise à jour de la logique du jeu
        gameLogic.update(delta, camera);

        // Mise à jour de la caméra pour suivre le joueur
        camera.position.set(
            gameLogic.getPlayer().getX(),
            gameLogic.getPlayer().getY(), 0);
        camera.update();

        // Nettoyage de l'écran avec une couleur grise foncée
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Affichage de la carte tiled
        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Affichage des parcelles colorées
        drawPlots();

        // Affichage des particules et du joueur
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        for (CoinParticle cp : gameLogic.getParticles()) cp.draw(batch);
        gameLogic.getPlayer().draw(batch);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Affichage du HUD et des popups
        drawHUD();
        drawPopup();
    }

    /**
     * Dessine les parcelles colorées sur la carte.
     * 
     * Affiche un rectangle coloré pour chaque parcelle qui n'est pas vide,
     * avec une transparence pour un bon rendu visuel.
     */
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

    /**
     * Dessine le HUD affichant le nombre de pièces du joueur.
     * 
     * Affiche une icône de pièce et le nombre total de pièces collectées.
     */
    private void drawHUD() {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Affichage de l'icône de pièce
        int frameW = coinTexture.getWidth() / 6;
        batch.draw(coinTexture,
            12, Gdx.graphics.getHeight() - 36,
            24, 24, 0, 0,
            frameW, coinTexture.getHeight(),
            false, false);

        // Affichage du texte avec le nombre de pièces
        String text = "x " + gameLogic.getCoins();
        layout.setText(font, text);
        font.draw(batch, text, 42, Gdx.graphics.getHeight() - 10);
        batch.end();
    }

    /**
     * Dessine la popup de déblocage du pont.
     * 
     * Cette popup s'affiche au centre de l'écran lorsque le joueur déverrouille le pont.
     * Elle s'efface progressivement avec un effet de transparence.
     */
    private void drawPopup() {
        float popupTimer = gameLogic.getPopupTimer();
        if (popupTimer <= 0) return;

        float alpha = Math.min(1f, popupTimer);
        float sw = Gdx.graphics.getWidth();
        float sh = Gdx.graphics.getHeight();
        float bw = 260f, bh = 50f;
        float bx = sw / 2f - bw / 2f;
        float by = sh / 2f - bh / 2f;

        // Affichage du rectangle de fond avec bordure
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

        // Affichage du texte de la popup
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

    /**
     * Gère le redimensionnement de la fenêtre.
     * 
     * @param w Nouvelle largeur de la fenêtre
     * @param h Nouvelle hauteur de la fenêtre
     */
    @Override
    public void resize(int w, int h) {
        viewport.update(w, h, false);
        hudCamera.setToOrtho(false, w, h);
    }

    /**
     * Libère toutes les ressources utilisées par l'écran.
     * 
     * Cette méthode est appelée lorsque l'écran est fermé ou supprimé.
     * Elle doit être appelée pour éviter les fuites mémoire.
     */
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