cmake_minimum_required(VERSION 3.14)
project(HW2 C)
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")
set(CMAKE_C_STANDARD 11)
file(GLOB source_files
        "include/*.h"
        "src/*.c"
        "lib/*.o"
        )


include_directories("/usr/include/criterion/") # get the header files

add_executable(HW3 ${source_files})

add_executable(HW3_Test tests/sfmm_tests.c include/sfmm.h include/debug.h lib/sfutil.o src/sfmm.c)

target_link_libraries(HW3_Test "/usr/lib/libcriterion.so") # get the actual libraries for header files to link to.