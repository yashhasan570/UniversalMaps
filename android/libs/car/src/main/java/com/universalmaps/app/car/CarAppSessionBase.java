package com.universalmaps.app.car;

import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.car.app.Screen;
import androidx.car.app.ScreenManager;
import androidx.car.app.Session;
import androidx.car.app.SessionInfo;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import com.universalmaps.app.car.screens.NavigationScreen;
import com.universalmaps.app.car.screens.PlaceScreen;
import com.universalmaps.app.car.screens.download.DownloadMapsScreen;
import com.universalmaps.app.car.util.CurrentCountryChangedListener;
import com.universalmaps.app.car.util.IntentUtils;
import com.universalmaps.app.car.util.ThemeUtils;
import com.universalmaps.app.sdk.Framework;
import com.universalmaps.app.sdk.MapsReadiness;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.PlacePageActivationListener;
import com.universalmaps.app.sdk.bookmarks.data.MapObject;
import com.universalmaps.app.sdk.car.CarSensorsManager;
import com.universalmaps.app.sdk.car.renderer.Renderer;
import com.universalmaps.app.sdk.car.renderer.RendererFactory;
import com.universalmaps.app.sdk.car.screens.BaseMapScreen;
import com.universalmaps.app.sdk.display.DisplayManager;
import com.universalmaps.app.sdk.location.LocationListener;
import com.universalmaps.app.sdk.location.LocationState;
import com.universalmaps.app.sdk.location.LocationUtils;
import com.universalmaps.app.sdk.routing.RoutingController;
import com.universalmaps.app.sdk.util.StringUtils;
import com.universalmaps.app.sdk.util.log.Logger;
import com.universalmaps.app.sdk.widget.placepage.PlacePageData;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public abstract class CarAppSessionBase
    extends Session implements DefaultLifecycleObserver, LocationState.ModeChangeListener, PlacePageActivationListener
{
  @NonNull
  private static final String TAG = CarAppSessionBase.class.getSimpleName();

  @NonNull
  protected final UniversalMaps mUniversalMapsContext;
  @NonNull
  protected final SessionInfo mSessionInfo;
  @NonNull
  protected final DisplayManager mDisplayManager;
  protected final boolean mIsDebug;
  @NonNull
  protected final ScreenManager mScreenManager;
  @NonNull
  protected final CurrentCountryChangedListener mCurrentCountryChangedListener;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  protected Renderer mSurfaceRenderer;
  @SuppressWarnings("NotNullFieldNotInitialized")
  @NonNull
  protected CarSensorsManager mSensorsManager;

  @NonNull
  private final LocationListener mSpeedLocationListener = this::updateSpeed;
  private boolean mSpeedLimitSignTypeInitialized;
  @NonNull
  private final MapsReadiness mMapsReadiness;
  private final Queue<Intent> mPendingIntents = new ArrayDeque<>();
  @Nullable
  private MapObject mPendingPlace;
  @NonNull
  private final Runnable mCarDisplayWhenReady = this::onCarDisplayReady;
  @NonNull
  private final Runnable mIntentWhenReady = this::processPendingIntents;
  @NonNull
  private final Runnable mPlaceWhenReady = this::showPendingPlace;

  public CarAppSessionBase(@NonNull UniversalMaps universalMapsContext, @NonNull DisplayManager displayManager,
                           @NonNull SessionInfo sessionInfo, boolean isDebug)
  {
    mUniversalMapsContext = universalMapsContext;
    mMapsReadiness = universalMapsContext.getMapsReadiness();
    mSessionInfo = sessionInfo;
    mDisplayManager = displayManager;
    mIsDebug = isDebug;
    mScreenManager = getCarContext().getCarService(ScreenManager.class);
    mCurrentCountryChangedListener = new CurrentCountryChangedListener();
    getLifecycle().addObserver(this);
  }

  @CallSuper
  @Override
  public void onCreate(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
    mSurfaceRenderer =
        RendererFactory.create(getCarContext(), mDisplayManager, mUniversalMapsContext.getLocationHelper(), owner);
    mSensorsManager = new CarSensorsManager(getCarContext(), mUniversalMapsContext.getSensorHelper(),
                                            mUniversalMapsContext.getLocationHelper());
  }

  @CallSuper
  @Override
  public void onCarConfigurationChanged(@NonNull Configuration newConfiguration)
  {
    Logger.d(TAG, "New configuration: " + newConfiguration);
    if (mDisplayManager.isCarDisplayUsed())
      ThemeUtils.update(getCarContext(), mSurfaceRenderer.isRenderingActive());
  }

  @NonNull
  @Override
  public final Screen onCreateScreen(@NonNull Intent intent)
  {
    Logger.d(TAG);

    Logger.i(TAG, "Session info: " + mSessionInfo);
    Logger.i(TAG, "API Level: " + getCarContext().getCarAppApiLevel());
    Logger.i(TAG, "Supported templates: " + mSessionInfo.getSupportedTemplates(getCarContext().getCarAppApiLevel()));
    Logger.i(TAG, "Host info: " + getCarContext().getHostInfo());
    Logger.i(TAG, "Car configuration: " + getCarContext().getResources().getConfiguration());

    return prepareScreens();
  }

  @Override
  public final void onNewIntent(@NonNull Intent intent)
  {
    Logger.d(TAG, intent.toString());
    mPendingIntents.add(intent);
    if (isStarted())
      mMapsReadiness.runWhenReady(mIntentWhenReady);
  }

  private void processPendingIntents()
  {
    // Preserve host delivery order, including intents received while stopped or maps are loading.
    while (isStarted() && !mPendingIntents.isEmpty())
    {
      final Intent intent = mPendingIntents.remove();
      IntentUtils.processIntent(getCarContext(), mUniversalMapsContext, mSurfaceRenderer, mDisplayManager, intent);
    }
  }

  @CallSuper
  @Override
  public void onStart(@NonNull LifecycleOwner owner)
  {
    // A display handoff can call onStart() while the car host has stopped the Session.
    if (!isStarted())
      return;

    Logger.d(TAG);
    mSpeedLimitSignTypeInitialized = false;
    updateSpeed(mUniversalMapsContext.getLocationHelper().getSavedLocation());
    mUniversalMapsContext.getLocationHelper().addListener(mSpeedLocationListener);
    if (mDisplayManager.isCarDisplayUsed())
    {
      LocationState.nativeSetListener(this);
      Framework.nativePlacePageActivationListener(this);
    }

    if (LocationUtils.checkFineLocationPermission(getCarContext()))
      mSensorsManager.onStart();

    if (mDisplayManager.isCarDisplayUsed())
    {
      ThemeUtils.update(getCarContext(), mSurfaceRenderer.isRenderingActive());
      mMapsReadiness.runWhenReady(mCarDisplayWhenReady);
    }
    if (!mPendingIntents.isEmpty())
      mMapsReadiness.runWhenReady(mIntentWhenReady);
  }

  @CallSuper
  @Override
  public void onStop(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
    mMapsReadiness.cancel(mCarDisplayWhenReady);
    // A display handoff keeps the host Session started, so its queued intents still run once maps load.
    if (!isStarted())
      mMapsReadiness.cancel(mIntentWhenReady);
    mMapsReadiness.cancel(mPlaceWhenReady);
    mPendingPlace = null;

    mUniversalMapsContext.getLocationHelper().removeListener(mSpeedLocationListener);

    mSensorsManager.onStop();

    if (mDisplayManager.isCarDisplayUsed())
    {
      LocationState.nativeRemoveListener();
      Framework.nativeRemovePlacePageActivationListener(this);
    }

    mCurrentCountryChangedListener.onStop();
  }

  private void onCarDisplayReady()
  {
    if (!canShowCarScreen())
      return;

    // Downloaded regions look missing to the download prompt until maps are registered.
    mCurrentCountryChangedListener.onStart(getCarContext(), mUniversalMapsContext);
    onRestoreRoute();
  }

  private boolean isStarted()
  {
    return getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
  }

  private boolean canShowCarScreen()
  {
    // Display ownership can change before the handoff callback runs.
    return isStarted() && mDisplayManager.isCarDisplayUsed();
  }

  @NonNull
  protected abstract Screen prepareScreens();

  private void updateSpeed(@Nullable Location location)
  {
    if (location != null && !mSpeedLimitSignTypeInitialized)
    {
      mSurfaceRenderer.setSpeedLimitSignType(
          Framework.nativeGetSpeedLimitSignType(location.getLatitude(), location.getLongitude()));
      mSpeedLimitSignTypeInitialized = true;
    }
    mSurfaceRenderer.setCurrentSpeed(
        StringUtils.nativeFormatSpeed(location != null && location.hasSpeed() ? location.getSpeed() : 0),
        StringUtils.nativeGetLocalizedSpeedUnits());
  }

  @Override
  public final void onMyPositionModeChanged(int newMode)
  {
    final Screen screen = mScreenManager.getTop();
    if (screen instanceof BaseMapScreen)
      screen.invalidate();
  }

  @Override
  public final void onPlacePageActivated(@NonNull PlacePageData data)
  {
    if (!canShowCarScreen())
      return;
    mPendingPlace = (MapObject) data;
    mMapsReadiness.runWhenReady(mPlaceWhenReady);
  }

  private void showPendingPlace()
  {
    final MapObject mapObject = Objects.requireNonNull(mPendingPlace);
    mPendingPlace = null;
    if (!canShowCarScreen())
      return;
    // A place queued during startup can be delivered while a download prompt is visible.
    if (DownloadMapsScreen.MARKER.equals(mScreenManager.getTop().getMarker()))
      return;

    // Don't display the PlaceScreen for 'MY_POSITION' or during navigation
    // TODO (AndrewShkrob): Implement the 'Add stop' functionality
    if (mapObject.isMyPosition() || RoutingController.get().isNavigating())
    {
      Framework.nativeDeactivatePopup();
      return;
    }
    final PlaceScreen placeScreen = new PlaceScreen.Builder(getCarContext(), mUniversalMapsContext, mSurfaceRenderer)
                                        .setDebugMode(mIsDebug)
                                        .setMapObject(mapObject)
                                        .build();
    mScreenManager.popToRoot();
    mScreenManager.push(placeScreen);
  }

  @Override
  public final void onPlacePageDeactivated()
  {
    mPendingPlace = null;
    mMapsReadiness.cancel(mPlaceWhenReady);
    // The function is called when we close the PlaceScreen or when we enter the navigation mode.
    // We only need to handle the first case.
    if (!canShowCarScreen() || !(mScreenManager.getTop() instanceof PlaceScreen))
      return;

    // Do NOT cancel routing here. This callback fires synchronously from the C++ tap handler
    // (Framework::OnTapEvent → DeactivateMapSelection → m_onPlacePageClose), including the Ruler
    // "extend route by tap" path which wipes the Finish mark mid-handler and aborts when the
    // stack unwinds back to ContinueRouteToPoint. User-initiated close goes through
    // PlaceScreen.onBackPressed(), which already calls RoutingController.cancel() explicitly.
    mScreenManager.popToRoot();
  }

  private void onRestoreRoute()
  {
    final RoutingController routingController = RoutingController.get();
    final boolean isNavigating = routingController.isNavigating();
    final boolean hasNavigatingScreen = hasNavigationScreenInStack();

    if (!isNavigating && hasNavigatingScreen)
      mScreenManager.popToRoot();

    if (isNavigating && routingController.getLastRouterType() == PlaceScreen.ROUTER && hasNavigatingScreen)
    {
      mScreenManager.popTo(NavigationScreen.MARKER);
      return;
    }

    if (routingController.isPlanning() || isNavigating || routingController.hasSavedRoute())
    {
      final PlaceScreen placeScreen = new PlaceScreen.Builder(getCarContext(), mUniversalMapsContext, mSurfaceRenderer)
                                          .setMapObject(routingController.getEndPoint())
                                          .setDebugMode(mIsDebug)
                                          .build();
      mScreenManager.popToRoot();
      mScreenManager.push(placeScreen);
    }
  }

  private boolean hasNavigationScreenInStack()
  {
    for (final Screen screen : mScreenManager.getScreenStack())
    {
      if (NavigationScreen.MARKER.equals(screen.getMarker()))
        return true;
    }
    return false;
  }
}
