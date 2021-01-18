cmake_minimum_required(VERSION 3.14)
project(HW2 C)
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")
set(CMAKE_C_STANDARD 11)
file(GLOB source_files
        "include/*.h"
        "include/toolsdir/*.h"
        "src/*.c"
        "src/toolsdir/*.c"
        )


include_directories("/usr/include/criterion/") # get the header files

add_executable(HW2 ${source_files} include/functions.h include/functions.h)

add_executable(HW2_Test tests/base_tests.c)

target_link_libraries(HW2_Test "/usr/lib/libcriterion.so") # get the actual libraries for header files to link to.