#include "const.h"
#include "transplant.h"
#include "debug.h"
#include <errno.h>
#ifdef _STRING_H
#error "Do not #include <string.h>. You will get a ZERO."
#endif

#ifdef _STRINGS_H
#error "Do not #include <strings.h>. You will get a ZERO."
#endif

#ifdef _CTYPE_H
#error "Do not #include <ctype.h>. You will get a ZERO."
#endif

/*
 * You may modify this file and/or move the functions contained here
 * to other source files (except for main.c) as you wish.
 *
 * IMPORTANT: You MAY NOT use any array brackets (i.e. [ and ]) and
 * you MAY NOT declare any arrays or allocate any storage with malloc().
 * The purpose of this restriction is to force you to use pointers.
 * Variables to hold the pathname of the current file or directory
 * as well as other data have been pre-declared for you in const.h.
 * You must use those variables, rather than declaring your own.
 * IF YOU VIOLATE THIS RESTRICTION, YOU WILL GET A ZERO!
 *
 * IMPORTANT: You MAY NOT use floating point arithmetic or declare
 * any "float" or "double" variables.  IF YOU VIOLATE THIS RESTRICTION,
 * YOU WILL GET A ZERO!
 */




// additional shits
static const int SET_SUCCESS = 1;
static const int SET_FAILED = -1;
typedef unsigned char byte;
typedef unsigned char* byte_ptr;



///////////////////////////////////////////////////////////////////////////////// added functions /////////////////////////////////////////////////////////////////////////////////////////////////////

static void output_name(char* name){
    while(*name != 0){
        putchar(*name);
        name += 1;
    }
}

static int push_one_char(char arg){
    if((path_length + 1) >= PATH_MAX){
        return -1;
    }
    char* temp = path_buf;
    temp += path_length;
    *temp = arg;
    *(temp + 1) = 0;
    path_length += 1;
    return 0;
}

static size_t get_length(char* string){
    int retval = 0;
    while(*string != 0){
        string += 1;
        retval += 1;
    }

    return retval;
}

// returns 0 if the strings are not equal, returns 1 if they are equal
static int string_equal(char* string_one, char* string_two){

    if(get_length(string_one) != get_length(string_two))
        return 0;

    while(*string_one != 0){
        if(*string_one != *string_two){
            return 0;
        }
        string_one += 1;
        string_two += 1;
    }

    return 1;
}

static int set_global(char** argv, int p_flag){
    int temp = global_options;
    if(string_equal(*argv, "-s") && p_flag == 0){
        global_options = global_options | 0x2;
        return SET_SUCCESS;
    } else if(string_equal(*argv, "-d") && p_flag == 0){
        global_options = global_options | 0x4;
        return SET_SUCCESS;
    } else if (string_equal(*argv, "-c") && p_flag == 0){
        global_options = global_options | 0x8;
        return SET_SUCCESS;
    } else{
        if(p_flag && !(string_equal(*argv, "-s") || string_equal(*argv, "-d") || string_equal(*argv, "-c")))
            return SET_SUCCESS;
        return SET_FAILED;
    }
}

static int check_magic_num(void){
    byte current_byte;
    unsigned long result = 0;
    for(int i = 0; i < 3; i++){
        current_byte = getchar();
        result = result << 8;
        result = result | current_byte;
    }
    if(result == 0b000011000000110111101101){
        return 0;
    }else{
        return -1;
    }
}



static unsigned long get_record_depth(void){
    byte current_byte;
    unsigned long result = 0;
    for(int i = 0; i < 4; i++){
        current_byte = getchar();
        result = result << 8;
        result = result | current_byte;
    }
    return result;
}

static unsigned long get_mode_t(void){
    byte current_byte;
    unsigned long result = 0;
    for(int i = 0; i < 4; i++){
        current_byte = getchar();
        result = result << 8;
        result = result | current_byte;
    }
    return result;
}

static unsigned long get_record_size(void){
    byte current_byte;
    unsigned long result = 0;
    for(int i = 0; i < 8; i++){
        current_byte = getchar();
        result = result << 8;
        result = result | current_byte;
    }
    return result;
}

static unsigned long get_off_t(void){
    byte current_byte;
    unsigned long result = 0;
    for(int i = 0; i < 8; i++){
        current_byte = getchar();
        result = result << 8;
        result = result | current_byte;
    }
    return result;
}

