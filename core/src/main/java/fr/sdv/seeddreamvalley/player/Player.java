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
    private static final int FRAME_ROWS = 1;
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

        // Découpe toutes les frames
        TextureRegion[][] all = TextureRegion.split(sheet, FRAME_W, FRAME_H);

        // Prend la première ligne comme animation de marche
        TextureRegion[] walkFrames = new TextureRegion[FRAME_COLS];
        for (int i = 0; i < FRAME_COLS; i++) {
            walkFrames[i] = all[0][i];
        }

        walkAnimation = new Animation<>(FRAME_DURATION, walkFrames);
        walkAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Frame idle = première frame
        idleFrame = all[0][0];
    }

    public void update(float delta) {
        GameSettings s = GameSettings.get();
        float speed = MOVE_SPEED * delta;
        moving = false;

        if (Gdx.input.isKeyPressed(s.keyUp))    { y += speed; moving = true; }
        if (Gdx.input.isKeyPressed(s.keyDown))  { y -= speed; moving = true; }
        if (Gdx.input.isKeyPressed(s.keyLeft))  { x -= speed; moving = true; facingLeft = true; }
        if (Gdx.input.isKeyPressed(s.keyRight)) { x += speed; moving = true; facingLeft = false; }

        // Clamp dans la map
        float mapW = Constants.MAP_WIDTH  * Constants.TILE_SIZE;
        float mapH = Constants.MAP_HEIGHT * Constants.TILE_SIZE;
        x = Math.max(0, Math.min(x, mapW));
        y = Math.max(0, Math.min(y, mapH));

        if (moving) stateTime += delta;
        else stateTime = 0f;
    }

    public void draw(SpriteBatch batch) {
        TextureRegion frame = moving ? walkAnimation.getKeyFrame(stateTime) : idleFrame;

        // Retourne le sprite si on va à gauche
        if (facingLeft && frame.isFlipX()) frame.flip(true, false);
        if (!facingLeft && !frame.isFlipX()) frame.flip(true, false);

        batch.draw(frame, x - FRAME_W / 2f, y - FRAME_H / 2f, FRAME_W, FRAME_H);
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void dispose() {
        sheet.dispose();
    }
}