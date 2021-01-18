#include "errno.h"
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>

#include "../include/constants_and_prototypes.h"
#include "../include/crypto.h"

int page_size; // dynamically resolve so no const and static
static int mode = -1;
static char* passFilePath = NULL;
static unsigned char* password = NULL;
static char* inPath;
static char* outPath;
static  int inFileFd = -1;
static  int outFileFd = -1;

// this is to store all the debug flags
static struct {
    unsigned int no_debug;
    unsigned int function_debug; // before and after exiting function, print function name then exit or enter
    unsigned int library_debug; // before and after library function call, print function name and exit/enter
    unsigned int syscall_debug; // before and after syscall, print name and exit/enter
    unsigned int arg_debug; // print arguments to the above
    unsigned int return_debug; // print the return values to all the above
} debug_flags = {NO_DEBUG, FUNCTION_DEBUG, LIBRARY_DEBUG, SYSCALL_DEBUG, ARGUMENT_DEBUG, RETURN_DEBUG};

static void usage(char* additional_msg);
static void debug_init(char* debug_val);
static void print_function_debug(const char* function_name, char* mode, char* args, char* return_val);
static void library_function_debug(char* function_name, char* mode, char* args, char* return_val);
static void syscall_function_debug(char* function_name, char* mode, char* args, char* return_val);
static void encrypt_file(void);
static void decrypt_file(void);
static void handle_files(void);
static void ask_for_password(void);

int main(int argc, char** argv){

    char c;
    int parseCount = 0;
    while((c = getopt(argc, argv, "devhp:D:")) != -1){
        switch (c){
            case 'D':
                debug_init(optarg);
                parseCount +=2;
                break;
            case 'd':
                if(mode != -1) usage(MODE_ERROR_MSG);
                mode = DECRYPT;
                parseCount +=1;
                break;
            case 'e':
                if(mode != -1) usage(MODE_ERROR_MSG);
                mode = ENCRYPT;
                parseCount +=1;
                break;
            case 'v':
                printf(VERSION);
                parseCount +=1;
                break;
            case 'h':
                usage(NULL);
                parseCount +=1;
                break;
            case 'p':{
                size_t len = strlen(optarg) + 1;
                passFilePath = malloc(len);
                memcpy(passFilePath, optarg, len);
                parseCount +=2;
            }
                break;
            case '?':
                usage(NULL);
                break;
            default:
                perror("error");
        }
    }

    if(mode == -1) usage(MODE_MISSING_MSG);
    if(parseCount + 3 != argc) usage(IN_OUT_MISSING_MSG);
    inPath = argv[argc - 2];
    outPath = argv[argc - 1];

    syscall_function_debug("getpagesize", "entering", "void", NULL);
    page_size = getpagesize();
    syscall_function_debug("getpagesize", "exiting", NULL, "pagesize");

    handle_files();

    if(password == NULL)
        ask_for_password();

    print_function_debug("crypto_init", "entering", (char*) password, NULL);
    crypto_init(password, (size_t)strlen((char* )password));
    print_function_debug("crypto_init", "exiting", NULL, "void");

    if(mode == DECRYPT)
        decrypt_file();
    else
        encrypt_file();


    return 0;
}

// encrypting a file
static void encrypt_file(void){
    print_function_debug(__func__, "entering", "void", NULL);
    byte input[page_size];
    byte out_buffer[page_size];
    int bytes_read;
    syscall_function_debug("read", "entering", "inFilefd", NULL);
    while((bytes_read = read(inFileFd, &input, page_size)) > 0){
        syscall_function_debug("read", "exiting", NULL, "bytes_read");
        int encrypted_bytes = encrypt(input, bytes_read, out_buffer);
        syscall_function_debug("write", "entering", "outFileFd", NULL);
        if(write(outFileFd, out_buffer, encrypted_bytes) != encrypted_bytes){
            perror("write failed");
            close(outFileFd);
            unlink(TEMP_FILE);
            exit(2);
        }
        syscall_function_debug("write", "exiting", NULL, "bytes_written");
    }

    if(errno){
        perror("error while reading input file");
        exit(2);
    }

    syscall_function_debug("close", "entering", "inFileFd", NULL);
    if(close(inFileFd) < 0){
        perror("can't close inFileFd");
        exit(2);
    }
    syscall_function_debug("close", "exiting", NULL, "0");

    syscall_function_debug("close", "entering", "outFileFd", NULL);
    if(close(outFileFd) < 0){
        perror("can't close outFileFd");
        exit(2);
    }
    syscall_function_debug("close", "exiting", NULL, "0");

    // this is to rename the temporary output file after success.
    if(strcmp(outPath, "-")){ // do not call if output is directed to stdout
        syscall_function_debug("access", "entering", outPath, NULL);
        if( access( outPath, F_OK ) != -1 )
            unlink(outPath);
        rename(TEMP_FILE, outPath); // rename the temp file to the new file
    }

    // delete the temp file after we finished
    unlink(TEMP_FILE);
    print_function_debug(__func__, "exiting", NULL, "void");
}

