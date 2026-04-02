package fr.sdv.seeddreamvalley.world;

import com.badlogic.gdx.graphics.Color;

import fr.sdv.seeddreamvalley.state.EmptyState;
import fr.sdv.seeddreamvalley.state.GrownState;
import fr.sdv.seeddreamvalley.state.PlotState;
import fr.sdv.seeddreamvalley.state.SeededState;

public class Plot {
    public final int tileX;
    public final int tileY;

    private PlotState state = new EmptyState();

    public Plot(int tileX, int tileY) {
        this.tileX = tileX;
        this.tileY = tileY;
    }

    public void setState(PlotState state) {
        this.state = state;
    }

    public void plant() {
        state = new SeededState();
    }

    public boolean harvest() {
        if (state instanceof GrownState) {
            state = new EmptyState();
            return true;
        }
        return false;
    }

    public void update(float delta) {
        state.update(this, delta);
    }

    public boolean isEmpty()  { return state instanceof EmptyState; }
    public boolean isGrown()  { return state instanceof GrownState; }

    public Color getFillColor() { return state.getColor(); }

    public float pixelX(int tileSize) { return tileX * tileSize; }
    public float pixelY(int tileSize) { return tileY * tileSize; }
}