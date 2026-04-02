package fr.sdv.seeddreamvalley.state;

import com.badlogic.gdx.graphics.Color;

import fr.sdv.seeddreamvalley.world.Plot;

public class EmptyState implements PlotState {
    @Override
    public void update(Plot plot, float delta) {} // rien à faire

    @Override
    public Color getColor() {
        return new Color(0.85f, 0.85f, 0.85f, 0.28f);
    }
}