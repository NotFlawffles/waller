#include <ctype.h>
#include <raylib.h>
#include <malloc.h>
#include <dirent.h>
#include <string.h>
#include <stdlib.h>

static const int WindowWidth = 1920;
static const int WindowHeight = 1080;
static const char WindowTitle[] = "Waller";
static const int WindowFramesPerSecond = 60;

static const char WallpapersPath[] = "/home/notflawffles/Pictures/Wallpapers";
static const char FallbackPath[] = "/home/notflawffles/.fehbg";
static const char CursorPath[] = "/home/notflawffles/Programming/waller/assets/miku-cursor.png";

typedef struct {
    const char *path;
    const char *name;
} Wallpaper;

Wallpaper wallpaper_new(const char *path) {
    return (Wallpaper) {
            path,
            path + strlen(WallpapersPath) + 1
    };
}

typedef struct {
    Wallpaper *container;
    size_t size;
} Wallpapers;

Wallpapers wallpapers_new(Wallpaper *container, size_t size) {
    return (Wallpapers) {
            container,
            size
    };
}

Wallpapers wallpapers_load() {
    Wallpaper *container = malloc(sizeof(Wallpaper));
    size_t size = 0;

    DIR *wallpapers_directory = opendir(WallpapersPath);
    struct dirent *dirent;

    while ((dirent = readdir(wallpapers_directory))) {
        if (*dirent->d_name == '.') {
            continue;
        }

        char *current_wallpaper_path = malloc(strlen(WallpapersPath) + strlen(dirent->d_name));
        sprintf(current_wallpaper_path, "%s/%s", WallpapersPath, dirent->d_name);
        container = realloc(container, sizeof(Wallpaper) * (size + 1));
        container[size++] = wallpaper_new(current_wallpaper_path);
    }

    return wallpapers_new(container, size);
}

typedef struct {
    Texture *container;
    size_t size;
} Textures;

Textures textures_new(Texture *container, size_t size) {
    return (Textures) {
            container,
            size
    };
}

Textures textures_load(const Wallpapers *wallpapers) {
    Texture *container = malloc(sizeof(Texture));
    size_t size = 0;

    for (size_t index = 0; index < wallpapers->size; index++) {
        container = realloc(container, sizeof(Texture) * (size + 1));
        container[size++] = LoadTexture(wallpapers->container[index].path);
    }

    return textures_new(container, size);
}

void set_wallpaper(const Wallpapers *wallpapers, const size_t *selection) {
    char *command = malloc(strlen("feh --no-fehbg --bg-scale ") + strlen(wallpapers->container[*selection].path) + 3);
    sprintf(command, "feh --no-fehbg --bg-scale '%s'\n", wallpapers->container[*selection].path);
    system(command);

    FILE* fehbg = fopen(FallbackPath, "w");

    if (!fehbg) {
        return;
    }

    fwrite("#!/bin/sh\n", strlen("#!/bin/sh\n"), sizeof(char), fehbg);
    fwrite(command, strlen(command), sizeof(char), fehbg);

    fclose(fehbg);
}

void print_help() {
    puts("-- waller usage --\n\n"
         "waller (alone)		launches gui\n"
         "waller s[et] <number>	sets wallpaper depending on its order of loading\n"
         "waller l[ist]		lists wallpapers\n"
         "waller (h[help]|.*)		prints this message");
}

int command_mode_main(char **argv, const Wallpapers *wallpapers, size_t *selection) {
    const char *command = argv[1];
    const char *subcommand = argv[2];

    if (memcmp(command, "s", 1) == 0) {
        if (subcommand && isdigit(*subcommand)) {
            *selection = atoi(subcommand);
            set_wallpaper(wallpapers, selection);
        } else {
            puts("waller error: expected a numeric argument");
        }
    } else if (memcmp(command, "l", 1) == 0) {
        for (size_t index = 0; index < wallpapers->size; index++) {
            puts(wallpapers->container[index].name);
        }
    } else if (memcmp(command, "h", 1) == 0) {
        print_help();
    } else {
        print_help();
    }

    return 0;
}

