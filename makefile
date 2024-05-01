CC = cc
CFLAGS = -Wall -Wextra -pedantic -ggdb -o
OBJECT = bin/waller
SRC = $(wildcard src/waller.c)
INSTALLATION_TARGET = /$(OBJECT)
LIBS = -lraylib

default: $(SRC)
	@mkdir -p bin
	@$(CC) $(CFLAGS) $(OBJECT) $(SRC) $(LIBS)

install:
	@make default
	@sudo echo "$(CURDIR)/$(OBJECT) $@" > $(INSTALLATION_TARGET)
	@sudo chmod +x $(INSTALLATION_TARGET)

clean: $(OBJECT)
	@rm $(OBJECT)
