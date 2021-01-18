//
// Created by xgao on 4/8/20.
//

#ifndef HW2_PARSER_H
#define HW2_PARSER_H

#define BUFFER_SIZE 1024
#define COMMANDS_SIZE 64
#define DELIMITER " \t\r\n\a"

char* read_input(void);
char** parse_commands(char* input);

#endif //HW2_PARSER_H
