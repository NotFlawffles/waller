CC = cc
CFLAGS = -Wall -Wextra -pedantic -ggdb -o
OBJECT = bin/waller
SRC = $(wildcard src/waller.c)
LIBS = -lraylib

default: $(SRC)
	@mkdir -p bin
	@$(CC) $(CFLAGS) $(OBJECT) $(SRC) $(LIBS)

clean: $(OBJECT)
	@rm $(OBJECT)
