cmake_minimum_required(VERSION 3.22.1)

project("demucs_native")

add_library(
        demucs_native
        SHARED
        demucs_engine.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        demucs_native
        ${log-lib})
