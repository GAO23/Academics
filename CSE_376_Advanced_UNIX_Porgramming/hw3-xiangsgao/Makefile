CFLAGS = -g -O2 -Wall -Werror -Wextra

SRC=$(wildcard src/*.c)

smash: $(SRC)
	gcc -o $@ $^ $(CFLAGS) $(LIBS)

test: smash
	chmod +x ./smash
	chmod +x ./test.sh
	./smash -d ./test.sh

.PHONY: clean
clean:
	rm -rf smash