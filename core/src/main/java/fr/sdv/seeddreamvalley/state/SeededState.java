package fr.sdv.seeddreamvalley.state;

import com.badlogic.gdx.graphics.Color;

import fr.sdv.seeddreamvalley.world.Plot;

public class SeededState implements PlotState {
    private static final float TIME_TO_SPROUT = 10f;
    private float timer = 0f;

    @Override
    public void update(Plot plot, float delta) {
        timer += delta;
        if (timer >= TIME_TO_SPROUT) {
            plot.setState(new SproutState());
        }
    }

    @Override
    public Color getColor() {
        return new Color(0.55f, 0.30f, 0.10f, 0.75f); // brun
    }
}