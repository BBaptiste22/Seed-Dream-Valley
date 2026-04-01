package fr.sdv.seeddreamvalley.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import fr.sdv.seeddreamvalley.settings.GameSettings;
import fr.sdv.seeddreamvalley.utils.Constants;

public class Player {

    private static final int FRAME_W    = 18;
    private static final int FRAME_H    = 20;
    private static final int FRAME_COLS = 2;
    private static final float FRAME_DURATION = 0.15f;
    private static final float MOVE_SPEED = 80f;

    private final Texture sheet;
    private final Animation<TextureRegion> walkAnimation;
    private final TextureRegion idleFrame;

    private float x, y;
    private float stateTime = 0f;
    private boolean moving = false;
    private boolean facingLeft = false;

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;

        sheet = new Texture(Gdx.files.internal("perso.png"));
        TextureRegion[][] all = TextureRegion.split(sheet, FRAME_W, FRAME_H);

        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            walkFrames[i] = all[0][i];
        }

        walkAnimation = new Animation<>(FRAME_DURATION, walkFrames);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);
        idleFrame = all[0][0];
    }

    public void update(float delta) {
        GameSettings s = GameSettings.get();
        float speed = MOVE_SPEED * delta;
        moving = false;

        // On applique les mouvements (les collisions seront gérées par le Screen via setX/setY)
        if (Gdx.input.isKeyPressed(s.keyUp))    { y += speed; moving = true; }
        if (Gdx.input.isKeyPressed(s.keyDown))  { y -= speed; moving = true; }
        if (Gdx.input.isKeyPressed(s.keyLeft))  { x -= speed; moving = true; facingLeft = true; }
        if (Gdx.input.isKeyPressed(s.keyRight)) { x += speed; moving = true; facingLeft = false; }

        // Rester dans la map
        float mapW = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
        float mapH = Constants.MAP_HEIGHT * Constants.TILE_SIZE;
        x = Math.max(0, Math.min(x, mapW - FRAME_W));
        y = Math.max(0, Math.min(y, mapH - FRAME_H));

        if (moving) stateTime += delta;
        else stateTime = 0f;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = moving ? walkAnimation.getKeyFrame(stateTime) : idleFrame;

        // Gestion propre du flip (évite que le sprite ne clignote ou ne reste retourné)
        if (!facingLeft && !frame.isFlipX()) frame.flip(true, false);
        else if (facingLeft && frame.isFlipX()) frame.flip(true, false);

        // On dessine. Note: x et y représentent ici le coin bas-gauche du perso
        batch.draw(frame, x, y, FRAME_W, FRAME_H);
    }

    // --- Getters et Setters nécessaires pour les collisions ---
    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    public void dispose() {
        sheet.dispose();
    }
}