// decrypting a file
static void decrypt_file(void){
    print_function_debug(__func__, "entering", "void", NULL);
    byte input[page_size];
    byte out_buffer[page_size];
    int bytes_read;
    syscall_function_debug("read", "entering", "inFilefd", NULL);
    while((bytes_read = read(inFileFd, &input, page_size)) > 0){
        syscall_function_debug("read", "exiting", NULL, "bytes_read");
        int decrypted_bytes = decrypt(input, bytes_read, out_buffer);
        syscall_function_debug("write", "entering", "outFileFd", NULL);
        if(write(outFileFd, out_buffer, decrypted_bytes) != decrypted_bytes){
            perror("write failed");
            close(outFileFd);
            unlink(TEMP_FILE);
            exit(2);
        }
        syscall_function_debug("write", "exiting", NULL, "bytes_written");
    }

    if(errno){
        perror("error while reading input file");
        exit(2);
    }

    syscall_function_debug("close", "entering", "inFileFd", NULL);
    if(close(inFileFd) < 0){
        perror("can't close inFileFd");
        exit(2);
    }
    syscall_function_debug("close", "exiting", NULL, "0");

    syscall_function_debug("close", "entering", "outFileFd", NULL);
    if(close(outFileFd) < 0){
        perror("can't close outFileFd");
        exit(2);
    }
    syscall_function_debug("close", "exiting", NULL, "0");

    // this is to rename the temporary output file after success.
    if(strcmp(outPath, "-")){ // do not call if output is directed to stdout
        if( access( outPath, F_OK ) != -1 )
            unlink(outPath);
        rename(TEMP_FILE, outPath); // rename the temp file to the new file
    }

    // delete the temp file after we finished
    unlink(TEMP_FILE);
    print_function_debug(__func__, "exiting", NULL, "void");
}


// parse the debug flag and initialize the debug struct
static void debug_init(char* debug_val) {
    unsigned int *intPtr = (unsigned int *) &(debug_flags.no_debug);
    unsigned int debug_argument = (unsigned int) strtol(debug_val, NULL, 0);
    *intPtr |= debug_argument; // this sets the no debug flag
    if (!(*intPtr)) return; // no need to check further cuz no debug flag is false

    // sets the rest of the debug flag
    intPtr += 1;
    for (int i = 0; i < 5; i++) {
        *intPtr = *intPtr & debug_argument;
        intPtr += 1;
    }

}

// printing functions for debugging
static void print_function_debug(const char* function_name, char* mode, char* args, char* return_val){
    if(!debug_flags.no_debug)
        return;

    if(!debug_flags.function_debug)
        return;

    fprintf(stderr, "now %s function %s\n", mode, function_name);

    if(debug_flags.arg_debug && args != NULL)
        fprintf(stderr, "with the arg: %s\n", args);

    if(debug_flags.return_debug && return_val != NULL)
        fprintf(stderr, "with the return value of %s\n", return_val);

}

// printing syscalls for debugging
static void syscall_function_debug(char* function_name, char* mode, char* args, char* return_val){
    if(!debug_flags.no_debug)
        return;

    if(!debug_flags.syscall_debug)
        return;

    fprintf(stderr, "now %s syscall %s\n", mode, function_name);

    if(debug_flags.arg_debug && args != NULL)
        fprintf(stderr, "with the arg: %s\n", args);

    if(debug_flags.return_debug && return_val != NULL)
        fprintf(stderr, "with the return value of %s\n", return_val);

}

