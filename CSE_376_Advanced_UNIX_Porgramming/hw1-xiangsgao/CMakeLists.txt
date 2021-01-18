cmake_minimum_required(VERSION 3.10)
project(HW1 C)
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")
set(CMAKE_C_STANDARD 11)

file(GLOB source_files
        "include/*.h"
        "src/*.c"
        )

link_libraries(crypto)


configure_file(${CMAKE_CURRENT_SOURCE_DIR}/test/password.txt ${CMAKE_CURRENT_BINARY_DIR}/password.txt COPYONLY)
configure_file(${CMAKE_CURRENT_SOURCE_DIR}/test/test.txt ${CMAKE_CURRENT_BINARY_DIR}/test.txt COPYONLY)


add_executable(HW1 ${source_files})
