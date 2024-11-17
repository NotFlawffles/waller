package com.notflawffles.waller.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import com.notflawffles.waller.Waller;

public class Loader {
    public static final String Font = new File(Waller.class.getClassLoader().getResource("JetBrainsMonoNerdFont-Regular.ttf").getFile()).getAbsolutePath();

    private static Path WallpapersPath = Path.of(System.getenv("HOME") + "/Pictures/Wallpapers");

    public static void ensurePathExistence() {
        try {
            Files.createDirectories(WallpapersPath);
        } catch (Exception ignore) {}
    }

    public static ArrayList<Path> getPaths() {
        File directory = WallpapersPath.toFile();

        ArrayList<Path> paths = new ArrayList<>();

        for (File file: directory.listFiles()) {
            paths.add(Path.of(file.getAbsolutePath()));
        }

        return paths;
    }
}
