package fr.sdv.seeddreamvalley.world;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CoinParticle {

    private float x, y;
    private float lifetime = 1.2f;
    private float elapsed  = 0f;
    private static final float SPEED = 30f;
    private boolean dead = false;

    private final Animation<TextureRegion> animation;

    public CoinParticle(float x, float y, TextureRegion fullTexture) {
        this.x = x - 8;
        this.y = y;

        // Découpe les 6 frames (chaque frame = largeur totale / 6)
        int frameW = fullTexture.getRegionWidth() / 6;
        int frameH = fullTexture.getRegionHeight();

        TextureRegion[] frames = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            frames[i] = new TextureRegion(
                fullTexture.getTexture(),
                fullTexture.getRegionX() + i * frameW,
                fullTexture.getRegionY(),
                frameW,
                frameH
            );
        }

        // 0.1f = durée par frame (6 frames × 0.1s = 0.6s par cycle)
        animation = new Animation<>(0.1f, frames);
        animation.setPlayMode(Animation.PlayMode.LOOP);
    }

    public void update(float delta) {
        elapsed += delta;
        y += SPEED * delta;
        if (elapsed >= lifetime) dead = true;
    }

    public void draw(SpriteBatch batch) {
        if (dead) return;
        float alpha = 1f - (elapsed / lifetime);
        batch.setColor(1f, 1f, 1f, alpha);
        TextureRegion frame = animation.getKeyFrame(elapsed);
        batch.draw(frame, x, y, 16, 16);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public boolean isDead() { return dead; }
}