static int move_over_12(unsigned int expected_depth){
    int depth;
    if((depth = get_record_depth()) == -1)
        return -1;

    if(depth != expected_depth)
        return -1;

    for(int i = 0; i < 8; i++){
        if(getchar() == EOF){
            return -1;
        }
    }

    return 0;
}

static int make_dir(void){
    int c = (global_options & 0b01000);
    struct stat st = {0};
    if(c == 0 && (stat(path_buf, &st) == 0))
        return -1;

    if (stat(path_buf, &st) == -1) {
        if(mkdir(path_buf, 0700) == -1)
            return -1;
    }
    return 0;

}


static int handle_entry_record(int expected_depth){
    int depth;
    if((depth = get_record_depth()) == -1)
        return -1;

    if(depth != expected_depth)
        return -1;

    unsigned long size = get_record_size();
    unsigned long mode = get_mode_t();
    unsigned long off = get_off_t();
    size -= 16;
    size -= 12;
    push_one_char('/');
    for(int i = 0; i < size; i++){
        unsigned int byte;
        if((byte = getchar()) == EOF)
            return -1;
        push_one_char((char)byte);
    }
    if(S_ISDIR(mode)){
        if(make_dir()== -1)
            return -1;
        if(chmod(path_buf, mode & 0777) == -1)
            return -1;
    } else if(S_ISREG(mode)){
        if(deserialize_file(expected_depth) == -1)
            return -1;
        if(chmod(path_buf, mode & 0777) == -1)
            return -1;
        path_pop();
    }

    if(check_magic_num() == -1)
        return -1;

    return 0;
}

static void make_file(FILE** file){
    int c = (global_options & 0b01000);
    if(c){
        *file = fopen(path_buf, "w+");
        return;
    }
    struct stat  buffer;
    if(stat (path_buf, &buffer) == 0){
        *file = NULL;
        return;
    }
    *file = fopen(path_buf, "w");

}

static void output_magic(void){
    byte first, second, third;
    first = 0x0C;
    second = 0x0D;
    third = 0xED;
    putchar(first);
    putchar(second);
    putchar(third);

}

static void output_bytes(byte_ptr ptr, size_t len) {
    int i;
    ptr += len -1;


    for (i = 0; i < len; i++) {
        putchar(*ptr);
        ptr -= 1;
    }
}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


/*
 * A function that returns printable names for the record types, for use in
 * generating debugging printout.
 */
static char *record_type_name(int i) {
    switch(i) {
    case START_OF_TRANSMISSION:
	return "START_OF_TRANSMISSION";
    case END_OF_TRANSMISSION:
	return "END_OF_TRANSMISSION";
    case START_OF_DIRECTORY:
	return "START_OF_DIRECTORY";
    case END_OF_DIRECTORY:
	return "END_OF_DIRECTORY";
    case DIRECTORY_ENTRY:
	return "DIRECTORY_ENTRY";
    case FILE_DATA:
	return "FILE_DATA";
    default:
	return "UNKNOWN";
    }
}

/*
 * @brief  Initialize path_buf to a specified base path.
 * @details  This function copies its null-terminated argument string into
 * path_buf, including its terminating null byte.
 * The function fails if the argument string, including the terminating
 * null byte, is longer than the size of path_buf.  The path_length variable 
 * is set to the length of the string in path_buf, not including the terminating
 * null byte.
 *
 * @param  Pathname to be copied into path_buf.
 * @return 0 on success, -1 in case of error
 */
int path_init(char *name) {
    int length = get_length(name);
    char* temp =  path_buf;
    if(length > PATH_MAX - 1) // length doesnt account for null terminator
        return -1;

    while(*name != 0){
        *temp = *name;
        temp += 1;
        name += 1;
    }

    *temp = 0;
    path_length = length;

    return 0;
}

/*
 * @brief  Append an additional component to the end of the pathname in path_buf.
 * @details  This function assumes that path_buf has been initialized to a valid
 * string.  It appends to the existing string the path separator character '/',
 * followed by the string given as argument, including its terminating null byte.
 * The length of the new string, including the terminating null byte, must be
 * no more than the size of path_buf.  The variable path_length is updated to
 * remain consistent with the length of the string in path_buf.
 * 
 * @param  The string to be appended to the path in path_buf.  The string must
 * not contain any occurrences of the path separator character '/'.
 * @return 0 in case of success, -1 otherwise.
 */
