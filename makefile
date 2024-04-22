CC = cc
CFLAGS = -Wall -Wextra -pedantic -ggdb -o
OBJECT = waller
SRC = $(wildcard src/*.c)
INSTALLATION_TARGET = /usr/bin/waller
LIBS = -lraylib

default: $(SRC)
	@$(CC) $(CFLAGS) $(OBJECT) $(SRC) $(LIBS)

clean: $(OBJECT)
	@rm $(OBJECT)

install: $(INSTALLATION_TARGET)
	@make default
	@sudo mv $(OBJECT) $(INSTALLATION_TARGET)

uninstall: $(INSTALLATION_TARGET)
	@sudo rm $(INSTALLATION_TARGET)
