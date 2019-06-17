# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/opencv2
        src/main/cpp/opencv
        src/main/cpp/include
        src/main/cpp/)

file(GLOB MTCNN_SRC src/main/cpp/*.h
        src/main/cpp/*.cpp
        src/main/cpp/mtcnn.cpp
        src/main/cpp/mobilefacenet.cpp
        src/main/cpp/mtcnn_jni.cpp)

set(MTCNN_COMPILE_CODE ${MTCNN_SRC})

add_library(mtcnn SHARED ${MTCNN_COMPILE_CODE})

add_library(libopencv_java3 SHARED IMPORTED)
set_target_properties(libopencv_java3 PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

add_library(libncnn STATIC IMPORTED)
set_target_properties(libncnn
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libncnn.a)

find_library(log-lib log)

target_link_libraries(
        mtcnn
        libopencv_java3
        libncnn
        jnigraphics
        z
        ${log-lib})