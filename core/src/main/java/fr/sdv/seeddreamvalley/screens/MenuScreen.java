package fr.sdv.seeddreamvalley.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import fr.sdv.seeddreamvalley.Main;

public class MenuScreen extends ScreenAdapter {

    private final Main game;
    private final OrthographicCamera camera;
    private final FitViewport        viewport;
    private final SpriteBatch        batch;
    private final ShapeRenderer      shape;
    private final BitmapFont         fontTitle;
    private final BitmapFont         fontLabel;
    private final BitmapFont         fontKey;
    private final GlyphLayout        layout;

    private static final float W = 800f;
    private static final float H = 600f;

    
    private static final Color COL_BG          = new Color(0.18f, 0.12f, 0.08f, 1f);
    private static final Color COL_PANEL       = new Color(0.35f, 0.22f, 0.12f, 1f);
    private static final Color COL_PANEL_DARK  = new Color(0.22f, 0.13f, 0.07f, 1f);
    private static final Color COL_PANEL_LIGHT = new Color(0.42f, 0.27f, 0.15f, 1f);
    private static final Color COL_GOLD        = new Color(0.95f, 0.78f, 0.35f, 1f);
    private static final Color COL_GOLD_DARK   = new Color(0.65f, 0.50f, 0.15f, 1f);
    private static final Color COL_CREAM       = new Color(0.97f, 0.92f, 0.75f, 1f);
    private static final Color COL_BTN         = new Color(0.42f, 0.68f, 0.22f, 1f);
    private static final Color COL_BTN_LIGHT   = new Color(0.55f, 0.82f, 0.30f, 1f);
    private static final Color COL_BTN_DARK    = new Color(0.25f, 0.42f, 0.10f, 1f);
    private static final Color COL_KEY_BG      = new Color(0.25f, 0.16f, 0.09f, 1f);

    
    private static final float BTN_W = 240f;
    private static final float BTN_H = 56f;
    private static final float BTN_X = W / 2f - BTN_W / 2f;
    private static final float BTN_Y = H - 200f;


    private static final float KEY_LBL_X  = 110f;
    private static final float KEY_BTN_X  = 420f;
    private static final float KEY_BTN_W  = 180f;
    private static final float KEY_BTN_H  = 34f;
    private static final float KEY_ROW_H  = 52f;
    private static final float KEY_START_Y = BTN_Y - 100f;


    public MenuScreen(Main game) {
        this.game = game;

        camera   = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);
        camera.position.set(W / 2f, H / 2f, 0);
        camera.update();

        batch  = new SpriteBatch();
        shape  = new ShapeRenderer();
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
        drawDivider(KEY_START_Y + 60f, "Touches de déplacement");
        drawKeysSection();
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
        float ty = H - 60f;

        batch.begin();
        fontTitle.setColor(0f, 0f, 0f, 0.5f);
        fontTitle.draw(batch, title, tx + 3, ty - 3);
        fontTitle.setColor(COL_GOLD);
        fontTitle.draw(batch, title, tx, ty);
        batch.end();

        float lineY  = ty - layout.height - 8f;
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
        batch.begin();
        fontLabel.setColor(COL_GOLD);
        fontLabel.draw(batch, label, KEY_LBL_X, y);
        batch.end();
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(COL_GOLD_DARK);
        shape.rect(KEY_LBL_X, y - layout.height - 4, layout.width + 20, 4);
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
        shape.setColor(1f, 1f, 1f, hover ? 0.18f : 0.10f);
        shape.rect(BTN_X + 4, BTN_Y + BTN_H * 0.55f, BTN_W - 8, BTN_H * 0.38f);
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
        fontLabel.draw(batch, label,
            BTN_X + BTN_W / 2f - layout.width / 2f + 2,
            BTN_Y + BTN_H / 2f + layout.height / 2f - 2);
        fontLabel.setColor(Color.WHITE);
        fontLabel.draw(batch, label,
            BTN_X + BTN_W / 2f - layout.width / 2f,
            BTN_Y + BTN_H / 2f + layout.height / 2f);
        batch.end();
    }

    
    private void drawKeysSection() {
        String[] labels   = {"Aller en haut", "Aller en bas", "Aller à gauche", "Aller à droite", "Interagir"};
        String[] keyNames = {"Z", "S", "Q", "D", "Clic gauche"};

        for (int i = 0; i < 5; i++) {
            float rowY = KEY_START_Y - i * KEY_ROW_H;
            float bx   = KEY_BTN_X;
            float by   = rowY - KEY_BTN_H / 2f - 4;

            fontLabel.getData().setScale(1.4f);
            batch.begin();
            fontLabel.setColor(COL_CREAM);
            fontLabel.draw(batch, labels[i], KEY_LBL_X + 10, rowY + 8);
            batch.end();

            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(COL_GOLD_DARK);
            for (float dx = KEY_LBL_X + 80; dx < bx - 8; dx += 10)
                shape.line(dx, rowY - 4, dx + 5, rowY - 4);
            shape.end();

            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(COL_KEY_BG);
            shape.rect(bx, by, KEY_BTN_W, KEY_BTN_H);
            shape.end();

            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setColor(COL_GOLD_DARK);
            shape.rect(bx, by, KEY_BTN_W, KEY_BTN_H);
            shape.end();

            fontKey.getData().setScale(1.35f);
            layout.setText(fontKey, keyNames[i]);
            batch.begin();
            fontKey.setColor(COL_GOLD);
            fontKey.draw(batch, keyNames[i],
                bx + KEY_BTN_W / 2f - layout.width / 2f,
                by + KEY_BTN_H / 2f + layout.height / 2f);
            batch.end();
        }
    }
    
    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if (over(mx(), my(), BTN_X, BTN_Y, BTN_W, BTN_H)) {
                game.setScreen(new GameScreen(game));
            }
        }
    }

    
    private float mx() { return viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY())).x; }
    private float my() { return viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY())).y; }
    private boolean over(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override public void resize(int w, int h) { viewport.update(w, h); }

    @Override
    public void dispose() {
        batch.dispose();
        shape.dispose();
        fontTitle.dispose();
        fontLabel.dispose();
        fontKey.dispose();
    }
}