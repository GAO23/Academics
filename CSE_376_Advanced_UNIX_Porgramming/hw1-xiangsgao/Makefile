# varible declaration
CC=gcc
CFLAGS=-g -O2 -Wall -Werror
# include directory
IDIR=./include
# for creating a new directory to hold all the object files
ODIR=obj
# for the libraries we need
LIBS=-lcrypto
# for the source files
SRC=./src

# I have no fucking idea what I am doing here. This is to list the files????
DEPS = $(IDIR)/crypto.h $(IDIR)/constants_and_prototypes.h $(SRC)/crypto.c $(SRC)/main.c

# command for building
filesec: $(DEPS)
	$(CC) -o filesec ./src/*c $(LIBS)

# command for cleaning
.PHONY: clean
clean:
	rm -rf  ./filesec

.PHONY: test
test: filesec ./test/test.sh
	chmod +x ./test/test.sh
	cp ./test/* ./
	./test/test.sh