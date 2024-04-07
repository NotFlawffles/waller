#include <ctype.h>
#include <raylib.h>
#include <stddef.h>
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

typedef struct {
    const char* path;
    const char* name;
} Wallpaper;

Wallpaper wallpaper_new(const char* path) {
    return (Wallpaper) {
	path,
	path + strlen(WallpapersPath) + 1
    };
}

typedef struct {
    Wallpaper* container;
    size_t size;
} Wallpapers;

Wallpapers wallpapers_new(Wallpaper* container, size_t size) {
    return (Wallpapers) {
	container,
	size
    };
}

Wallpapers wallpapers_load() {
    Wallpaper* container = malloc(sizeof(Wallpaper));
    size_t size = 0;
    
    DIR* wallpapers_directory = opendir(WallpapersPath);
    struct dirent* dirent;

    while ((dirent = readdir(wallpapers_directory))) {
	if (*dirent->d_name == '.') {
	    continue;
	}

	char* current_wallpaper_path = malloc(strlen(WallpapersPath) + strlen(dirent->d_name));
	sprintf(current_wallpaper_path, "%s/%s", WallpapersPath, dirent->d_name);
	container = realloc(container, sizeof(Wallpaper) * (size + 1));
	container[size++] = wallpaper_new(current_wallpaper_path);
    }

    return wallpapers_new(container, size);
}

typedef struct {
    Texture* container;
    size_t size;
} Textures;

Textures textures_new(Texture* container, size_t size) {
    return (Textures) {
	container,
	size
    };
}

Textures textures_load(const Wallpapers* wallpapers) {
    Texture* container = malloc(sizeof(Texture));
    size_t size = 0;

    for (size_t index = 0; index < wallpapers->size; index++) {
	container = realloc(container, sizeof(Texture) * (size + 1));
	container[size++] = LoadTexture(wallpapers->container[index].path);
    }

    return textures_new(container, size);
}

void set_wallpaper(const Wallpapers* wallpapers, const size_t* selection) {
    char* command = malloc(strlen("feh --bg-scale ") + strlen(wallpapers->container[*selection].path));
    sprintf(command, "feh --bg-scale %s", wallpapers->container[*selection].path);
    system(command);
}

void print_help() {
    puts("-- waller usage --\n\n"
	 "waller (alone)		launches gui\n"
	 "waller s[et] <number>	sets wallpaper depending on its order of loading\n"
	 "waller l[ist]		lists wallpapers\n"
	 "waller (h[help]|.*)		prints this message");
}

int command_mode_main(const int argc, char** argv, const Wallpapers* wallpapers, size_t* selection) {
    const char* command = argv[1];
    const char* subcommand = argv[2];

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

void fallback(const Wallpapers* wallpapers, size_t* selection) {
    FILE* fehbg = fopen(FallbackPath, "r");

    if (!fehbg) {
	return;
    }

    const char* to_skip = "#!/bin/sh\nfeh --no-fehbg --bg-scale '";
    char* match = malloc(255);

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

int main(int argc, char** argv) {
    const Wallpapers wallpapers = wallpapers_load();
    size_t selection = 0;

    if (argc > 1) {
	return command_mode_main(argc, argv, &wallpapers, &selection);
    }

    InitWindow(WindowWidth, WindowHeight, WindowTitle);
    SetTargetFPS(WindowFramesPerSecond);
    HideCursor();
    ToggleFullscreen();

    bool should_redraw = true;
    Textures textures = textures_load(&wallpapers);

    fallback(&wallpapers, &selection);

    int left_frames = 0;
    int left_size = 30;
    Color left_color = WHITE;

    int right_frames = 0;
    int right_size = 30;
    Color right_color = WHITE;

    while (!WindowShouldClose()) {
	BeginDrawing();
    
	if (should_redraw) {
	    ClearBackground(BLACK);
	    DrawTexture(textures.container[selection], 0, 0, WHITE);
	    DrawText("<", 10, GetScreenHeight() / 2.0 - left_size / 2.0, left_size, left_color);
	    DrawText(">", GetScreenWidth() - 5 - right_size / 2.0, GetScreenHeight() / 2.0 - right_size / 2.0, right_size, right_color);

	    const Vector2 text_size = MeasureTextEx(GetFontDefault(), wallpapers.container[selection].name, 30, 0);
	    DrawText(wallpapers.container[selection].name, GetScreenWidth() / 2.0 - text_size.x / 2.0, text_size.y / 2.0, 30, WHITE);

	    should_redraw = false;
	}

	if (IsKeyPressed(KEY_RIGHT) && selection < wallpapers.size - 1) {
	    selection++;
	    right_size = 50;
	    right_color = GREEN;
	    should_redraw = true;
	} else if (IsKeyPressed(KEY_LEFT) && selection > 0) {
	    selection--;
	    left_size = 50;
	    left_color = GREEN;
	    should_redraw = true;
	}

	if (IsKeyPressed(KEY_ENTER)) {
	    set_wallpaper(&wallpapers, &selection);
	    break;
	}

	if (left_size == 50) {
	    if (left_frames < 45) {
		left_frames++;
	    } else {
		left_size = 30;
		left_color = WHITE;
		should_redraw = true;
	    }
	}

	if (right_size == 50) {
	    if (right_frames < 45) {
		right_frames++;
	    } else {
		right_size = 30;
		right_color = WHITE;
		should_redraw = true;
	    }
	}

	EndDrawing();
    }

    CloseWindow();
}
