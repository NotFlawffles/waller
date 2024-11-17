package com.notflawffles.waller;

import com.notflawffles.waller.core.Loader;
import com.notflawffles.waller.core.Window;
import com.notflawffles.waller.preview.PreviewViewer;
import com.raylib.Jaylib;

public class Waller implements Runnable {
    private static final byte TraceLogLevel = Jaylib.LOG_NONE;

    public ImageViewer imageViewer;
    public PreviewViewer previewViewer;

    public Jaylib.Camera2D camera = new Jaylib.Camera2D();

    private static final byte HintPopupsCount = 3;
    public HintPopup[] hintPopups = new HintPopup[HintPopupsCount];

    public static enum HintPopupIdentifier {
        Navigation,
        Preview,
        Set
    }

    private void initializeWindow() {
        Jaylib.SetTraceLogLevel(TraceLogLevel);

        Jaylib.SetConfigFlags(Window.ConfigurationFlags);
        Jaylib.InitWindow(Window.Width, Window.Height, Window.Title);
        Jaylib.SetTargetFPS(Window.FramesPerSecond);
        Jaylib.ToggleFullscreen();
        Jaylib.HideCursor();
    }

    private void initializeImageViewer() {
        Loader.ensurePathExistence();

        imageViewer = new ImageViewer(this, Loader.getPaths());
        imageViewer.load();
    }

    private void initializePreviewViewer() {
        previewViewer = new PreviewViewer(this);

        previewViewer.load();
    }

    private void initialize() {
        initializeImageViewer();
        initializePreviewViewer();

        hintPopups[HintPopupIdentifier.Navigation.ordinal()] = new HintPopup(this, "Press left/right to navigate or press and hold left shift to zoom out");
    }

    private void updateCamera() {
        camera.offset(Jaylib.Vector2Divide(new Jaylib.Vector2(Jaylib.GetScreenWidth(), Jaylib.GetScreenHeight()), new Jaylib.Vector2(2.0f, 2.0f)));
        camera.target(Jaylib.Vector2Divide(new Jaylib.Vector2(Jaylib.GetScreenWidth(), Jaylib.GetScreenHeight()), new Jaylib.Vector2(2.0f, 2.0f)));
        camera.zoom(-imageViewer.slideSpeed / 1e+5f + 1.0f);
    }

    private void updateHintPopups(float delta) {
        for (int index = 0; index < HintPopupsCount; ++index) {
            if (hintPopups[index] != null) {
                if (hintPopups[index].dead()) {
                    hintPopups[index] = null;
                } else {
                    hintPopups[index].update(delta);

                    switch (index) {
                        case 0:
                            if (Jaylib.IsKeyPressed(Jaylib.KEY_LEFT) || Jaylib.IsKeyPressed(Jaylib.KEY_RIGHT) || Jaylib.IsKeyPressed(Jaylib.KEY_ENTER) || Jaylib.IsKeyPressed(Jaylib.KEY_LEFT_SHIFT)) {
                                hintPopups[index].kill();
                            }

                            break;
                        
                        case 1:
                            if (Jaylib.IsKeyPressed(Jaylib.KEY_UP) || Jaylib.IsKeyPressed(Jaylib.KEY_DOWN) || Jaylib.IsKeyPressed(Jaylib.KEY_ENTER)) {
                                hintPopups[index].kill();
                            }

                            break;

                        case 2:
                            break;
                    }
                }
            }
        }
    }

    private void update(float delta) {
        updateCamera();

        imageViewer.update(delta);
        previewViewer.update(delta);

        updateHintPopups(delta);
    }

    private void drawHintPopups() {
        for (int index = 0; index < HintPopupsCount; ++index) {
            if (hintPopups[index] != null) {
                hintPopups[index].draw();
            }
        }
    }

    private void draw() {
        Jaylib.BeginDrawing();
        Jaylib.ClearBackground(Jaylib.BLACK);
        Jaylib.BeginMode2D(camera);

        imageViewer.draw();
        previewViewer.draw();

        drawHintPopups();

        Jaylib.EndMode2D();
        Jaylib.EndDrawing();
    }

    @Override
    public void run() {
        initializeWindow();
        initialize();

        while (!Jaylib.WindowShouldClose()) {
            update(Jaylib.GetFrameTime());
            draw();
        }

        Jaylib.CloseWindow();
    }

    public static void main(String[] args) {
        new Thread(new Waller()).start();;
    }
}
