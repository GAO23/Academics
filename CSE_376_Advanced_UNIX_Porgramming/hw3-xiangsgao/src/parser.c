//
// Created by xgao on 4/8/20.
//

#include "../include/commons.h"
#include "../include/parser.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

char* read_input(void){
    int buffer_size = BUFFER_SIZE;
    char* retVal = calloc(1, buffer_size);

    if(retVal == NULL){
        perror("Failed to calloc in read_input");
        exit(1);
    }

    int position = 0;
    char c = '\0';

    while(true){
        c = getchar();
        if(c == EOF || c == '\n') break;
        retVal[position] = c;
        position += 1;
        if(position >= buffer_size){
            buffer_size += BUFFER_SIZE;
            retVal = realloc(retVal, buffer_size + buffer_size);
            if(retVal == NULL) {
                perror("realloc failed in read_input: ");
                quick_exit(1);
            }
        }
    }

    retVal[position] = '\0';
    return retVal;
}

char** parse_commands(char* input){
    int commands_size = COMMANDS_SIZE;
    char** commands = calloc(commands_size, sizeof(char*));
    char* command = NULL;

    if(commands == NULL){
        perror("calloc failed in parse_commands: ");
        exit(1);
    }

    command = strtok(input, DELIMITER);
    int position = 0;
    while(command != NULL){
        commands[position] = command;
        position += 1;
        if(position >= commands_size){
            commands_size += COMMANDS_SIZE;
            commands = realloc(commands, commands_size * sizeof(char*));
            if(commands == NULL) {
                perror("realloc failed in parse_argument: ");
                exit(1);
            }
        }
        command = strtok(NULL, DELIMITER);
    }

    return commands;
}