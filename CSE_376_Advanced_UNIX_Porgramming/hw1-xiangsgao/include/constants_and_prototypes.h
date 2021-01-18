//
// Created by xgao on 2/10/20.
//

#ifndef HW1_CONSTANTS_AND_PROTOTYPES_H
#define HW1_CONSTANTS_AND_PROTOTYPES_H

#include <unistd.h>

#define NO_DEBUG 0x00
#define FUNCTION_DEBUG 0x01
#define LIBRARY_DEBUG 0x02
#define SYSCALL_DEBUG 0x04
#define ARGUMENT_DEBUG 0x10
#define RETURN_DEBUG 0x20
#define ENCRYPT 0
#define DECRYPT 1
#define VERSION "\nversion 0.1\n"
#define MODE_ERROR_MSG "either -e or -d but not both\n"
#define USAGE_MSG "filesec [-devh] [-D DBGVAL] [-p PASSFILE] infile outfile\n"
#define MODE_MISSING_MSG "please specify a -e or -d flag\n"
#define IN_OUT_MISSING_MSG "please provide exactly one input and one output file\n"
#define TEMP_FILE "./.filesec_temp"
extern int page_size; // shared varible

typedef unsigned char byte;




#endif //HW1_CONSTANTS_AND_PROTOTYPES_H
