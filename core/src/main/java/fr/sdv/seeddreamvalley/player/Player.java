package fr.sdv.seeddreamvalley.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fr.sdv.seeddreamvalley.settings.GameSettings;
import fr.sdv.seeddreamvalley.utils.Constants;

/**
 * Classe représentant le joueur dans le jeu.
 * 
 * Gère la position, l'animation et le mouvement du joueur.
 * Le joueur est contrôlé via les touches configurées dans {@link GameSettings}.
 * Les limites de la carte sont appliquées pour garder le joueur sur la carte.
 * 
 * @author Seed Dream Valley
 * @see GameSettings
 * @see Constants
 */
public class Player {

    // ── Dimensions des frames d'animation ──────────────────────────────
    /** Largeur d'une frame du sprite (en pixels). */
    private static final int FRAME_W = 18;
    
    /** Hauteur d'une frame du sprite (en pixels). */
    private static final int FRAME_H = 20;
    
    /** Nombre de colonnes dans la feuille de sprite. */
    private static final int FRAME_COLS = 2;
    
    /** Durée d'affichage de chaque frame (en secondes). */
    private static final float FRAME_DURATION = 0.15f;
    
    /** Vitesse de mouvement du joueur (pixels par seconde). */
    private static final float MOVE_SPEED = 80f;

    // ── Ressources graphiques ──────────────────────────────────────────
    /** Texture contenant la feuille de sprite du joueur. */
    private final Texture sheet;
    
    /** Animation de marche avec boucle. */
    private final Animation<TextureRegion> walkAnimation;
    
    /** Frame immobile (idle) du joueur. */
    private final TextureRegion idleFrame;

    // ── État du joueur ─────────────────────────────────────────────────
    /** Position X du joueur (coin bas-gauche du sprite). */
    private float x;
    
    /** Position Y du joueur (coin bas-gauche du sprite). */
    private float y;
    
    /** Temps écoulé dans l'animation actuelle (en secondes). */
    private float stateTime = 0f;
    
    /** Indique si le joueur est en mouvement. */
    private boolean moving = false;
    
    /** Indique si le joueur regarde vers la gauche. */
    private boolean facingLeft = false;

    /**
     * Constructeur du joueur.
     * 
     * Charge la feuille de sprite et initialise l'animation de marche.
     * 
     * @param startX Position X de départ du joueur
     * @param startY Position Y de départ du joueur
     */
    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        // Chargement de la feuille de sprite
        sheet = new Texture(Gdx.files.internal("perso.png"));
        TextureRegion[][] all = TextureRegion.split(sheet, FRAME_W, FRAME_H);

        // Extraction des frames de marche (première ligne)
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            walkFrames[i] = all[0][i];
        }

        // Création de l'animation avec boucle
        walkAnimation = new Animation<>(FRAME_DURATION, walkFrames);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);
        
        // Frame immobile
        idleFrame = all[0][0];
    }

    /**
     * Met à jour la position et l'animation du joueur.
     * 
     * Applique les mouvements basés sur les entrées clavier.
     * Limite la position du joueur aux limites de la carte.
     * Met à jour le temps d'animation.
     * 
     * @param delta Temps écoulé depuis la dernière frame (en secondes)
     */
    public void update(float delta) {
        GameSettings s = GameSettings.get();
        float speed = MOVE_SPEED * delta;
        moving = false;

        // Application des mouvements (touches de direction)
        if (Gdx.input.isKeyPressed(s.keyUp))    { y += speed; moving = true; }
        if (Gdx.input.isKeyPressed(s.keyDown))  { y -= speed; moving = true; }
        if (Gdx.input.isKeyPressed(s.keyLeft))  { x -= speed; moving = true; facingLeft = true; }
        if (Gdx.input.isKeyPressed(s.keyRight)) { x += speed; moving = true; facingLeft = false; }

        // Limitation du joueur aux limites de la carte
        float mapW = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
        float mapH = Constants.MAP_HEIGHT * Constants.TILE_SIZE;
        x = Math.max(0, Math.min(x, mapW - FRAME_W));
        y = Math.max(0, Math.min(y, mapH - FRAME_H));

        // Mise à jour du temps d'animation
        if (moving) stateTime += delta;
        else stateTime = 0f;
    }

    /**
     * Dessine le joueur à sa position courante.
     * 
     * Affiche la frame appropriée (marche ou immobile) avec la bonne orientation.
     * 
     * @param batch Batch de rendu pour dessiner le sprite
     */
    public void draw(SpriteBatch batch) {
        // Sélection de la frame appropriée
        TextureRegion frame = moving ? walkAnimation.getKeyFrame(stateTime) : idleFrame;

        // Gestion propre du flip (évite le clignotement ou reste retourné)
        if (facingLeft == frame.isFlipX()) {
            frame.flip(true, false);
        }
        
        // Dessin du sprite à sa position
        // Note: x et y représentent le coin bas-gauche du personnage
        batch.draw(frame, x, y, FRAME_W, FRAME_H);
    }

    // ── Getters et Setters pour gestion des collisions ──────────────────

    /**
     * Obtient la position X du joueur.
     * 
     * @return Position X (coin bas-gauche du sprite)
     */
    public float getX() { 
        return x; 
    }

    /**
     * Obtient la position Y du joueur.
     * 
     * @return Position Y (coin bas-gauche du sprite)
     */
    public float getY() { 
        return y; 
    }

    /**
     * Définit la position X du joueur.
     * 
     * Utilisé pour les corrections de collision.
     * 
     * @param x Nouvelle position X
     */
    public void setX(float x) { 
        this.x = x; 
    }

    /**
     * Définit la position Y du joueur.
     * 
     * Utilisé pour les corrections de collision.
     * 
     * @param y Nouvelle position Y
     */
    public void setY(float y) { 
        this.y = y; 
    }

    /**
     * Libère les ressources utilisées par le joueur.
     * 
     * Doit être appelée lors de la suppression de l'instance pour éviter les fuites mémoire.
     */
    public void dispose() {
        sheet.dispose();
    }
}