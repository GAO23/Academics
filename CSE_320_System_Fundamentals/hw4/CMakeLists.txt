cmake_minimum_required(VERSION 3.14)
project(HW4 C)
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")
set(CMAKE_C_STANDARD 11)
file(GLOB source_files
        "include/*.h"
        "src/*.c"
        "lib/*.o"
        )


include_directories("/usr/include/criterion/") # get the header files

add_executable(HW4 ${source_files})

add_executable(HW4_Test tests/hw4_tests.c ${souce_files})

target_link_libraries(HW4_Test "/usr/lib/libcriterion.so") # get the actual libraries for header files to link to.