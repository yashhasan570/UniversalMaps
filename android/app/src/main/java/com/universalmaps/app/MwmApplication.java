package com.universalmaps.app;

import static com.universalmaps.app.sdk.location.LocationState.LOCATION_TAG;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.preference.PreferenceManager;
import com.universalmaps.app.background.OsmUploadWork;
import com.universalmaps.app.downloader.DownloaderNotifier;
import com.universalmaps.app.location.TrackRecordingService;
import com.universalmaps.app.routing.NavigationService;
import com.universalmaps.app.sdk.Map;
import com.universalmaps.app.sdk.MapsReadiness;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.display.DisplayManager;
import com.universalmaps.app.sdk.location.LocationHelper;
import com.universalmaps.app.sdk.location.LocationState;
import com.universalmaps.app.sdk.location.LocationUtils;
import com.universalmaps.app.sdk.location.SensorHelper;
import com.universalmaps.app.sdk.location.TrackRecorder;
import com.universalmaps.app.sdk.maplayer.isolines.IsolinesManager;
import com.universalmaps.app.sdk.maplayer.subway.SubwayManager;
import com.universalmaps.app.sdk.routing.RoutingController;
import com.universalmaps.app.sdk.util.Config;
import com.universalmaps.app.sdk.util.log.Logger;
import com.universalmaps.app.sdk.wear.WearBridge;
import com.universalmaps.app.util.ThemeSwitcher;
import com.universalmaps.app.util.Utils;
import com.universalmaps.app.widget.TrackRecordingWidget;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class MwmApplication extends Application implements Application.ActivityLifecycleCallbacks
{
  @NonNull
  private static final String TAG = MwmApplication.class.getSimpleName();

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private UniversalMaps mUniversalMaps;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private DisplayManager mDisplayManager;

  @Nullable
  private WeakReference<Activity> mTopActivity;

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  public static MwmApplication sInstance;

  @UiThread
  @Nullable
  public Activity getTopActivity()
  {
    return mTopActivity != null ? mTopActivity.get() : null;
  }

  @NonNull
  public SubwayManager getSubwayManager()
  {
    return getUniversalMaps().getSubwayManager();
  }

  @NonNull
  public IsolinesManager getIsolinesManager()
  {
    return getUniversalMaps().getIsolinesManager();
  }

  @NonNull
  public LocationHelper getLocationHelper()
  {
    return getUniversalMaps().getLocationHelper();
  }

  @NonNull
  public SensorHelper getSensorHelper()
  {
    return getUniversalMaps().getSensorHelper();
  }

  @NonNull
  public MapsReadiness getMapsReadiness()
  {
    return getUniversalMaps().getMapsReadiness();
  }

  @NonNull
  public DisplayManager getDisplayManager()
  {
    return mDisplayManager;
  }

  @NonNull
  public UniversalMaps getUniversalMaps()
  {
    return mUniversalMaps;
  }

  @NonNull
  public static MwmApplication from(@NonNull Context context)
  {
    return (MwmApplication) context.getApplicationContext();
  }

  @NonNull
  public static SharedPreferences prefs(@NonNull Context context)
  {
    return from(context).getUniversalMaps().getPreferences();
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    Logger.i(TAG, "Initializing application");

    sInstance = this;

    PreferenceManager.setDefaultValues(this, R.xml.prefs_main, false);
    mUniversalMaps = new UniversalMaps(getApplicationContext(), BuildConfig.FLAVOR, BuildConfig.APPLICATION_ID,
                                   BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME);
    getMapsReadiness().runWhenReady(() -> {
      ThemeSwitcher.INSTANCE.synchronizeApplicationTheme();
      ProcessLifecycleOwner.get().getLifecycle().addObserver(mProcessLifecycleObserver);
    });

    DownloaderNotifier.createNotificationChannel(this);
    initNavigationService();
    // Mirror navigation start/stop to a paired Wear OS device from the routing state owner, so every
    // trigger is covered (phone UI, Android Auto, notification stop). No-op unless the Google Wear
    // bridge is present; the initial publish corrects stale state left by a force-killed session.
    RoutingController.get().addNavigationStateListener(WearBridge::publishNavigating);
    WearBridge.publishNavigating(RoutingController.get().isNavigating());
    // The service outlives phone/car UI handoffs and stops when navigation ends, including in the background.
    RoutingController.get().addNavigationStateListener(navigating -> {
      if (!navigating)
      {
        NavigationService.stopService(this);
        onNavigationOrRecordingStopped();
      }
    });
    TrackRecordingService.createNotificationChannel(this);
    // Reset the widget in case the process was killed while recording.
    TrackRecordingWidget.update(this);

    registerActivityLifecycleCallbacks(this);
    mDisplayManager = new DisplayManager();
  }

  public void initUniversalMaps() throws IOException
  {
    ThemeSwitcher.INSTANCE.initialize(this);
    mUniversalMaps.init();
  }

  private final LifecycleObserver mProcessLifecycleObserver = new DefaultLifecycleObserver() {
    @Override
    public void onStart(@NonNull LifecycleOwner owner)
    {
      MwmApplication.this.onForeground();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner)
    {
      MwmApplication.this.onBackground();
    }
  };

  @Override
  public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState)
  {}

  @Override
  public void onActivityStarted(@NonNull Activity activity)
  {}

  @Override
  public void onActivityResumed(@NonNull Activity activity)
  {
    Logger.d(TAG, "activity = " + activity);
    Utils.showOnLockScreen(Config.isShowOnLockScreenEnabled(), activity);
    getSensorHelper().setRotation(activity.getWindowManager().getDefaultDisplay().getRotation());
    mTopActivity = new WeakReference<>(activity);
  }

  @Override
  public void onActivityPaused(@NonNull Activity activity)
  {
    Logger.d(TAG, "activity = " + activity);
    mTopActivity = null;
  }

  @Override
  public void onActivityStopped(@NonNull Activity activity)
  {}

  @Override
  public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState)
  {
    Logger.d(TAG, "activity = " + activity + " outState = " + outState);
  }

  @Override
  public void onActivityDestroyed(@NonNull Activity activity)
  {
    Logger.d(TAG, "activity = " + activity);
  }

  private void onForeground()
  {
    Logger.d(TAG);

    getLocationHelper().resumeLocationInForeground();
  }

  private void onBackground()
  {
    Logger.d(TAG);

    OsmUploadWork.startActionUploadOsmChanges(this);
    stopLocationInBackgroundIfUnused();
  }

  /**
   * Navigation and track recording keep the location running in the background and at a faster refresh
   * interval, and both can end while the app is in the background (notification action, arrival).
   */
  public void onNavigationOrRecordingStopped()
  {
    if (!ProcessLifecycleOwner.get().getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED))
      stopLocationInBackgroundIfUnused();
    // Whatever is still running goes back to the regular refresh interval.
    if (getLocationHelper().isActive() && LocationUtils.checkLocationPermission(this))
      getLocationHelper().restartWithNewMode();
  }

  // Recording from the widget runs without the map, so nothing else needs location then.
  public void onTrackRecordingStopped()
  {
    if (!Map.isEngineCreated() && mDisplayManager.isDeviceDisplayUsed() && !RoutingController.get().isNavigating())
      getLocationHelper().stop();
    else
      onNavigationOrRecordingStopped();
  }

  /**
   * Stops location updates while the app is in the background unless a feature still needs them there.
   */
  private void stopLocationInBackgroundIfUnused()
  {
    if (!mDisplayManager.isDeviceDisplayUsed())
      Logger.i(LOCATION_TAG, "Android Auto is active, keeping location in the background");
    else if (RoutingController.get().isNavigating())
      Logger.i(LOCATION_TAG, "Navigation is in progress, keeping location in the background");
    else if (!Map.isEngineCreated() || LocationState.getMode() == LocationState.PENDING_POSITION)
      Logger.i(LOCATION_TAG, "PENDING_POSITION mode, keeping location in the background");
    else if (TrackRecorder.nativeIsTrackRecordingEnabled())
      Logger.i(LOCATION_TAG, "Track Recordr is active, keeping location in the background");
    else
    {
      Logger.i(LOCATION_TAG, "Stopping location in the background");
      getLocationHelper().stop();
    }
  }

  private void initNavigationService()
  {
    NavigationService.createNotificationChannel(this);
    NavigationService.setUniversalMaps(getUniversalMaps());

    final int FLAG_IMMUTABLE = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? 0 : PendingIntent.FLAG_IMMUTABLE;
    final Intent contentIntent = new Intent(this, MwmActivity.class);
    final PendingIntent pendingIntent =
        PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
    NavigationService.setOpenAppPendingIntent(pendingIntent);
  }
}
