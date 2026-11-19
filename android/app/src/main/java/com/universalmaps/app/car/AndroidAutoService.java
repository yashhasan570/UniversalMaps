package com.universalmaps.app.car;

import androidx.annotation.NonNull;
import androidx.car.app.Session;
import androidx.car.app.SessionInfo;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationManagerCompat;
import com.universalmaps.app.BuildConfig;
import com.universalmaps.app.MwmApplication;
import com.universalmaps.app.R;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.car.CarType;
import com.universalmaps.app.sdk.car.CarTypeHelper;
import com.universalmaps.app.sdk.display.DisplayManager;
import com.universalmaps.app.sdk.display.DisplayType;
import com.universalmaps.app.sdk.util.Config;
import com.universalmaps.app.sdk.util.log.Logger;
import java.io.IOException;

public final class AndroidAutoService extends CarAppServiceBase
{
  @NonNull
  private static final String TAG = AndroidAutoService.class.getSimpleName();

  @NonNull
  public static final String ANDROID_AUTO_NOTIFICATION_CHANNEL_ID = "ANDROID_AUTO";

  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private UniversalMaps mUniversalMapsContext;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  private DisplayManager mDisplayManager;
  private boolean mInitFailed = false;

  public AndroidAutoService()
  {
    super(/* isDebug */ BuildConfig.DEBUG);
  }

  @NonNull
  @Override
  public Session onCreateSession(@NonNull SessionInfo sessionInfo)
  {
    mDisplayManager.init(DisplayType.Car);
    return new AndroidAutoSession(mUniversalMapsContext, mDisplayManager, sessionInfo, /* isDebug */ BuildConfig.DEBUG,
                                  mInitFailed);
  }

  @Override
  public void onCreate()
  {
    super.onCreate();
    createNotificationChannel();
    CarTypeHelper.setCarType(CarType.AndroidAuto);

    final MwmApplication app = MwmApplication.from(getApplicationContext());
    mUniversalMapsContext = app.getUniversalMaps();
    mDisplayManager = app.getDisplayManager();

    if (!mUniversalMapsContext.arePlatformAndCoreInitialized())
    {
      try
      {
        app.initUniversalMaps();
      }
      catch (IOException e)
      {
        Logger.e(TAG, "Failed to initialize the app: " + e.getMessage());
        mInitFailed = true;
      }
    }

    // TODO: Show dialog to the user
    Config.setFirstStartDialogSeen(getApplicationContext());
  }

  private void createNotificationChannel()
  {
    final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    final NotificationChannelCompat notificationChannel =
        new NotificationChannelCompat
            .Builder(ANDROID_AUTO_NOTIFICATION_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MIN)
            .setName(getString(R.string.car_notification_channel_name))
            .setLightsEnabled(false) // less annoying
            .setVibrationEnabled(false) // less annoying
            .build();
    notificationManager.createNotificationChannel(notificationChannel);
  }
}
