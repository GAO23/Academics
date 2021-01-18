#!/bin/bash

if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 
   exit 1
fi

# important, you need to set  TO_TRAIN to true if you are running for the first time to train the ai
apt update; apt install python python3 python-pip python3-pip;
pip3 install flask spacy torch torchtext transformers;
python3 -m spacy download en;
python3 ./avg_model.py;

