cmake_minimum_required(VERSION 3.29)
project(distinctive_character)

set(CMAKE_CXX_STANDARD 23)

add_compile_options(-g -O2 -static -lrt -Wl,--whole-archive -lpthread -Wl,--no-whole-archive)

add_executable(distinctive_character
        bfs.cpp)
