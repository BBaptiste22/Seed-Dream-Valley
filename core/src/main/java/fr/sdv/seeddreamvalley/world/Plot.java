package fr.sdv.seeddreamvalley.world;

import com.badlogic.gdx.graphics.Color;

public class Plot {

    public final int tileX;
    public final int tileY;
    public boolean clicked = false;

    public Plot(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public float pixelX(int tileSize) { return tileX * tileSize; }
    public float pixelY(int tileSize) { return tileY * tileSize; }

    // Couleur de remplissage selon l'état
    public Color getFillColor() {
        return clicked
            ? new Color(0.55f, 0.30f, 0.10f, 0.75f) // brun = cliqué
            : new Color(0.85f, 0.85f, 0.85f, 0.28f); // gris = adjacent
    }
}