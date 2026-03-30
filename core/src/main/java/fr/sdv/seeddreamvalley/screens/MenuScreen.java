package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import fr.sdv.seeddreamvalley.Main;
import fr.sdv.seeddreamvalley.settings.GameSettings;

public class MenuScreen extends ScreenAdapter {

    private final Main game;
    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final SpriteBatch batch;
    private final ShapeRenderer shape;
    private final BitmapFont fontTitle;
    private final BitmapFont fontLabel;
    private final BitmapFont fontKey;
    private final GlyphLayout layout;

    private static final float W = 800f;
    private static final float H = 600f;

    // Palette
    private static final Color COL_BG          = new Color(0.18f, 0.12f, 0.08f, 1f);
    private static final Color COL_PANEL       = new Color(0.35f, 0.22f, 0.12f, 1f);
    private static final Color COL_PANEL_DARK  = new Color(0.22f, 0.13f, 0.07f, 1f);
    private static final Color COL_PANEL_LIGHT = new Color(0.42f, 0.27f, 0.15f, 1f);
    private static final Color COL_GOLD         = new Color(0.95f, 0.78f, 0.35f, 1f);
    private static final Color COL_GOLD_DARK    = new Color(0.65f, 0.50f, 0.15f, 1f);
    private static final Color COL_CREAM        = new Color(0.97f, 0.92f, 0.75f, 1f);
    private static final Color COL_BTN          = new Color(0.42f, 0.68f, 0.22f, 1f);
    private static final Color COL_BTN_LIGHT    = new Color(0.55f, 0.82f, 0.30f, 1f);
    private static final Color COL_BTN_DARK     = new Color(0.25f, 0.42f, 0.10f, 1f);
    private static final Color COL_KEY_BG       = new Color(0.25f, 0.16f, 0.09f, 1f);
    private static final Color COL_KEY_HOVER    = new Color(0.38f, 0.24f, 0.13f, 1f);
    private static final Color COL_KEY_ACTIVE   = new Color(0.70f, 0.42f, 0.08f, 1f);
    private static final Color COL_SLIDER_BG    = new Color(0.20f, 0.12f, 0.06f, 1f);

    private static final float BTN_W = 240f;
    private static final float BTN_H = 56f;
    private static final float BTN_X = W / 2f - BTN_W / 2f;
    private static final float BTN_Y = H - 145f;

    private static final float KEY_START_Y = H - 250f;
    private static final float KEY_ROW_H   = 48f;
    private static final float KEY_LBL_X   = 110f;
    private static final float KEY_BTN_X   = 380f;
    private static final float KEY_BTN_W   = 180f;
    private static final float KEY_BTN_H   = 34f;
    private static final String[] KEY_LABELS = {"Haut", "Bas", "Gauche", "Droite"};

    private static final float SLD_X = 190f;
    private static final float SLD_Y = 108f;
    private static final float SLD_W = 420f;
    private static final float SLD_H = 12f;

    private int remappingIndex = -1;
    private boolean draggingZoom = false;

    public MenuScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        shape = new ShapeRenderer();
        layout = new GlyphLayout();

