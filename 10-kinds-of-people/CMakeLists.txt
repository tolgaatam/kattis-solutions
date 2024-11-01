cmake_minimum_required(VERSION 3.29)
project(ten_kinds_of_people)

set(CMAKE_CXX_STANDARD 23)

add_compile_options(-g -O2 -static -lrt -Wl,--whole-archive -lpthread -Wl,--no-whole-archive)

add_executable(ten_kinds_of_people
        main.cpp)

