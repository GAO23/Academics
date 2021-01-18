#!/bin/bash

./filesec -e -p ./password.txt test.txt encrypted.txt;
./filesec -d -p ./password.txt encrypted.txt decrypted.txt;