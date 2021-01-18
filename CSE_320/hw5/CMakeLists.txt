cmake_minimum_required(VERSION 3.14)
project(HW5 C)
set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall -Wextra")
set(CMAKE_C_STANDARD 11)
file(GLOB source_files
        "include/*.h"
        "src/*.c"
        )

find_library(BOURSE bourse_debug.a ${CMAKE_SOURCE_DIR}/lib) # stick libraries into a varible
find_package (Threads) # import pthread

include_directories("/usr/include/criterion/") # get the header files for criterion libs

add_executable(HW5 ${source_files})
add_executable(HW5_Test tests/bourse_tests.c ${souce_files})

target_link_libraries(HW5 ${BOURSE} ${CMAKE_THREAD_LIBS_INIT}) # include the libraries in the varible
target_link_libraries(HW5_Test "/usr/lib/libcriterion.so" ${BOURSE} ${CMAKE_THREAD_LIBS_INIT}) # get the actual libraries for header files to link to.