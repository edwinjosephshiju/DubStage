cmake_minimum_required(VERSION 3.22.1)

project("demucs_native")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
        demucs_native
        SHARED
        demucs_engine.cpp)

target_compile_options(
        demucs_native
        PRIVATE
        -O3
        -ffast-math)

find_library(
        log-lib
        log)

target_link_libraries(
        demucs_native
        ${log-lib})
