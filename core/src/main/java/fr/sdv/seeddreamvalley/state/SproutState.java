package fr.sdv.seeddreamvalley.state;

import com.badlogic.gdx.graphics.Color;

import fr.sdv.seeddreamvalley.world.Plot;

public class SproutState implements PlotState {
    private static final float TIME_TO_GROWN = 10f; // 10s supplémentaires
    private float timer = 0f;

    @Override
    public void update(Plot plot, float delta) {
        timer += delta;
        if (timer >= TIME_TO_GROWN) {
            plot.setState(new GrownState());
        }
    }

    @Override
    public Color getColor() {
        return new Color(0.90f, 0.80f, 0.10f, 0.75f); // jaune
    }
}