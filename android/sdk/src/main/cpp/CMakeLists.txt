cmake_minimum_required(VERSION 3.22.1)

project(universalmaps C CXX)

set(SRC
  # JNI headers
  ../../../../../private.h
  com/universalmaps/app/sdk/core/jni_helper.hpp
  com/universalmaps/app/sdk/core/jni_java_methods.hpp
  com/universalmaps/app/sdk/core/logging.hpp
  com/universalmaps/app/sdk/core/ScopedEnv.hpp
  com/universalmaps/app/sdk/core/ScopedLocalRef.hpp
  com/universalmaps/app/sdk/Framework.hpp
  com/universalmaps/app/sdk/FrameworkJni.hpp
  com/universalmaps/app/sdk/opengl/android_gl_utils.hpp
  com/universalmaps/app/sdk/opengl/androidoglcontext.hpp
  com/universalmaps/app/sdk/opengl/androidoglcontextfactory.hpp
  com/universalmaps/app/sdk/opengl/gl3stub.h
  com/universalmaps/app/sdk/platform/GuiThread.hpp
  com/universalmaps/app/sdk/platform/AndroidPlatform.hpp
  com/universalmaps/app/sdk/util/Distance.hpp
  com/universalmaps/app/sdk/vulkan/android_vulkan_context_factory.hpp

  # JNI sources
  com/universalmaps/app/sdk/search/DisplayedCategories.cpp
  com/universalmaps/app/sdk/search/SearchEngine.cpp
  com/universalmaps/app/sdk/search/SearchRecents.cpp
  com/universalmaps/app/sdk/routing/RoutingJni.cpp
  com/universalmaps/app/sdk/routing/RoutingJni.hpp
  com/universalmaps/app/sdk/ChoosePositionMode.cpp
  com/universalmaps/app/sdk/MapStyle.cpp
  com/universalmaps/app/sdk/UniversalMaps.cpp
  com/universalmaps/app/sdk/Router.cpp
  com/universalmaps/app/sdk/core/jni_helper.cpp
  com/universalmaps/app/sdk/core/jni_java_methods.cpp
  com/universalmaps/app/sdk/core/logging.cpp
  com/universalmaps/app/sdk/bookmarks/data/Bookmark.cpp
  com/universalmaps/app/sdk/bookmarks/data/Bookmark.hpp
  com/universalmaps/app/sdk/bookmarks/data/BookmarkCategory.cpp
  com/universalmaps/app/sdk/bookmarks/data/BookmarkCategory.hpp
  com/universalmaps/app/sdk/bookmarks/data/BookmarkManager.cpp
  com/universalmaps/app/sdk/bookmarks/data/ElevationInfo.cpp
  com/universalmaps/app/sdk/bookmarks/data/ElevationInfo.hpp
  com/universalmaps/app/sdk/bookmarks/data/Icon.cpp
  com/universalmaps/app/sdk/bookmarks/data/Icon.hpp
  com/universalmaps/app/sdk/bookmarks/data/MapObject.cpp
  com/universalmaps/app/sdk/bookmarks/data/MapObject.hpp
  com/universalmaps/app/sdk/bookmarks/data/Metadata.cpp
  com/universalmaps/app/sdk/bookmarks/data/Metadata.hpp
  com/universalmaps/app/sdk/bookmarks/data/PredefinedColors.cpp
  com/universalmaps/app/sdk/bookmarks/data/PredefinedColors.hpp
  com/universalmaps/app/sdk/bookmarks/data/Track.cpp
  com/universalmaps/app/sdk/bookmarks/data/Track.hpp
  com/universalmaps/app/sdk/bookmarks/data/TrackStatistics.cpp
  com/universalmaps/app/sdk/bookmarks/data/TrackStatistics.hpp
  com/universalmaps/app/sdk/DownloadResourcesLegacyActivity.cpp
  com/universalmaps/app/sdk/editor/Editor.cpp
  com/universalmaps/app/sdk/editor/OpeningHours.cpp
  com/universalmaps/app/sdk/editor/OsmOAuth.cpp
  com/universalmaps/app/sdk/Framework.cpp
  com/universalmaps/app/sdk/isolines/IsolinesManager.cpp
  com/universalmaps/app/sdk/LocationState.cpp
  com/universalmaps/app/sdk/Map.cpp
  com/universalmaps/app/sdk/MapManager.cpp
  com/universalmaps/app/sdk/settings/UnitLocale.cpp
  com/universalmaps/app/sdk/settings/MapLanguageCode.cpp
  com/universalmaps/app/sdk/sound/tts.cpp
  com/universalmaps/app/sdk/subway/SubwayManager.cpp
  com/universalmaps/app/sdk/TrackRecorder.cpp
  com/universalmaps/app/sdk/TrafficState.cpp
  com/universalmaps/app/sdk/opengl/android_gl_utils.cpp
  com/universalmaps/app/sdk/opengl/androidoglcontext.cpp
  com/universalmaps/app/sdk/opengl/androidoglcontextfactory.cpp
  com/universalmaps/app/sdk/opengl/gl3stub.cpp
  com/universalmaps/app/sdk/platform/GuiThread.cpp
  com/universalmaps/app/sdk/platform/PlatformGuiThread.cpp
  com/universalmaps/app/sdk/platform/Language.cpp
  com/universalmaps/app/sdk/platform/Localization.cpp
  com/universalmaps/app/sdk/platform/AndroidPlatform.cpp
  com/universalmaps/app/sdk/platform/PThreadImpl.cpp
  com/universalmaps/app/sdk/platform/SecureStorage.cpp
  com/universalmaps/app/sdk/platform/SocketImpl.cpp
  com/universalmaps/app/sdk/util/Config.cpp
  com/universalmaps/app/sdk/util/GeoUtils.cpp
  com/universalmaps/app/sdk/util/HttpClient.cpp
  com/universalmaps/app/sdk/util/LogsManager.cpp
  com/universalmaps/app/sdk/util/NetworkPolicy.cpp
  com/universalmaps/app/sdk/util/StringUtils.cpp
  com/universalmaps/app/sdk/util/UiThread.cpp
  com/universalmaps/app/sdk/vulkan/android_vulkan_context_factory.cpp
)

omim_add_library(${PROJECT_NAME} SHARED ${SRC})

target_include_directories(${PROJECT_NAME} PRIVATE .)

target_link_libraries(${PROJECT_NAME} PRIVATE
  # Universal Maps libs
  map
  # ge0
  # tracking
  # routing
  # traffic
  # routing_common
  # drape_frontend
  # shaders
  # search
  # storage
  # descriptions
  # drape
  # kml
  # editor
  # transit
  # indexer
  # platform
  # mwm_diff
  # bsdiff
  # geometry
  # coding
  # base
  # opening_hours
  # pugixml
  # expat
  # freetype
  # minizip
  # succinct
  # stb_image
  # icu
  # vulkan_wrapper

  # Android libs
  log
  android
  EGL
  GLESv2
  atomic
)

#target_link_options(${PROJECT_NAME} PRIVATE "-fuse-ld=gold")
