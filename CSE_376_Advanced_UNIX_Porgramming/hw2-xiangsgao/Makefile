IDIR =./include
CC=gcc
CFLAGS=-g -O2 -Wall -Werror

ODIR=./obj
LDIR =./lib

driver: driver.o
	$(CC) -o driver -L $(LDIR) $(ODIR)/driver.o -llkmalloc $(CFLAGS)

driver.o: liblkmalloc.a
	$(CC) -c ./src/main.c -o $(ODIR)/driver.o $(CFLAGS)

liblkmalloc.a: LKmalloc.o
	mkdir -p $(LDIR)
	ar rs $(LDIR)/liblkmalloc.a $(ODIR)/LKmalloc.o

LKmalloc.o: ./src/LKmalloc.c
	mkdir -p $(ODIR)
	$(CC) -c ./src/LKmalloc.c -o $(ODIR)/LKmalloc.o $(CFLAGS)

test: driver
	chmod 777 ./test.sh
	./test.sh

.PHONY: clean

clean:
	rm -rf $(LDIR) $(ODIR) ./report driver