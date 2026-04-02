package fr.sdv.seeddreamvalley.state;

import fr.sdv.seeddreamvalley.world.Plot;

public interface PlotState {
    void update(Plot plot, float delta);
    com.badlogic.gdx.graphics.Color getColor();
}