cmake_minimum_required(VERSION 3.22.1)

project(UnityIntegration)


add_library(unityLib SHARED IMPORTED)
set_target_properties(unityLib PROPERTIES
        IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libunity.so)

add_library(mainLib SHARED IMPORTED)
set_target_properties(mainLib PROPERTIES
        IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libmain.so)

add_library(burstLib SHARED IMPORTED)
set_target_properties(burstLib PROPERTIES
        IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/lib_burst_generated.so)

add_library(GameActivity SHARED
        GameActivity/UGAApplication.cpp
        GameActivity/UGAConfiguration.cpp
        GameActivity/UGADebug.cpp
        GameActivity/UGAEntry.cpp
        GameActivity/UGAInput.cpp
        GameActivity/UGAInputKeyEvent.cpp
        GameActivity/UGAInputMotionEvent.cpp
        GameActivity/UGASoftKeyboard.cpp
)


target_include_directories(GameActivity PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/GameActivity
)


find_package(game-activity REQUIRED CONFIG)

target_link_libraries(GameActivity
        unityLib
        mainLib
        burstLib
        game-activity::game-activity
        android
        log
        EGL
        GLESv3
)
