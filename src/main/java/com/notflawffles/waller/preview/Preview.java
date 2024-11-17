package com.notflawffles.waller.preview;

import com.raylib.Jaylib;

public class Preview {
    public String path;
    public Jaylib.Image image;
    public Jaylib.Texture texture;

    public Preview(String path) {
        this.path = path;

        image = Jaylib.LoadImage(this.path);
    }

    public void load() {
        texture = Jaylib.LoadTextureFromImage(image);
    }

    public void draw(int y) {
        Jaylib.DrawTexturePro(
            texture,
            new Jaylib.Rectangle(0, 0, image.width(), image.height()),
            new Jaylib.Rectangle(0, y, Jaylib.GetScreenWidth(), Jaylib.GetScreenHeight()),
            new Jaylib.Vector2(),
            0.0f,
            Jaylib.WHITE
        );
    }
}
