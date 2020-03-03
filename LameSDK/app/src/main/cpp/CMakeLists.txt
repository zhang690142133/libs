# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        lameSDK

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp lamemp3/bitstream.c  lamemp3/encoder.c  lamemp3/fft.c lamemp3/gain_analysis.c lamemp3/id3tag.c lamemp3/lame.c
        lamemp3/mpglib_interface.c lamemp3/newmdct.c lamemp3/presets.c lamemp3/psymodel.c lamemp3/quantize.c lamemp3/quantize_pvt.c lamemp3/reservoir.c
        lamemp3/set_get.c lamemp3/tables.c lamemp3/takehiro.c lamemp3/util.c lamemp3/vbrquantize.c lamemp3/VbrTag.c lamemp3/version.c)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        lameSDK

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        lameSDK

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})