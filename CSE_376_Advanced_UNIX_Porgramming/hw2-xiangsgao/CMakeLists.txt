cmake_minimum_required(VERSION 3.10)
project(HW2 C)
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")
set(CMAKE_C_STANDARD 11)

file(GLOB source_files
        "include/*.h"
        "src/*.c"
        )


add_executable(HW2 ${source_files})