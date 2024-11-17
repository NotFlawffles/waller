package com.notflawffles.waller;

import com.notflawffles.waller.core.Loader;
import com.raylib.Jaylib;
import com.raylib.Raylib;

public class HintPopup {
    private Waller session;

    private String message;

    private Jaylib.Font font;
    
    private static final int MessageFontSize = 30;

    private static final float MessageTextSpacing = 1.0f;

    private Raylib.Vector2 messageTextSize;
    private Raylib.Vector2 textPosition;
    private Raylib.Vector2 boxSize;
    private Raylib.Vector2 boxPosition;

    private static final Raylib.Color BoxColor = Jaylib.GetColor(0x333333ee);

    private byte lifetime = 5;
    private byte frames = 0;

    public HintPopup(Waller session, String message) {
        this.session = session;
        this.message = message;
        
        font = Jaylib.LoadFont(Loader.Font);

        messageTextSize = Raylib.MeasureTextEx(font, this.message, MessageFontSize, MessageTextSpacing);
        textPosition = new Jaylib.Vector2(Jaylib.GetScreenWidth() / 2.0f - messageTextSize.x() / 2.0f, Jaylib.GetScreenHeight() / 2.0f - messageTextSize.y() / 2.0f);
        boxSize = new Jaylib.Vector2(messageTextSize.x() * Jaylib.GetScreenWidth() / 1500.0f, messageTextSize.y() * Jaylib.GetScreenHeight() / 100.0f);
        boxPosition = new Jaylib.Vector2(Jaylib.GetScreenWidth() / 2.0f - boxSize.x() / 2.0f, Jaylib.GetScreenHeight() / 2.0f - boxSize.y() / 2.0f);
    }

    private void updateLifetime() {
        if (frames++ == Jaylib.GetFPS() && lifetime > 0) {
            lifetime--;
            frames = 0;
        }
    }

    public boolean dead() {
        return lifetime == 0;
    }

    public void kill() {
        lifetime = 0;
    }

    public void update(float delta) {
        updateLifetime();
    }

    public void draw() {
        Jaylib.EndMode2D();

        Jaylib.DrawRectangleRounded(new Jaylib.Rectangle(boxPosition.x(), boxPosition.y(), boxSize.x(), boxSize.y()), 0.1f, 0, BoxColor);
        Jaylib.DrawTextPro(font, message, textPosition, new Jaylib.Vector2(), 0.0f, MessageFontSize, MessageTextSpacing, Jaylib.WHITE);

        Jaylib.BeginMode2D(session.camera);
    }
}
