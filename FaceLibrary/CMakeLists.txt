cmake_minimum_required(VERSION 3.4.1)

# openmp
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp -static-openmp")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp -static-openmp")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fopenmp -static-openmp")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fomit-frame-pointer -fstrict-aliasing -ffast-math")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fomit-frame-pointer -fstrict-aliasing -ffast-math")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fvisibility=hidden")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden")

# disable rtti and exceptions
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-rtti -fno-exceptions")


set(lib_DIR ${CMAKE_SOURCE_DIR}/libs)
include_directories(${CMAKE_SOURCE_DIR}/cpp_include)

add_library(libncnn STATIC IMPORTED)
set_target_properties(libncnn PROPERTIES IMPORTED_LOCATION ${lib_DIR}/${ANDROID_ABI}/libncnn.a)

add_library(libopencv_java SHARED IMPORTED)
set_target_properties(libopencv_java PROPERTIES IMPORTED_LOCATION ${lib_DIR}/${ANDROID_ABI}/libopencv_java4.so)

file(GLOB_RECURSE CPP_SRCS
        src/main/cpp/ultra_face.h
        src/main/cpp/ultra_face.cpp
        src/main/cpp/mobile_facenet.h
        src/main/cpp/mobile_facenet.cpp
        src/main/cpp/base_util.h
        src/main/cpp/base_util.cpp
        src/main/cpp/face_jni.cpp)

add_library(faceLibrary SHARED ${CPP_SRCS})

find_library(log-lib log android)
find_library(jnigraphics-lib jnigraphics)
find_library(vulkan-lib vulkan)

target_link_libraries(faceLibrary libncnn libopencv_java z android ${log-lib} ${jnigraphics-lib} ${vulkan-lib})