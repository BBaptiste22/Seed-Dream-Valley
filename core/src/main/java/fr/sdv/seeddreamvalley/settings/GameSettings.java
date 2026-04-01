package fr.sdv.seeddreamvalley.settings;

import com.badlogic.gdx.Input;

public class GameSettings {

    public int keyUp    = Input.Keys.W;
    public int keyDown  = Input.Keys.S;
    public int keyLeft  = Input.Keys.A;
    public int keyRight = Input.Keys.D;

    private static GameSettings instance;

    public static GameSettings get() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }

    private GameSettings() {}
}