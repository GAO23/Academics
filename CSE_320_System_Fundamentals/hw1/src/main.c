#include <stdio.h>
#include <stdlib.h>

#include "const.h"
#include "debug.h"

#ifdef _STRING_H
#error "Do not #include <string.h>. You will get a ZERO."
#endif

#ifdef _STRINGS_H
#error "Do not #include <strings.h>. You will get a ZERO."
#endif

#ifdef _CTYPE_H
#error "Do not #include <ctype.h>. You will get a ZERO."
#endif

int main(int argc, char **argv)
{


    int ret = EXIT_SUCCESS;
    if(validargs(argc, argv))
        USAGE(*argv, EXIT_FAILURE);

    if(global_options & 1)
        USAGE(*argv, EXIT_SUCCESS);

    // doing this because we cant import shit from elsewhere, we can't set path when validargs, and we can't write another function in main. Annoying.....
    char* path_para_original = "-p";
    char* path_para = path_para_original;
    for(int i = 0; i < argc; i++){
        char* current_arg = *argv;
        while(*path_para != 0){
            if(*path_para != *current_arg)
                break;
            path_para += 1;
            current_arg += 1;
        }
        if(*path_para == 0 && *current_arg == 0){
            path_init(*(argv+1));
            break;
        }
        argv += 1;
        path_para = path_para_original;
    }

    if(global_options & 0b0100){
        ret = deserialize();
    }

    if(global_options & 0b0010){
        ret = serialize();
    }

    fflush(stdout);

    if(ret == -1){
        return EXIT_FAILURE;
    }
    if(ret == 0){
        EXIT_SUCCESS;
    }
}

/*
 * Just a reminder: All non-main functions should
 * be in another file not named main.c
 */
