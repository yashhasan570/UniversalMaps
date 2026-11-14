package com.universalmaps.app.car;

import androidx.annotation.NonNull;
import androidx.car.app.Screen;
import androidx.car.app.SessionInfo;
import androidx.lifecycle.LifecycleOwner;
import com.universalmaps.app.R;
import com.universalmaps.app.car.screens.ErrorScreen;
import com.universalmaps.app.car.screens.MapPlaceholderScreen;
import com.universalmaps.app.car.screens.MapScreen;
import com.universalmaps.app.car.screens.download.DownloadMapsScreenBuilder;
import com.universalmaps.app.car.screens.download.DownloaderHelpers;
import com.universalmaps.app.car.screens.permissions.RequestPermissionsScreenBuilder;
import com.universalmaps.app.car.util.UserActionRequired;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.display.DisplayChangedListener;
import com.universalmaps.app.sdk.display.DisplayManager;
import com.universalmaps.app.sdk.display.DisplayType;
import com.universalmaps.app.sdk.location.LocationUtils;
import com.universalmaps.app.sdk.util.log.Logger;
import java.util.ArrayList;
import java.util.List;

public final class AndroidAutoSession extends CarAppSessionBase implements DisplayChangedListener
{
  private static final String TAG = AndroidAutoSession.class.getSimpleName();

  private final boolean mInitFailed;

  public AndroidAutoSession(@NonNull UniversalMaps universalMapsContext, @NonNull DisplayManager displayManager,
                            @NonNull SessionInfo sessionInfo, boolean isDebug, boolean initFailed)
  {
    super(universalMapsContext, displayManager, sessionInfo, isDebug);
    mInitFailed = initFailed;
  }

  @Override
  public void onCreate(@NonNull LifecycleOwner owner)
  {
    super.onCreate(owner);
    mDisplayManager.addListener(DisplayType.Car, this);
  }

  @Override
  public void onDestroy(@NonNull LifecycleOwner owner)
  {
    super.onDestroy(owner);
    mDisplayManager.removeListener(DisplayType.Car);
  }

  @NonNull
  protected Screen prepareScreens()
  {
    if (mInitFailed)
      return new ErrorScreen.Builder(getCarContext(), mUniversalMapsContext)
          .setErrorMessage(R.string.dialog_error_storage_message)
          .build();

    final List<Screen> screensStack = new ArrayList<>();
    screensStack.add(new MapScreen(getCarContext(), mUniversalMapsContext, mSurfaceRenderer));

    if (DownloaderHelpers.isWorldMapsDownloadNeeded(mUniversalMapsContext.getFlavor()))
    {
      mScreenManager.push(new DownloadMapsScreenBuilder(getCarContext(), mUniversalMapsContext)
                              .setDownloaderType(DownloadMapsScreenBuilder.DownloaderType.FirstLaunch)
                              .build());
    }

    if (!LocationUtils.checkFineLocationPermission(getCarContext()))
      screensStack.add(
          RequestPermissionsScreenBuilder.build(getCarContext(), mUniversalMapsContext, mSensorsManager::onStart));

    if (mDisplayManager.isDeviceDisplayUsed())
    {
      mSurfaceRenderer.disable();
      onStop(this);
      screensStack.add(new MapPlaceholderScreen(getCarContext(), mUniversalMapsContext));
    }

    for (int i = 0; i < screensStack.size() - 1; i++)
      mScreenManager.push(screensStack.get(i));

    return screensStack.get(screensStack.size() - 1);
  }

  @Override
  public void onDisplayChangedToDevice(@NonNull Runnable onTaskFinishedCallback)
  {
    Logger.d(TAG);
    final Screen topScreen = mScreenManager.getTop();
    onStop(this);
    mSurfaceRenderer.disable();

    final MapPlaceholderScreen mapPlaceholderScreen = new MapPlaceholderScreen(getCarContext(), mUniversalMapsContext);
    if (topScreen instanceof UserActionRequired)
      mScreenManager.popToRoot();

    mScreenManager.push(mapPlaceholderScreen);

    onTaskFinishedCallback.run();
  }

  @Override
  public void onDisplayChangedToCar(@NonNull Runnable onTaskFinishedCallback)
  {
    Logger.d(TAG);
    onStart(this);
    mSurfaceRenderer.enable();

    if (mScreenManager.getTop() instanceof MapPlaceholderScreen)
      mScreenManager.pop();

    onTaskFinishedCallback.run();
  }
}
