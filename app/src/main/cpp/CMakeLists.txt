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

# Import LibTorch (PyTorch Mobile) Headers & Libraries
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/libtorch/include)

add_library(pytorch_jni SHARED IMPORTED)
set_target_properties(pytorch_jni PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libtorch/libs/${ANDROID_ABI}/libpytorch_jni.so)

add_library(fbjni SHARED IMPORTED)
set_target_properties(fbjni PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/libtorch/libs/${ANDROID_ABI}/libfbjni.so)

find_library(
        log-lib
        log)

target_link_libraries(
        demucs_native
        pytorch_jni
        fbjni
        ${log-lib})
