cmake_minimum_required(VERSION 3.10)
project(HW2 C)
# -Werror
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -g -O2 -Wall -Werror -Wextra")
set(CMAKE_C_STANDARD 11)

file(GLOB source_files
        "include/*.h"
        "src/*.c"
        )


add_executable(HW3 ${source_files})