int path_push(char *name) {
    int name_length = get_length(name);
    int total = name_length + path_length; // total number of letters after concatenate strings
    total += 2; // plus the null terminator and the file separator to account for them

    if(total > PATH_MAX)
        return -1;

    // move the pointer to the null terminator
    char* temp = path_buf;
    temp += path_length;

    *temp = '/'; // append the path separator
    temp += 1; // move to the next position for appending

    while(*name != 0){
        *temp = *name;
        temp += 1;
        name += 1;
    }

    *temp = 0; // append the null terminator
    path_length = total - 1;
    return 0;

}

/*
 * @brief  Remove the last component from the end of the pathname.
 * @details  This function assumes that path_buf contains a non-empty string.
 * It removes the suffix of this string that starts at the last occurrence
 * of the path separator character '/'.  If there is no such occurrence,
 * then the entire string is removed, leaving an empty string in path_buf.
 * The variable path_length is updated to remain consistent with the length
 * of the string in path_buf.  The function fails if path_buf is originally
 * empty, so that there is no path component to be removed.
 *
 * @return 0 in case of success, -1 otherwise.
 */
int path_pop() {
    if(path_length <= 0)
        return -1;

    int removed_chars = 0;
    char* temp = path_buf +  path_length; // pointer to the null terminator, -1 to compensate for the count of null terminate, + 1 to compensate for the removal of '/' so 0 to start
    while(*temp != '/' && removed_chars < path_length){
        *temp = 0;
        temp -= 1;
        removed_chars += 1;
    }

    *temp = 0;
    path_length = path_length - removed_chars;
    return 0;


}

/*
 * @brief Deserialize directory contents into an existing directory.
 * @details  This function assumes that path_buf contains the name of an existing
 * directory.  It reads (from the standard input) a sequence of DIRECTORY_ENTRY
 * records bracketed by a START_OF_DIRECTORY and END_OF_DIRECTORY record at the
 * same depth and it recreates the entries, leaving the deserialized files and
 * directories within the directory named by path_buf.
 *
 * @param depth  The value of the depth field that is expected to be found in
 * each of the records processed.
 * @return 0 in case of success, -1 in case of an error.  A variety of errors
 * can occur, including depth fields in the records read that do not match the
 * expected value, the records to be processed to not being with START_OF_DIRECTORY
 * or end with END_OF_DIRECTORY, or an I/O error occurs either while reading
 * the records from the standard input or in creating deserialized files and
 * directories.
 */
int deserialize_directory(int depth) {
    if(check_magic_num() == -1){
        return -1;
    }

    if((getchar()) != START_OF_DIRECTORY)
        return -1;

    if(move_over_12(depth) == -1)
        return -1;


    if(check_magic_num() == -1)
        return -1;

    int num_directory = 1;
    while(1){
        int type = getchar();
        if(type == EOF){
            return -1;
        }else if(type == DIRECTORY_ENTRY){
            if(handle_entry_record(depth) == -1)
                return -1;
        } else if(type == START_OF_DIRECTORY){
            num_directory += 1;
            depth += 1;
            if(move_over_12(depth) == -1)
                return -1;
            if(check_magic_num() == -1)
                return -1;
        } else if(type == END_OF_DIRECTORY){
            num_directory -= 1;
            if(move_over_12(depth) == -1)
                return -1;
            depth -= 1;
            if(check_magic_num() == -1)
                return -1;
            path_pop();
        }else{
            return -1;
        }

        if(num_directory == 0)
            break;
    }
    return 0;
}

/*
 * @brief Deserialize the contents of a single file.
 * @details  This function assumes that path_buf contains the name of a file
 * to be deserialized.  The file must not already exist, unless the ``clobber''
 * bit is set in the global_options variable.  It reads (from the standard input)
 * a single FILE_DATA record containing the file content and it recreates the file
 * from the content.
 *
 * @param depth  The value of the depth field that is expected to be found in
 * the FILE_DATA record.
 * @return 0 in case of success, -1 in case of an error.  A variety of errors
 * can occur, including a depth field in the FILE_DATA record that does not match
 * the expected value, the record read is not a FILE_DATA record, the file to
 * be created already exists, or an I/O error occurs either while reading
 * the FILE_DATA record from the standard input or while re-creating the
 * deserialized file.
 */
