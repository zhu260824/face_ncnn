cmake_minimum_required(VERSION 3.4.1)

set(lib_DIR ${CMAKE_SOURCE_DIR}/libs)
include_directories(${CMAKE_SOURCE_DIR}/includes)

set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fopenmp")
set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fopenmp")
set (CMAKE_LINKER_FLAGS "${CMAKE_LINKER_FLAGS} -fopenmp")

set (CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O2 -fvisibility=hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math")
set (CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -fvisibility=hidden -fvisibility-inlines-hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math")
set (CMAKE_LINKER_FLAGS "${CMAKE_LINKER_FLAGS} -Wl,--gc-sections")

add_library(libncnn STATIC IMPORTED)
set_target_properties(libncnn PROPERTIES IMPORTED_LOCATION ${lib_DIR}/${ANDROID_ABI}/libncnn.a)

add_library(libopencv_java3 SHARED IMPORTED)
set_target_properties(libopencv_java3 PROPERTIES IMPORTED_LOCATION ${lib_DIR}/${ANDROID_ABI}/libopencv_java3.so)

file(GLOB_RECURSE CPP_SRCS src/main/cpp/*.h
        src/main/cpp/*.cpp
        src/main/cpp/mtcnn.cpp
        src/main/cpp/mtcnn_gpu.cpp
        src/main/cpp/mtcnn_gpu_jni.cpp
        src/main/cpp/mtcnn_jni.cpp)

add_library(faceDetect SHARED ${CPP_SRCS} )

find_library(log-lib log)
find_library(jnigraphics-lib jnigraphics)
find_library(vulkan-lib vulkan)

target_link_libraries(faceDetect libncnn libopencv_java3 z ${log-lib} ${jnigraphics-lib} ${vulkan-lib})