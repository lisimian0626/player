cmake_minimum_required(VERSION 3.6)

find_library(log log)
find_library(EGL EGL)
find_library(GLESv2 GLESv2)
find_library(jnigraphics jnigraphics)
find_library(libz z)
find_library(libdl dl)
find_library(libm m)

set(CMAKE_CXX_STANDARD 11)
set(CGE_DEBUG_MODE 0)
set(CGE_USE_FACE_MODULE 1)
set(CGE_USE_VIDEO_MODULE 1)

#设置生成的so动态库最后输出的路径
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# ffmpeg
add_library(ffmpeg SHARED IMPORTED)
set_target_properties(ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_LIST_DIR}/ffmpeg/${ANDROID_ABI}/libffmpeg.so)

set_property(TARGET ffmpeg PROPERTY INTERFACE_INCLUDE_DIRECTORIES ${CMAKE_CURRENT_LIST_DIR}/ffmpeg)

# CGE
add_library(CGE
        SHARED
        cge/common/cgeCommonDefine.cpp
        cge/common/cgeGLFunctions.cpp
        cge/common/cgeImageFilter.cpp
        cge/common/cgeImageHandler.cpp
        cge/common/cgeShaderFunctions.cpp
        cge/common/cgeGlobal.cpp
        cge/common/cgeTextureUtils.cpp

        cge/filters/cgeAdvancedEffects.cpp
        cge/filters/cgeAdvancedEffectsCommon.cpp
        cge/filters/cgeBilateralBlurFilter.cpp
        cge/filters/cgeMosaicBlurFilter.cpp
        cge/filters/cgeBeautifyFilter.cpp

        cge/filters/cgeBrightnessAdjust.cpp
        cge/filters/cgeColorLevelAdjust.cpp
        cge/filters/cgeContrastAdjust.cpp
        cge/filters/cgeCurveAdjust.cpp
        cge/filters/cgeExposureAdjust.cpp
        cge/filters/cgeFilterBasic.cpp
        cge/filters/cgeHueAdjust.cpp
        cge/filters/cgeMonochromeAdjust.cpp
        cge/filters/cgeSaturationAdjust.cpp
        cge/filters/cgeSelectiveColorAdjust.cpp
        cge/filters/cgeShadowHighlightAdjust.cpp
        cge/filters/cgeSharpenBlurAdjust.cpp
        cge/filters/cgeTiltshiftAdjust.cpp
        cge/filters/cgeVignetteAdjust.cpp
        cge/filters/cgeWhiteBalanceAdjust.cpp
        cge/filters/cgeColorBalanceAdjust.cpp
        cge/filters/cgeLookupFilter.cpp

        cge/filters/cgeBlendFilter.cpp

        cge/filters/cgeDataParsingEngine.cpp
        cge/filters/cgeMultipleEffects.cpp
        cge/filters/cgeMultipleEffectsCommon.cpp

        cge/filters/cgeHazeFilter.cpp
        cge/filters/cgePolarPixellateFilter.cpp
        cge/filters/cgePolkaDotFilter.cpp
        cge/filters/cgeHalftoneFilter.cpp
        cge/filters/cgeEdgeFilter.cpp
        cge/filters/cgeEmbossFilter.cpp
        cge/filters/cgeCrosshatchFilter.cpp
        cge/filters/cgeLiquidationFilter.cpp
        cge/filters/cgeRandomBlurFilter.cpp
        cge/filters/cgeMinValueFilter.cpp
        cge/filters/cgeMaxValueFilter.cpp
        cge/filters/cgeSketchFilter.cpp
        cge/filters/cgeLerpblurFilter.cpp

        cge/filters/cgeDynamicFilters.cpp
        cge/filters/cgeDynamicWaveFilter.cpp
        cge/filters/cgeMotionFlowFilter.cpp
        cge/filters/cgeColorMappingFilter.cpp
        cge/extends/cgeThread.cpp

        interface/cgeNativeLibrary.cpp
        interface/cgeFFmpegNativeLibrary.cpp
        interface/cgeSharedGLContext.cpp
        interface/cgeFrameRenderer.cpp
        interface/cgeFrameRendererWrapper.cpp
        interface/cgeFrameRecorder.cpp
        interface/cgeFrameRecorderWrapper.cpp
        interface/cgeVideoEncoder.cpp
        interface/cgeUtilFunctions.cpp
        interface/cgeVideoDecoder.cpp
        interface/cgeVideoPlayer.cpp
        interface/cgeImageHandlerAndroid.cpp
        interface/cgeImageHandlerWrapper.cpp
        )
target_include_directories(CGE PUBLIC include include/filters)
if (CGE_USE_VIDEO_MODULE)
    target_compile_definitions(CGE PRIVATE _CGE_USE_FFMPEG_)
endif ()

if (NOT CGE_USE_VIDEO_MODULE)
    target_compile_definitions(CGE PRIVATE _CGE_ONLY_FILTERS_)