int deserialize_file(int depth){
    if(check_magic_num() == -1)
        return -1;

    if(getchar() != FILE_DATA)
        return -1;

    unsigned long this_depth = get_record_depth();
    if(this_depth != (unsigned long)depth)
        return -1;

    unsigned long size = get_record_size();

    size -= 16;
    FILE* file = NULL;
    make_file(&file);
    if(file  == NULL){
        return -1;
    }

    for(int i = 0; i < size; i++){
        char byte;
        if((byte = getchar()) == EOF){
            return -1;
        }
        if(fputc(byte, file) == EOF){
            return -1;
        }
    }
    fclose(file);
    return 0;
}

/*
 * @brief  Serialize the contents of a directory as a sequence of records written
 * to the standard output.
 * @details  This function assumes that path_buf contains the name of an existing
 * directory to be serialized.  It serializes the contents of that directory as a
 * sequence of records that begins with a START_OF_DIRECTORY record, ends with an
 * END_OF_DIRECTORY record, and with the intervening records all of type DIRECTORY_ENTRY.
 *
 * @param depth  The value of the depth field that is expected to occur in the
 * START_OF_DIRECTORY, DIRECTORY_ENTRY, and END_OF_DIRECTORY records processed.
 * Note that this depth pertains only to the "top-level" records in the sequence:
 * DIRECTORY_ENTRY records may be recursively followed by similar sequence of
 * records describing sub-directories at a greater depth.
 * @return 0 in case of success, -1 otherwise.  A variety of errors can occur,
 * including failure to open files, failure to traverse directories, and I/O errors
 * that occur while reading file content and writing to standard output.
 */
int serialize_directory(int depth) {
    DIR* dir = NULL;
    dir = opendir(path_buf);
    if(dir == NULL)
        return -1;

    output_magic();
    byte type = START_OF_DIRECTORY;
    putchar(type);
    output_bytes((byte_ptr) &depth, sizeof(int));
    off_t size = 16;
    output_bytes((byte_ptr) & size, sizeof(off_t));
    struct dirent* de = NULL;
    errno = 0;
    while((de = readdir(dir)) != NULL){
        if(errno != 0)
            return -1;
        char* name = de->d_name;
        if(string_equal(name, ".") ||  string_equal(name, ".."))
            continue;
        if(path_push(name) == -1)
            return -1;
        struct stat stat_buf;
        if(stat(path_buf, &stat_buf) == -1)
            return -1;
        output_magic();
        type = DIRECTORY_ENTRY;
        putchar(type);
        output_bytes((byte_ptr) &depth, sizeof(int));
        size = 12 + 16 + get_length(name);
        output_bytes((byte_ptr) &size, sizeof(off_t));
        output_bytes((byte_ptr) &(stat_buf.st_mode), sizeof(mode_t));
        output_bytes((byte_ptr) &(stat_buf.st_size), sizeof(off_t));
        output_name(name);

        if(S_ISDIR(stat_buf.st_mode)){
            if(serialize_directory(depth + 1) == -1)
                return -1;
            if(path_pop()==-1)
                return -1;
        }else if(S_ISREG(stat_buf.st_mode)){
            if(serialize_file(depth, stat_buf.st_size)==-1)
                return -1;
            if(path_pop()==-1)
                return -1;
        }
        errno = 0;
    }

    output_magic();
    type = END_OF_DIRECTORY;
    putchar(type);
    output_bytes((byte_ptr) &depth, sizeof(int));
    size = 16;
    output_bytes((byte_ptr) &size, sizeof(off_t));
    closedir(dir);
    return 0;
}

/*
 * @brief  Serialize the contents of a file as a single record written to the
 * standard output.
 * @details  This function assumes that path_buf contains the name of an existing
 * file to be serialized.  It serializes the contents of that file as a single
 * FILE_DATA record emitted to the standard output.
 *
 * @param depth  The value to be used in the depth field of the FILE_DATA record.
 * @param size  The number of bytes of data in the file to be serialized.
 * @return 0 in case of success, -1 otherwise.  A variety of errors can occur,
 * including failure to open the file, too many or not enough data bytes read
 * from the file, and I/O errors reading the file data or writing to standard output.
 */
int serialize_file(int depth, off_t size) {
    FILE* file = fopen(path_buf, "r");
    if(file == NULL)
        return -1;
    output_magic();
    putchar(FILE_DATA);
    output_bytes((byte_ptr) &depth, sizeof(int));
    off_t arg = size;
    arg += 16;
    output_bytes((byte_ptr) &arg, sizeof(off_t));
    int read;
    while(1){
        read = fgetc(file);
        if(read == EOF)
            break;
        char byte = (char) read;
        putchar(byte);
        size -= 1;
    }
    if(size != 0)
        return -1;

    return 0;
}

