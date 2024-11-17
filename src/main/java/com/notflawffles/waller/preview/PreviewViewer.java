package com.notflawffles.waller.preview;

import java.util.ArrayList;
import java.util.Arrays;

import com.notflawffles.waller.Waller;
import com.raylib.Jaylib;
import com.raylib.Raylib;

public class PreviewViewer {
    Waller session;

    private ArrayList<Preview> previews;

    private int offset = 0;
    private int target = 0;

    private int selection = 0;

    private static enum SlideDirection {
        Up,
        Down
    }

    private SlideDirection slideDirection;

    public float slideSpeed = 0.0f;
    private float slideSpeedModifier = 100.0f;

    private static final float MaximumSlideSpeed = 1000.0f;

    public boolean enabled = false;

//    private boolean showedHintPopup = false;

    public PreviewViewer(Waller session) {
        this.session = session;

        previews = new ArrayList<>(Arrays.asList(
            new VisualStudioCodePreview(),
            new NeovimPreview(),
            new TerminalPreview()
        ));

        toggleEnabled();
    }

    public void load() {
        for (Preview preview: previews) {
            preview.load();
        }
    }

    private boolean wantsToSlide() {
        return Jaylib.IsKeyPressed(Jaylib.KEY_UP) || Jaylib.IsKeyPressed(Jaylib.KEY_DOWN);
    }

    private boolean canSlide() {
        return true;
    }

    private void slide() {
        if (Jaylib.IsKeyPressed(Jaylib.KEY_DOWN) && selection < previews.size() - 1) {
            slideDirection = SlideDirection.Down;
            target += Jaylib.GetScreenHeight();
            selection++;
        } else if (Jaylib.IsKeyPressed(Jaylib.KEY_UP) && selection > 0) {
            slideDirection = SlideDirection.Up;
            target -= Jaylib.GetScreenHeight();
            selection--;
        }
    }

    private boolean shouldMove() {
        return slideDirection != null;
    }

    private void move(float delta) {
        switch (slideDirection) {
            case Up:
                offset -= slideSpeed * delta;

                if (offset < target) {
                    offset = target;
                    slideDirection = null;
                }

                break;

            case Down:
                offset += slideSpeed * delta;

                if (offset > target) {
                    offset = target;
                    slideDirection = null;
                }

                break;
        }

        slideSpeed += Math.min(slideSpeedModifier, MaximumSlideSpeed);
    }

    private void resetSlideSpeed() {
        slideSpeed = Math.max(slideSpeed - slideSpeed / 4.0f - 0.1f, 0.0f);
    }

    private boolean wantsToToggle() {
        return Jaylib.IsKeyPressed(Jaylib.KEY_P);
    }

    private boolean canToggle() {
        return !session.imageViewer.setMode;
    }

    public void toggleEnabled() {
//        enabled = !enabled;
//
//        if (enabled) {
//            if (selection >= previews.size() / 2) {
//                slideDirection = SlideDirection.Up;
//                target -= Jaylib.GetScreenHeight() * (selection + 1);
//            } else {
//                slideDirection = SlideDirection.Down;
//                target += Jaylib.GetScreenHeight() * (selection + 1);
//            }
//        } else {
//            if (selection >= previews.size() / 2) {
//                slideDirection = SlideDirection.Down;
//                target += Jaylib.GetScreenHeight() * (selection + 1);
//            } else {
//                slideDirection = SlideDirection.Up;
//                target -= Jaylib.GetScreenHeight() * (selection + 1);
//            }
//        }
//
//        if (enabled && !showedHintPopup) {
//            session.hintPopups[HintPopupIdentifier.Preview.ordinal()] = new HintPopup(session, "Press up/down to navigate");
//            showedHintPopup = true;
//        }
    }

    public void toggleEnabled(boolean value) {
//        enabled = false;
//        slideSpeed = 1e+4f;
//
//        if (value && !enabled) {
//            toggleEnabled();
//        } else if (!value && enabled) {
//            toggleEnabled();
//        }
    }

    public void update(float delta) {
//        if (wantsToToggle() && canToggle()) {
//            toggleEnabled();
//        }

        if (enabled && wantsToSlide() && canSlide()) {
            slide();
        }

        if (shouldMove()) {
            move(delta);
        } else {
            resetSlideSpeed();
        }
    }

    private void drawNoPreviewsScreen() {
        Jaylib.ClearBackground(Jaylib.GRAY);

        String text = "No previews";
        int fontSize = 60;

        Raylib.Vector2 textSize = Jaylib.MeasureTextEx(Jaylib.GetFontDefault(), text, fontSize, 0);

        Jaylib.DrawText(text, (int) (Jaylib.GetScreenWidth() / 2.0f - textSize.x() / 2.0f), (int) (Jaylib.GetScreenHeight() / 2.0f - textSize.y() / 2.0f), fontSize, Jaylib.WHITE);
    }

    public void draw() {
        if (previews.isEmpty()) {
            drawNoPreviewsScreen();
        }

        for (int index = 0; index < previews.size(); ++index) {
            if ((!enabled && slideSpeed == 0.0f) && index == 0 || session.imageViewer.wantsToZoomOut()) {
                continue;
            }

            previews.get(index).draw(index * Jaylib.GetScreenHeight() - offset);
        }
    }
}