// printing library calls for debugging
static void library_function_debug(char* function_name, char* mode, char* args, char* return_val){
    if(!debug_flags.no_debug)
        return;

    if(!debug_flags.library_debug)
        return;

    fprintf(stderr, "now %s library call %s\n", mode, function_name);

    if(debug_flags.arg_debug && args != NULL)
        fprintf(stderr, "with the arg: %s\n", args);

    if(debug_flags.return_debug && return_val != NULL)
        fprintf(stderr, "with the return value of %s\n", return_val);

}

// printing out usage info and then exit because user does not know how to use the program
static void usage(char* additional_msg){
    if(additional_msg != NULL){
        fprintf(stderr, "%s", additional_msg);
    }

    fprintf(stderr, "%s", USAGE_MSG);
    exit(1);
}

// asking user for password when password file is not present
static void ask_for_password(void){
    print_function_debug("ask_for_password", "entering", "void", NULL);

    char* userInput;
    library_function_debug("getpass", "entering", "Enter a password", NULL);
    if((userInput = getpass("Enter a password: ")) == NULL){
        library_function_debug("getpass", "exiting", NULL, userInput);
        perror("error in ask_for_password");
        exit(2);
    }
    library_function_debug("strlen", "entering", userInput, NULL);
    size_t len = strlen(userInput) + 1; // to compensate for the null terminator
    library_function_debug("strlen", "exiting", NULL, "whatever the user input size is");
    library_function_debug("malloc", "entering", "user input len + 1", NULL);
    password = malloc(len);
    library_function_debug("malloc", "exiting", NULL, "user input ptr");
    if(password == NULL){
        perror("malloc error");
        exit(2);
    }
    library_function_debug("memcpy", "entering", "password, userInput, len", NULL);
    memcpy(password, userInput, len);
    library_function_debug("memcpy", "exiting", NULL, "void");

    print_function_debug("ask_for_password", "exiting", NULL, "void");
}

// opening the input and output file as well as reading the password file
static void handle_files(void){
    print_function_debug("handle_files", "entering", "void", NULL);
    struct stat st;

    // first read the password file
    if(passFilePath != NULL){
        syscall_function_debug("open", "entering", passFilePath, NULL);
        int passFileFd = open(passFilePath, O_RDONLY);
        if(passFileFd < 0){
            perror("can't open password file");
            exit(2);
        }
        syscall_function_debug("open", "exiting", NULL, "file descriptor");

        library_function_debug("stat", "entering", passFilePath, NULL);
        stat(passFilePath, &st);
        library_function_debug("stat", "exiting", NULL, "NULL");
        size_t len = st.st_size;
        if(len > page_size){
            fprintf(stderr, "your password file is too long, keep it to  %d bytes\n", page_size);
            exit(1);
        }
        library_function_debug("malloc", "entering", "len of the password file", NULL);
        password = malloc(len);
        library_function_debug("malloc", "exiting", NULL, (password == NULL)? "NULL" : "ptr to a char");

        if(password == NULL){
            perror("malloc failed");
            exit(2);
        }

        syscall_function_debug("read", "entering", "password file descriptor", NULL);
        if(read(passFileFd, password, len) < 1){
            if(errno != 0)
                perror("can't read password file");
            else
                fprintf(stderr, "%s\n", "nothing was read, blank file?");

            exit(2);
        }
        syscall_function_debug("read", "exiting", NULL, "bytes read");

        syscall_function_debug("close", "entering", "passfile descriptor", NULL);
        if(close(passFileFd) < 0){
            perror("can't close file descriptor");
            exit(2);
        }
        syscall_function_debug("close", "exiting",  NULL, "0");
    }

    // now opens the input file
    if(strcmp(inPath, "-") == 0){
        inFileFd = STDIN_FILENO;
    }else{
        syscall_function_debug("open", "entering", inPath, NULL);
        if((inFileFd = open(inPath, O_RDONLY)) < 0){
            perror("can't open input file");
            exit(2);
        }
        syscall_function_debug("open", "exiting", NULL, "inFileFd");
    }

    // now opens the output files
    if(strcmp(outPath, "-") == 0)
        outFileFd = STDOUT_FILENO;
    else{
        syscall_function_debug("open", "entering", TEMP_FILE, NULL);
        if((outFileFd = open(TEMP_FILE,O_CREAT | O_WRONLY | O_TRUNC, 0655)) < 0){
            perror("can't open output file");
            exit(2);
        }
        syscall_function_debug("open", "exiting", NULL, "outFileFd");
    }

    print_function_debug("handle_files", "exiting", NULL, "void");
}