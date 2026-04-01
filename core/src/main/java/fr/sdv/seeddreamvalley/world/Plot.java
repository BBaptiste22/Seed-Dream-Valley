package fr.sdv.seeddreamvalley.world;

import com.badlogic.gdx.graphics.Color;

public class Plot {

    public static final int STAGE_EMPTY  = 0;
    public static final int STAGE_SEEDED = 1;
    public static final int STAGE_SPROUT = 2;
    public static final int STAGE_GROWN  = 3;

    private static final float TIME_TO_SPROUT = 10f;
    private static final float TIME_TO_GROWN  = 20f;

    public final int tileX;
    public final int tileY;
    public boolean clicked = false;

    private int stage = STAGE_EMPTY;
    private float timer = 0f;

    public Plot(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public void plant() {
        stage = STAGE_SEEDED;
        timer = 0f;
        clicked = true;
    }

    // Récolte — retourne true si on peut récolter
    public boolean harvest() {
        if (stage == STAGE_GROWN) {
            stage = STAGE_EMPTY;
            clicked = false;
            timer = 0f;
            return true;
        }
        return false;
    }

    public void update(float delta) {
        if (stage == STAGE_EMPTY || stage == STAGE_GROWN) return;
        timer += delta;
        if (stage == STAGE_SEEDED && timer >= TIME_TO_SPROUT) stage = STAGE_SPROUT;
        else if (stage == STAGE_SPROUT && timer >= TIME_TO_GROWN) stage = STAGE_GROWN;
    }

    public int getStage() { return stage; }

    public Color getFillColor() {
        switch (stage) {
            case STAGE_SEEDED: return new Color(0.55f, 0.30f, 0.10f, 0.75f);
            case STAGE_SPROUT: return new Color(0.90f, 0.80f, 0.10f, 0.75f);
            case STAGE_GROWN:  return new Color(0.20f, 0.75f, 0.20f, 0.75f);
            default:           return new Color(0.85f, 0.85f, 0.85f, 0.28f);
        }
    }

    public float pixelX(int tileSize) { return tileX * tileSize; }
    public float pixelY(int tileSize) { return tileY * tileSize; }
}