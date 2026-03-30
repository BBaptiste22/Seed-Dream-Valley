package fr.sdv.seeddreamvalley;

import com.badlogic.gdx.Game;
import fr.sdv.seeddreamvalley.screens.MenuScreen;

public class Main extends Game {

    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}