package com.notflawffles.waller;

import java.nio.file.Path;
import java.util.ArrayList;

import com.notflawffles.waller.Waller.HintPopupIdentifier;
import com.raylib.Jaylib;
import com.raylib.Raylib;

public class ImageViewer {
    private Waller session;

    private ArrayList<Path> paths;
    private ArrayList<Jaylib.Image> images = new ArrayList<>();
    private ArrayList<Jaylib.Texture> textures = new ArrayList<>();

    private int offset = 0;
    private int target = 0;

    private int selection = 0;

    private static enum SlideDirection {
        Left,
        Right
    }

    private SlideDirection slideDirection;

    public float slideSpeed = 0.0f;
    private float slideSpeedModifier = 200.0f;

    private static final float MaximumSlideSpeed = 1e+4f;

    public boolean setMode = false;

    private boolean alreadySet = false;

    public ImageViewer(Waller session, ArrayList<Path> paths) {
        this.session = session;
        this.paths = paths;
        
        for (Path path: this.paths) {
            images.add(Jaylib.LoadImage(path.toString()));
        }
    }

    public void load() {
        for (Jaylib.Image image: images) {
            textures.add(Jaylib.LoadTextureFromImage(image));
        }
    }

    private boolean wantsToSlide() {
        return Jaylib.IsKeyPressed(Jaylib.KEY_LEFT) || Jaylib.IsKeyPressed(Jaylib.KEY_RIGHT);
    }

    private boolean canSlide() {
        return !setMode;
    }

    private void slide() {
        if (Jaylib.IsKeyPressed(Jaylib.KEY_RIGHT) && selection < images.size() - 1) {
            slideDirection = SlideDirection.Right;
            target += Jaylib.GetScreenWidth();
            selection++;
        } else if (Jaylib.IsKeyPressed(Jaylib.KEY_LEFT) && selection > 0) {
            slideDirection = SlideDirection.Left;
            target -= Jaylib.GetScreenWidth();
            selection--;
        }
    }

    private boolean shouldMove() {
        return slideDirection != null;
    }

    private void move(float delta) {
        switch (slideDirection) {
            case Left:
                offset -= slideSpeed * delta;

                if (offset < target) {
                    offset = target;
                    slideDirection = null;
                }

                break;

            case Right:
                offset += slideSpeed * delta;

                if (offset > target) {
                    offset = target;
                    slideDirection = null;
                }

                break;
        }

        if (!wantsToZoomOut()) {
            slideSpeed = Math.min(slideSpeed + slideSpeedModifier, MaximumSlideSpeed);
        }
    }

    private void resetSlideSpeed() {
        slideSpeed = Math.max(slideSpeed - slideSpeed / 4.0f - 0.1f, 0.0f);
    }

    private boolean wantsToSet() {
        return Jaylib.IsKeyDown(Jaylib.KEY_ENTER);
    }

    private boolean canSet() {
        return slideDirection == null && !wantsToZoomOut();
    }

    private void set() {
        session.previewViewer.toggleEnabled(false);
        slideSpeed += slideSpeedModifier * 10;

        if (slideSpeed > 7999) {
            session.hintPopups[HintPopupIdentifier.Set.ordinal()] = new HintPopup(session, "Successfully set as wallpaper");

            try {
                Runtime.getRuntime().exec("feh --bg-fill " + paths.get(selection));
                alreadySet = true;
            } catch (Exception ignore) {}
        }
    }

    public boolean wantsToZoomOut() {
        return Jaylib.IsKeyDown(Jaylib.KEY_LEFT_SHIFT);
    }

    private void zoomOut() {
        session.previewViewer.toggleEnabled(false);
        slideSpeed += slideSpeedModifier * 30;

        if (slideSpeed > MaximumSlideSpeed * 6) {
            slideSpeed = MaximumSlideSpeed * 6;
        }
    }

    public void update(float delta) {
        if (wantsToSlide() && canSlide()) {
            slide();
        }

        if (shouldMove()) {
            move(delta);
        } else {
            if (!wantsToZoomOut()) {
                resetSlideSpeed();
            }
        }

        if (wantsToSet() && canSet() && !alreadySet) {
            set();
        }

        if (!wantsToSet()) {
            alreadySet = false;
        }

        if (wantsToZoomOut()) {
            zoomOut();
        }

        setMode = wantsToSet() && canSet() && !alreadySet;
    }

    private void drawNoWallpapersScreen() {
        Jaylib.ClearBackground(Jaylib.GRAY);

        String text = "No wallpapers";
        int fontSize = 60;

        Raylib.Vector2 textSize = Jaylib.MeasureTextEx(Jaylib.GetFontDefault(), text, fontSize, 0);

        Jaylib.DrawText(text, (int) (Jaylib.GetScreenWidth() / 2.0f - textSize.x() / 2.0f), (int) (Jaylib.GetScreenHeight() / 2.0f - textSize.y() / 2.0f), fontSize, Jaylib.WHITE);
    }

    private Raylib.Color getInvertedColor(Raylib.Color color) {
        return new Jaylib.Color(0xff - color.r(), 0xff - color.g(), 0xff - color.b(), color.a());
    }

    public void drawName() {
        String name = paths.get(selection).getFileName().toString();

        int size = Jaylib.MeasureText(name, 30);
        int position = (int) (Jaylib.GetScreenWidth() / 2.0f - size / 2.0f);

        Raylib.Color color = getInvertedColor(Jaylib.GetImageColor(images.get(selection), position, 0));

        Jaylib.DrawText(name, (int) (Jaylib.GetScreenWidth() / 2.0f - size / 2.0f), 0, 30, color);
    }

    public void draw() {
        if (textures.isEmpty()) {
            drawNoWallpapersScreen();
        }

        for (int index = 0; index < textures.size(); ++index) {
            Jaylib.DrawTexture(textures.get(index), index * Jaylib.GetScreenWidth() - offset, 0, Jaylib.WHITE);

            Jaylib.DrawTexturePro(
                textures.get(index),
                new Jaylib.Rectangle(0, 0, images.get(index).width(), images.get(index).height()),
                new Jaylib.Rectangle(index * Jaylib.GetScreenWidth() - offset, 0, Jaylib.GetScreenWidth(), Jaylib.GetScreenHeight()),
                new Jaylib.Vector2(),
                0.0f,
                Jaylib.WHITE
            );
        }

        Jaylib.EndMode2D();

        if (!session.previewViewer.enabled) {
            drawName();
        }

        Jaylib.BeginMode2D(session.camera);

        if (setMode) {
            Jaylib.DrawRectangle(0, 0, (int) slideSpeed / 4 - 79, (int) slideSpeed / 8 + 81, Jaylib.GetColor(0x00550055));
        }

        if (wantsToZoomOut() && slideDirection == null) {
            Jaylib.DrawRectangleLines(0, 0, Jaylib.GetScreenWidth() - 1, Jaylib.GetScreenHeight() - 1, Jaylib.GREEN);
        }
    }
}
