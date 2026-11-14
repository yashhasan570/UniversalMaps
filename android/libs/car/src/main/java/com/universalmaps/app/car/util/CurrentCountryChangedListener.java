package com.universalmaps.app.car.util;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.CarContext;
import androidx.car.app.ScreenManager;
import com.universalmaps.app.car.screens.download.DownloadMapsScreen;
import com.universalmaps.app.car.screens.download.DownloadMapsScreenBuilder;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.downloader.CountryItem;
import com.universalmaps.app.sdk.downloader.MapManager;
import com.universalmaps.app.sdk.routing.RoutingController;

public class CurrentCountryChangedListener implements MapManager.CurrentCountryChangedListener
{
  @Nullable
  private CarContext mCarContext;
  @Nullable
  private UniversalMaps mUniversalMapsContext;

  @Nullable
  private String mPreviousCountryId;

  @Override
  public void onCurrentCountryChanged(@Nullable String countryId)
  {
    if (TextUtils.isEmpty(countryId))
    {
      mPreviousCountryId = countryId;
      return;
    }

    if (mPreviousCountryId != null && mPreviousCountryId.equals(countryId))
      return;

    if (mCarContext == null || mUniversalMapsContext == null)
      return;

    final ScreenManager screenManager = mCarContext.getCarService(ScreenManager.class);

    if (DownloadMapsScreen.MARKER.equals(screenManager.getTop().getMarker()))
      return;

    if (CountryItem.fill(countryId).present || RoutingController.get().isNavigating())
      return;

    mPreviousCountryId = countryId;
    screenManager.push(new DownloadMapsScreenBuilder(mCarContext, mUniversalMapsContext)
                           .setDownloaderType(DownloadMapsScreenBuilder.DownloaderType.View)
                           .setMissingMaps(new String[] {countryId})
                           .build());
  }

  public void onStart(@NonNull final CarContext carContext, @NonNull UniversalMaps universalMapsContext)
  {
    mCarContext = carContext;
    mUniversalMapsContext = universalMapsContext;
    MapManager.nativeSubscribeOnCountryChanged(this);
  }

  public void onStop()
  {
    MapManager.nativeUnsubscribeOnCountryChanged();
    mUniversalMapsContext = null;
    mCarContext = null;
  }
}