endif ()

if (CGE_USE_FACE_MODULE)
    target_compile_definitions(CGE PRIVATE _CGE_USE_FFMPEG_ _CGE_USE_FACE_MODULE_)
endif ()

if (NOT CGE_RELEASE_MODE)
    target_compile_definitions(CGE PRIVATE _CGE_LOGS_)
endif ()

target_compile_definitions(CGE PUBLIC
        ANDROID_NDK
        CGE_LOG_TAG="libCGE"
        CGE_TEXTURE_PREMULTIPLIED=1
        __STDC_CONSTANT_MACROS
        _CGE_DISABLE_GLOBALCONTEXT_)

target_compile_options(CGE PUBLIC -O3 -ffast-math -frtti -std=gnu++11)
target_link_libraries(CGE ${log} ${EGL} ${GLESv2} ${jnigraphics} -latomic ffmpeg)

# CGEExt
add_library(CGEExt SHARED
        interface/cgeNativeLibrary.cpp
        interface/cgeFFmpegNativeLibrary.cpp
        interface/cgeSharedGLContext.cpp
        interface/cgeFrameRenderer.cpp
        interface/cgeFrameRendererWrapper.cpp
        interface/cgeFrameRecorder.cpp
        interface/cgeFrameRecorderWrapper.cpp
        interface/cgeVideoEncoder.cpp
        interface/cgeUtilFunctions.cpp
        interface/cgeVideoDecoder.cpp
        interface/cgeVideoPlayer.cpp
        interface/cgeImageHandlerAndroid.cpp
        interface/cgeImageHandlerWrapper.cpp

        source/cgeVideoUtils.cpp
        source/cgeCustomFilters.cpp
        source/cgeCustomFilterWrapper.cpp
        source/customHelper.cpp
        source/customFilter_0.cpp
        source/customFilter_N.cpp)
target_include_directories(CGEExt PRIVATE
        ${CMAKE_CURRENT_LIST_DIR}/include
        ${CMAKE_CURRENT_LIST_DIR}/interface)
target_link_libraries(CGEExt CGE ${log} ${EGL} ${GLESv2} ${jnigraphics} -latomic)


# OpenCV
add_library(opencv_core STATIC IMPORTED)
add_library(opencv_imgproc STATIC IMPORTED)
add_library(opencv_objdetect STATIC IMPORTED)
add_library(tbb STATIC IMPORTED)

set_target_properties(
        opencv_core
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_LIST_DIR}/opencv/${ANDROID_ABI}/libopencv_core.a)

set_property(TARGET opencv_core PROPERTY INTERFACE_INCLUDE_DIRECTORIES
        ${CMAKE_CURRENT_LIST_DIR}/opencv ${CMAKE_CURRENT_LIST_DIR}/opencv/opencv2)

set_target_properties(
        opencv_imgproc
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_LIST_DIR}/opencv/${ANDROID_ABI}/libopencv_imgproc.a)
set_target_properties(
        opencv_objdetect
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_LIST_DIR}/opencv/${ANDROID_ABI}/libopencv_objdetect.a)
set_target_properties(
        tbb
        PROPERTIES IMPORTED_LOCATION
        ${CMAKE_CURRENT_LIST_DIR}/opencv/${ANDROID_ABI}/libtbb.a)


if (CGE_USE_FACE_MODULE)

    # faceTracker
    add_library(faceTracker SHARED faceTracker/FaceTracker/CLM.cpp
            faceTracker/FaceTracker/FDet.cpp
            faceTracker/FaceTracker/PAW.cpp
            faceTracker/FaceTracker/Patch.cpp
            faceTracker/FaceTracker/FCheck.cpp
            faceTracker/FaceTracker/IO.cpp
            faceTracker/FaceTracker/PDM.cpp
            faceTracker/FaceTracker/Tracker.cpp

            faceTracker/cgeFaceTracker.cpp
            faceTracker/cgeFaceTrackerWrapper.cpp)
    target_include_directories(
            faceTracker
            PRIVATE
            ${CMAKE_CURRENT_LIST_DIR}/include
            ${CMAKE_CURRENT_LIST_DIR}/faceTracker
            ${CMAKE_CURRENT_LIST_DIR}/faceTracker/FaceTracker)

    if (${ANDROID_ABI} STREQUAL armeabi-v7a)
        target_compile_options(faceTracker PRIVATE -march=armv7-a -mfpu=neon -mfloat-abi=softfp)
    endif ()

    if (${ANDROID_ABI} STREQUAL armeabi)
        target_compile_options(faceTracker PRIVATE -mfloat-abi=softfp)
    endif ()

    target_link_libraries(faceTracker
            CGE
            opencv_objdetect
            opencv_imgproc
            opencv_core
            tbb
            ${log}
            ${jnigraphics}
            -latomic
            ${libz}
            ${libdl}
            ${libm})
endif ()