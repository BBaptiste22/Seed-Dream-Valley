package fr.sdv.seeddreamvalley.state;

import com.badlogic.gdx.graphics.Color;

import fr.sdv.seeddreamvalley.world.Plot;

public class GrownState implements PlotState {
    @Override
    public void update(Plot plot, float delta) {} // prêt à récolter, rien à faire

    @Override
    public Color getColor() {
        return new Color(0.20f, 0.75f, 0.20f, 0.75f); // vert
    }
}