        fontTitle = new BitmapFont();
        fontTitle.getData().setScale(3.2f);
        fontLabel = new BitmapFont();
        fontLabel.getData().setScale(1.5f);
        fontKey = new BitmapFont();
        fontKey.getData().setScale(1.35f);
    }

    @Override
    public void render(float delta) {
        handleInput();
        Gdx.gl.glClearColor(COL_BG.r, COL_BG.g, COL_BG.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        shape.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        drawPanel();
        drawTitle();
        drawStartButton();
        drawDivider(KEY_START_Y + 42f, "Touches de déplacement");
        drawKeysSection();
        drawDivider(SLD_Y + 80f, "Zoom caméra");
        drawZoomSection();

        if (remappingIndex >= 0) drawRemappingOverlay();
    }

    private void drawPanel() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.6f);
        shape.rect(38, 24, W - 72, H - 58);
        shape.setColor(COL_PANEL_DARK);
        shape.rect(32, 30, W - 64, H - 60);
        shape.setColor(COL_PANEL_LIGHT);
        shape.rect(36, 34, W - 72, H - 68);
        shape.setColor(COL_PANEL);
        shape.rect(40, 38, W - 80, H - 76);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(0f, 0f, 0f, 0.10f);
        for (int i = 0; i < 16; i++) {
            float yy = 42f + i * ((H - 84f) / 16f);
            shape.line(40, yy, W - 40, yy);
        }
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(COL_GOLD_DARK);
        shape.rect(46, 44, W - 92, H - 88);
        shape.end();
    }

    private void drawTitle() {
        String title = "Seed Dream Valley";
        fontTitle.getData().setScale(3.2f);
        layout.setText(fontTitle, title);
        float tx = W / 2f - layout.width / 2f;
        float ty = H - 48f;

        batch.begin();
        fontTitle.setColor(0f, 0f, 0f, 0.5f);
        fontTitle.draw(batch, title, tx + 3, ty - 3);
        fontTitle.setColor(COL_GOLD);
        fontTitle.draw(batch, title, tx, ty);
        batch.end();

        float lineY = ty - layout.height - 8f;
        float lineX1 = 56f;
        float lineX2 = tx - 14f;
        float lineX3 = tx + layout.width + 14f;
        float lineX4 = W - 56f;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(COL_GOLD);
        shape.rect(lineX1, lineY + 2, lineX2 - lineX1, 3);
        drawDiamond(lineX2, lineY + 3.5f, 7f);
        drawDiamond(lineX3, lineY + 3.5f, 7f);
        shape.rect(lineX3 + 8, lineY + 2, lineX4 - lineX3 - 8, 3);
        shape.end();
    }

    private void drawDiamond(float cx, float cy, float r) {
        shape.triangle(cx, cy + r, cx + r, cy, cx, cy - r);
        shape.triangle(cx, cy + r, cx - r, cy, cx, cy - r);
    }

    private void drawDivider(float y, String label) {
        fontLabel.getData().setScale(1.5f);
        layout.setText(fontLabel, label);
        float lx = KEY_LBL_X;
        batch.begin();
        fontLabel.setColor(COL_GOLD);
        fontLabel.draw(batch, label, lx, y);
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(COL_GOLD_DARK);
        shape.rect(lx, y - layout.height - 2, layout.width + 20, 2);
        shape.end();
    }

    private void drawStartButton() {
        float mx = mx(), my = my();
        boolean hover = over(mx, my, BTN_X, BTN_Y, BTN_W, BTN_H);

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.45f);
        shape.rect(BTN_X + 5, BTN_Y - 5, BTN_W, BTN_H);
        shape.setColor(COL_BTN_DARK);
        shape.rect(BTN_X, BTN_Y - 4, BTN_W, BTN_H);
        shape.setColor(hover ? COL_BTN_LIGHT : COL_BTN);
        shape.rect(BTN_X, BTN_Y, BTN_W, BTN_H);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(COL_BTN_DARK);
        shape.rect(BTN_X, BTN_Y, BTN_W, BTN_H);
        shape.end();

        String label = "▶   START";
        fontLabel.getData().setScale(1.8f);
        layout.setText(fontLabel, label);
        batch.begin();
        fontLabel.setColor(0f, 0f, 0f, 0.4f);
        fontLabel.draw(batch, label, BTN_X + BTN_W / 2f - layout.width / 2f + 2, BTN_Y + BTN_H / 2f + layout.height / 2f - 2);
        fontLabel.setColor(Color.WHITE);
        fontLabel.draw(batch, label, BTN_X + BTN_W / 2f - layout.width / 2f, BTN_Y + BTN_H / 2f + layout.height / 2f);
        batch.end();
    }

    private void drawKeysSection() {
        float mx = mx(), my = my();
        GameSettings s = GameSettings.get();
        int[] keys = {s.keyUp, s.keyDown, s.keyLeft, s.keyRight};

        for (int i = 0; i < 4; i++) {
            float rowY = KEY_START_Y - i * KEY_ROW_H;
            float bx = KEY_BTN_X;
            float by = rowY - KEY_BTN_H / 2f - 4;
            boolean hover = over(mx, my, bx, by, KEY_BTN_W, KEY_BTN_H);
            boolean active = remappingIndex == i;

            fontLabel.getData().setScale(1.4f);
            batch.begin();
            fontLabel.setColor(COL_CREAM);
            fontLabel.draw(batch, KEY_LABELS[i], KEY_LBL_X + 10, rowY + 8);
            batch.end();

            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(COL_GOLD_DARK);
            float dotY = rowY - 4;
            for (float dx = KEY_LBL_X + 80; dx < bx - 8; dx += 10) shape.line(dx, dotY, dx + 5, dotY);
            shape.end();

            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(0f, 0f, 0f, 0.35f);
            shape.rect(bx + 3, by - 3, KEY_BTN_W, KEY_BTN_H);
            Color bg = active ? COL_KEY_ACTIVE : (hover ? COL_KEY_HOVER : COL_KEY_BG);
            shape.setColor(bg);
            shape.rect(bx, by, KEY_BTN_W, KEY_BTN_H);
            shape.end();

            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(active ? COL_GOLD : COL_GOLD_DARK);
            shape.rect(bx, by, KEY_BTN_W, KEY_BTN_H);
            shape.end();

            String keyName = active ? "< appuie... >" : Input.Keys.toString(keys[i]);
            fontKey.getData().setScale(1.35f);
            layout.setText(fontKey, keyName);
            batch.begin();
            fontKey.setColor(active ? Color.WHITE : COL_GOLD);
            fontKey.draw(batch, keyName, bx + KEY_BTN_W / 2f - layout.width / 2f, by + KEY_BTN_H / 2f + layout.height / 2f);
            batch.end();
        }
    }

    private void drawZoomSection() {
        GameSettings s = GameSettings.get();
        float ratio = (s.zoom - GameSettings.ZOOM_MIN) / (GameSettings.ZOOM_MAX - GameSettings.ZOOM_MIN);
        float handleX = SLD_X + SLD_W * ratio;
        float handleY = SLD_Y + SLD_H / 2f;

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(COL_SLIDER_BG);
        shape.rect(SLD_X, SLD_Y, SLD_W, SLD_H);
        shape.setColor(COL_GOLD);
        shape.rect(SLD_X, SLD_Y, SLD_W * ratio, SLD_H);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(COL_GOLD_DARK);
        shape.rect(SLD_X, SLD_Y, SLD_W, SLD_H);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.4f);
        shape.circle(handleX + 3, handleY - 3, 13, 24);
        shape.setColor(COL_GOLD);
        shape.circle(handleX, handleY, 13, 24);
        shape.end();

        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(COL_GOLD_DARK);
        shape.circle(handleX, handleY, 13, 24);
        shape.end();

        fontKey.getData().setScale(1.2f);
        batch.begin();
        fontKey.setColor(COL_CREAM);
        fontKey.draw(batch, "0.25x", SLD_X - 4, SLD_Y - 6);
        layout.setText(fontKey, "4x");
        fontKey.draw(batch, "4x", SLD_X + SLD_W - layout.width, SLD_Y - 6);

        String zv = String.format("%.2fx", s.zoom);
        fontKey.getData().setScale(1.35f);
        fontKey.setColor(COL_GOLD);
        layout.setText(fontKey, zv);
        fontKey.draw(batch, zv, handleX - layout.width / 2f, SLD_Y + SLD_H + 30);
        batch.end();
    }

    private void drawRemappingOverlay() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0f, 0f, 0f, 0.72f);
        shape.rect(0, 0, W, H);
        shape.end();

        float bw = 440f, bh = 110f;
        float bx = W / 2f - bw / 2f, by = H / 2f - bh / 2f;
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(COL_PANEL_DARK);
        shape.rect(bx, by, bw, bh);
        shape.setColor(COL_PANEL);
        shape.rect(bx + 4, by + 4, bw - 8, bh - 8);
        shape.end();
        shape.begin(ShapeRenderer.ShapeType.Line);
        shape.setColor(COL_GOLD);
        shape.rect(bx, by, bw, bh);
        shape.end();

        String msg = "Nouvelle touche pour  « " + KEY_LABELS[remappingIndex] + " »";
        fontLabel.getData().setScale(1.4f);
        layout.setText(fontLabel, msg);
        batch.begin();
        fontLabel.setColor(COL_GOLD);
        fontLabel.draw(batch, msg, W / 2f - layout.width / 2f, H / 2f + 26);
        fontLabel.getData().setScale(1.2f);
        fontLabel.setColor(COL_CREAM);
        layout.setText(fontLabel, "Échap pour annuler");
        fontLabel.draw(batch, "Échap pour annuler", W / 2f - layout.width / 2f, H / 2f - 14);
        batch.end();
    }

    private void handleInput() {
        float mx = mx(), my = my();
        GameSettings s = GameSettings.get();

        if (remappingIndex >= 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) { remappingIndex = -1; return; }
            for (int k = 0; k < 256; k++) {
                if (Gdx.input.isKeyJustPressed(k)) { assignKey(remappingIndex, k); remappingIndex = -1; return; }
            }
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (over(mx, my, BTN_X, BTN_Y, BTN_W, BTN_H)) {
                game.setScreen(new GameScreen(game));
                return;
            }
            for (int i = 0; i < 4; i++) {
                float by = KEY_START_Y - i * KEY_ROW_H - KEY_BTN_H / 2f - 4;
                if (over(mx, my, KEY_BTN_X, by, KEY_BTN_W, KEY_BTN_H)) {
                    remappingIndex = i;
                    return;
                }
            }
            if (my >= SLD_Y - 10 && my <= SLD_Y + SLD_H + 10 && mx >= SLD_X && mx <= SLD_X + SLD_W) draggingZoom = true;
            float ratio = (s.zoom - GameSettings.ZOOM_MIN) / (GameSettings.ZOOM_MAX - GameSettings.ZOOM_MIN);
            float handleX = SLD_X + SLD_W * ratio;
            float handleY = SLD_Y + SLD_H / 2f;
            float dx = mx - handleX, dy = my - handleY;
            if (dx * dx + dy * dy <= 16 * 16) draggingZoom = true;
        }

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) draggingZoom = false;
        if (draggingZoom) {
            float ratio = MathUtils.clamp((mx - SLD_X) / SLD_W, 0f, 1f);
            s.zoom = GameSettings.ZOOM_MIN + ratio * (GameSettings.ZOOM_MAX - GameSettings.ZOOM_MIN);
        }
    }

    private void assignKey(int index, int key) {
        GameSettings s = GameSettings.get();
        switch (index) {
            case 0: s.keyUp = key; break;
            case 1: s.keyDown = key; break;
            case 2: s.keyLeft = key; break;
            case 3: s.keyRight = key; break;
        }
    }

    private float mx() { return viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY())).x; }
    private float my() { return viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY())).y; }
    private boolean over(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void dispose() {
        batch.dispose();
        shape.dispose();
        fontTitle.dispose();
        fontLabel.dispose();
        fontKey.dispose();
    }
}