/**
 * @brief Serializes a tree of files and directories, writes
 * serialized data to standard output.
 * @details This function assumes path_buf has been initialized with the pathname
 * of a directory whose contents are to be serialized.  It traverses the tree of
 * files and directories contained in this directory (not including the directory
 * itself) and it emits on the standard output a sequence of bytes from which the
 * tree can be reconstructed.  Options that modify the behavior are obtained from
 * the global_options variable.
 *
 * @return 0 if serialization completes without error, -1 if an error occurs.
 */
int serialize() {
    if(path_length == 0){
        if(path_init(".") == -1)
            return -1;
    }

    byte type = START_OF_TRANSMISSION;
    output_magic();
    putchar(type);
    unsigned int depth = 0;
    output_bytes((byte_ptr) &depth, sizeof(int));
    off_t size = 16;
    output_bytes((byte_ptr) &size, sizeof(off_t));

    if(serialize_directory(1) == -1)
        return -1;

    type = END_OF_TRANSMISSION;
    output_magic();
    putchar(type);
    output_bytes((byte_ptr) &depth, sizeof(int));
    output_bytes((byte_ptr) &size, sizeof(off_t));
    return 0;
}

/**
 * @brief Reads serialized data from the standard input and reconstructs from it
 * a tree of files and directories.
 * @details  This function assumes path_buf has been initialized with the pathname
 * of a directory into which a tree of files and directories is to be placed.
 * If the directory does not already exist, it is created.  The function then reads
 * from from the standard input a sequence of bytes that represent a serialized tree
 * of files and directories in the format written by serialize() and it reconstructs
 * the tree within the specified directory.  Options that modify the behavior are
 * obtained from the global_options variable.
 *
 * @return 0 if deserialization completes without error, -1 if an error occurs.
 */
int deserialize() {
    if(path_length == 0){
        path_init(".");
    }else {
        struct stat st = {0};
        if (stat(path_buf, &st) == -1) {
            if(mkdir(path_buf, 0700) == -1)
                return -1;
        }
    }

    if(check_magic_num() == -1)
        return -1;
    char type = getchar();
    if(type != START_OF_TRANSMISSION){
        return -1;
    }
    for(int i = 0; i < 12; i++){
        if(getchar() == EOF){
            return -1;
        }
    }
    if(deserialize_directory(1) == -1){
        return -1;
    }

    type = getchar();
    if(type != END_OF_TRANSMISSION)
        return -1;

    return 0;
}

/**
 * @brief Validates command line arguments passed to the program.
 * @details This function will validate all the arguments passed to the
 * program, returning 0 if validation succeeds and -1 if validation fails.
 * Upon successful return, the selected program options will be set in the
 * global variable "global_options", where they will be accessible
 * elsewhere in the program.
 *
 * @param argc The number of arguments passed to the program from the CLI.
 * @param argv The argument strings passed to the program from the CLI.
 * @return 0 if validation succeeds and -1 if validation fails.
 * Refer to the homework document for the effects of this function on
 * global variables.
 * @modifies global variable "global_options" to contain a bitmap representing
 * the selected options.
 */
int validargs(int argc, char **argv)
{

    if(argc == 1 || argc > 5){
        return -1;
    }

    argv += 1;

    if(string_equal(*argv, "-h")){
        global_options = global_options | 0x1;
        return 0;
    }

    if(!string_equal(*argv, "-d") && !string_equal(*argv, "-s")){
        return -1;
    }

    int count = 1;
    int after_p = 0;
    while(1){
        count += 1;
        int p_flag = string_equal(*argv, "-p");

        if(p_flag == 1){
            if(argc == count){
                global_options = 0x0;
                return -1;
            }
            if(set_global(argv+1, p_flag) == SET_FAILED){
                global_options = 0x0;
                return -1;
            }
            argv += 1;
            after_p = 1;
            continue;
        }

        if(after_p == 1){
            argv += 1;
            after_p = 0;
            if(count == argc)
                break;
            continue;
        }

        if (set_global(argv, p_flag) == SET_FAILED){
            global_options = 0x0;
            return -1;
        }

        if (count == argc){
            break;
        }
        argv += 1;
    }




    if((global_options & 0x6) == 0x6){
        global_options = 0x0;
        return -1;
    }

    if((global_options & 0xa) == 0xa){
        global_options = 0x0;
        return -1;
    }

    return 0;
}