void fallback(const Wallpapers *wallpapers, size_t *selection) {
    FILE *fehbg = fopen(FallbackPath, "r");

    if (!fehbg) {
        return;
    }

    const char *to_skip = "#!/bin/sh\nfeh --no-fehbg --bg-scale '";
    char *match = malloc(255);

    fread(match, 1, strlen(to_skip), fehbg);
    memset(match, 0, strlen(to_skip));

    size_t start = ftell(fehbg);
    fseek(fehbg, 0, SEEK_END);
    size_t end = ftell(fehbg);
    fseek(fehbg, 0, SEEK_SET);

    fread(match, 1, strlen(to_skip), fehbg);
    memset(match, 0, strlen(to_skip));

    fread(match, 1, end - start - 1, fehbg);

    for (size_t index = 0; index < wallpapers->size; index++) {
        if (memcmp(wallpapers->container[index].path, match, strlen(wallpapers->container[index].path)) == 0) {
            *selection = index;
            break;
        }
    }
}

typedef struct {
    Texture texture;
    Vector2 position;
} Cursor;

Cursor cursor_new(Texture texture, const Vector2 position) {
    return (Cursor) {
            texture,
            position
    };
}

void cursor_set_position(Cursor *cursor, const Vector2 position) {
    cursor->position = position;
}

int main(int argc, char **argv) {
    const Wallpapers wallpapers = wallpapers_load();
    size_t selection = 0;

    if (argc > 1) {
        return command_mode_main(argv, &wallpapers, &selection);
    }

    SetConfigFlags(FLAG_FULLSCREEN_MODE | FLAG_VSYNC_HINT | FLAG_MSAA_4X_HINT | FLAG_WINDOW_UNDECORATED | FLAG_WINDOW_RESIZABLE);
    InitWindow(WindowWidth, WindowHeight, WindowTitle);
    SetTargetFPS(WindowFramesPerSecond);
    HideCursor();

    Textures textures = textures_load(&wallpapers);

    fallback(&wallpapers, &selection);

    Cursor cursor = cursor_new(LoadTexture(CursorPath), GetMousePosition());

    unsigned short counter = 0;

    bool gui_active = true;

    while (!WindowShouldClose()) {
        BeginDrawing();

        if (GetKeyPressed() || (GetMouseX() != cursor.position.x && GetMouseY() != cursor.position.y)) {
            counter = 0;
        }

        if (IsKeyPressed(KEY_RIGHT) && selection < wallpapers.size - 1) {
            selection++;
        } else if (IsKeyPressed(KEY_LEFT) && selection > 0) {
            selection--;
        }

        if (IsKeyPressed(KEY_ENTER)) {
            set_wallpaper(&wallpapers, &selection);
            break;
        }

        cursor_set_position(&cursor, GetMousePosition());

        ClearBackground(BLACK);

        DrawTexturePro(textures.container[selection],
                        (Rectangle) {0, 0, textures.container[selection].width, textures.container[selection].height}, (Rectangle) {0, 0, GetRenderWidth(), GetRenderHeight()}, (Vector2) {0, 0}, 0, WHITE);

        unsigned short ui_disappearance_frames_delay = GetFPS() * 3;

        if (IsKeyPressed(KEY_SPACE)) {
            if (gui_active) {
                counter = ui_disappearance_frames_delay;
                gui_active = false;
            } else {
                counter = 0;
                gui_active = true;
            }
        }

        if (counter < ui_disappearance_frames_delay) {
            DrawText("<", 10, GetRenderHeight() / 2.0 - 30 / 2.0, 30, WHITE);
            DrawText(">", GetRenderWidth() - 5 - 30 / 2.0, GetRenderHeight() / 2.0 - 30 / 2.0, 30, WHITE);

            const Vector2 text_size = MeasureTextEx(GetFontDefault(), wallpapers.container[selection].name, 30, 0);
            DrawText(wallpapers.container[selection].name, GetRenderWidth() / 2.0 - text_size.x / 2.0,
                     text_size.y / 2.0,
                     30, WHITE);

            DrawTexture(cursor.texture, cursor.position.x, cursor.position.y, WHITE);
        }

        if (counter >= ui_disappearance_frames_delay) {
            gui_active = false;
        }

        EndDrawing();

        counter++;
    }

    CloseWindow();
}