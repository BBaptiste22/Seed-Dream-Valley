package fr.sdv.seeddreamvalley.settings;

import com.badlogic.gdx.Input;

public class GameSettings {

    // Touches de déplacement (valeurs par défaut ZQSD)
    public int keyUp    = Input.Keys.Z;
    public int keyDown  = Input.Keys.S;
    public int keyLeft  = Input.Keys.Q;
    public int keyRight = Input.Keys.D;

    // Zoom caméra (1f = normal)
    public float zoom = 1f;
    public static final float ZOOM_MIN = 0.25f;
    public static final float ZOOM_MAX = 4f;

    // Singleton simple
    private static GameSettings instance;
    public static GameSettings get() {
        if (instance == null) instance = new GameSettings();
        return instance;
    }
    private GameSettings() {}
}