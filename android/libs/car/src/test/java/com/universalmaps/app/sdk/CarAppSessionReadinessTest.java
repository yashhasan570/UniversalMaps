package com.universalmaps.app.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.net.Uri;
import androidx.car.app.CarContext;
import androidx.car.app.HandshakeInfo;
import androidx.car.app.Screen;
import androidx.car.app.ScreenManager;
import androidx.car.app.SessionInfo;
import androidx.car.app.model.MessageTemplate;
import androidx.car.app.model.Template;
import androidx.car.app.testing.SessionController;
import androidx.car.app.testing.TestCarContext;
import androidx.car.app.versioning.CarAppApiLevels;
import androidx.lifecycle.Lifecycle;
import com.universalmaps.app.car.CarAppSessionBase;
import com.universalmaps.app.car.screens.PlaceScreen;
import com.universalmaps.app.car.screens.download.DownloadMapsScreen;
import com.universalmaps.app.car.util.IntentUtils;
import com.universalmaps.app.car.util.ThemeUtils;
import com.universalmaps.app.sdk.bookmarks.data.MapObject;
import com.universalmaps.app.sdk.car.CarSensorsManager;
import com.universalmaps.app.sdk.car.renderer.Renderer;
import com.universalmaps.app.sdk.car.renderer.RendererFactory;
import com.universalmaps.app.sdk.display.DisplayManager;
import com.universalmaps.app.sdk.downloader.CountryItem;
import com.universalmaps.app.sdk.downloader.MapManager;
import com.universalmaps.app.sdk.location.LocationHelper;
import com.universalmaps.app.sdk.location.LocationState;
import com.universalmaps.app.sdk.routing.RoutingController;
import com.universalmaps.app.sdk.util.StringUtils;
import com.universalmaps.app.sdk.util.log.Logger;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {CarAppSessionReadinessTest.Sdk.class, CarAppSessionReadinessTest.Formatting.class,
                   CarAppSessionReadinessTest.Position.class, CarAppSessionReadinessTest.NativeFramework.class,
                   CarAppSessionReadinessTest.MapStorage.class})
public class CarAppSessionReadinessTest
{
  private final MapsReadiness mReadiness = new MapsReadiness();
  private final List<String> mDelivered = new ArrayList<>();
  private final RoutingController mRouting = mock(RoutingController.class);
  private final DisplayManager mDisplay = mock(DisplayManager.class);
  private MockedStatic<RoutingController> mController;
  private MockedStatic<RendererFactory> mRenderers;
  private MockedStatic<IntentUtils> mIntents;
  private MockedStatic<ThemeUtils> mTheme;
  private MockedStatic<Logger> mLogger;
  private MockedConstruction<CarSensorsManager> mSensors;
  private MockedConstruction<ScreenManager> mScreens;
  private Session mSession;
  private SessionController mLifecycle;
  private ScreenManager mManager;

  @Before
  public void createSession()
  {
    mController = mockStatic(RoutingController.class);
    mController.when(RoutingController::get).thenReturn(mRouting);
    mLogger = mockStatic(Logger.class);
    mTheme = mockStatic(ThemeUtils.class);
    mIntents = mockStatic(IntentUtils.class);
    mIntents.when(() -> IntentUtils.processIntent(any(), any(), any(), any(), any())).thenAnswer(call -> {
      final String data = ((Intent) call.getArgument(4)).getDataString();
      if (data != null)
        mDelivered.add(data);
      return null;
    });
    final TestCarContext context = TestCarContext.createCarContext(RuntimeEnvironment.getApplication());
    context.updateHandshakeInfo(new HandshakeInfo("test", CarAppApiLevels.getLatest()));
    final UniversalMaps sdk = mock(UniversalMaps.class);
    when(sdk.getLocationHelper()).thenReturn(mock(LocationHelper.class));
    when(sdk.getMapsReadiness()).thenReturn(mReadiness);
    when(mDisplay.isCarDisplayUsed()).thenReturn(true);
    final Renderer renderer = mock(Renderer.class);
    mRenderers = mockStatic(RendererFactory.class);
    mRenderers.when(() -> RendererFactory.create(any(), any(), any(), any())).thenReturn(renderer);
    mSensors = mockConstruction(CarSensorsManager.class);
    mScreens = mockConstruction(ScreenManager.class);
    mSession = new Session(sdk, mDisplay);
    mManager = mScreens.constructed().get(0);
    when(mManager.getTop()).thenReturn(mock(Screen.class));
    mLifecycle = new SessionController(mSession, context, new Intent(Intent.ACTION_MAIN));
    mLifecycle.moveToState(Lifecycle.State.CREATED);
  }

  @After
  public void closeSession()
  {
    if (mLifecycle != null)
      mLifecycle.moveToState(Lifecycle.State.DESTROYED);
    mScreens.close();
    mSensors.close();
    mRenderers.close();
    mIntents.close();
    mTheme.close();
    mLogger.close();
    mController.close();
  }

  @Test
  public void newIntentsWaitForReadinessAndKeepTheirDeliveryOrder()
  {
    mSession.onNewIntent(intent("first"));
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    mSession.onNewIntent(intent("second"));
    assertEquals(List.of(), mDelivered);
    mReadiness.complete();
    assertEquals(List.of("geo:1,2?q=first", "geo:1,2?q=second"), mDelivered);
  }

  @Test
  public void stoppedSessionWaitsUntilRestartedBeforeDeliveringItsIntents()
  {
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    mSession.onNewIntent(intent("first"));
    mLifecycle.moveToState(Lifecycle.State.CREATED);
    mSession.onNewIntent(intent("second"));
    mReadiness.complete();
    assertEquals(List.of(), mDelivered);
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    assertEquals(List.of("geo:1,2?q=first", "geo:1,2?q=second"), mDelivered);
  }

  @Test
  public void displayHandoffWhileMapsLoadStillDeliversQueuedIntents()
  {
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    mSession.onNewIntent(intent("first"));
    // The phone takes the display while the car host keeps the Session started.
    when(mDisplay.isCarDisplayUsed()).thenReturn(false);
    mSession.onStop(mSession);
    mReadiness.complete();
    assertEquals(List.of("geo:1,2?q=first"), mDelivered);
  }

  @Test
  public void stoppedSessionRestoresTheRouteOnlyAfterRestart()
  {
    when(mRouting.hasSavedRoute()).thenReturn(true);
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    mLifecycle.moveToState(Lifecycle.State.CREATED);
    // A display handoff can call onStart without starting the AndroidX Session.
    mSession.onStart(mSession);
    mReadiness.complete();
    verify(mManager, never()).push(any());

    mLifecycle.moveToState(Lifecycle.State.STARTED);
    verify(mManager).push(any(PlaceScreen.class));
  }

  @Test
  public void placeScreenIsNotPushedUntilRoutingIsReady()
  {
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    mSession.onPlacePageActivated(MapObject.createMapObject(MapObject.POI, "Place", "", 1, 2));
    verify(mManager, never()).push(any());
    mReadiness.complete();
    verify(mManager).push(any(PlaceScreen.class));
  }

  @Test
  public void stoppedSessionDiscardsItsPendingPlacePage()
  {
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    mSession.onPlacePageActivated(MapObject.createMapObject(MapObject.POI, "Place", "", 1, 2));
    mLifecycle.moveToState(Lifecycle.State.CREATED);
    mReadiness.complete();
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    verify(mManager, never()).push(any());
  }

  @Test
  public void missingRegionPromptsOnlyInAStartedSessionWithLoadedMaps()
  {
    final ScreenManager screens = mSession.getCarContext().getCarService(ScreenManager.class);
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    MapStorage.reportCountry("Region");
    assertFalse(screens.getTop() instanceof DownloadMapsScreen);

    mLifecycle.moveToState(Lifecycle.State.CREATED);
    mReadiness.complete();
    MapStorage.reportCountry("Region");
    assertFalse(screens.getTop() instanceof DownloadMapsScreen);

    mLifecycle.moveToState(Lifecycle.State.STARTED);
    MapStorage.reportCountry("Region");
    assertTrue(screens.getTop() instanceof DownloadMapsScreen);
  }

  @Test
  public void routeIsNotRestoredAfterThePhoneTookTheDisplay()
  {
    when(mRouting.hasSavedRoute()).thenReturn(true);
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    // The phone owns the display, but its posted handoff callback has not stopped the session yet.
    when(mDisplay.isCarDisplayUsed()).thenReturn(false);
    mReadiness.complete();
    verify(mManager, never()).push(any());
  }

  @Test
  public void pendingPlaceIsNotShownAfterThePhoneTookTheDisplay()
  {
    mLifecycle.moveToState(Lifecycle.State.STARTED);
    mSession.onPlacePageActivated(MapObject.createMapObject(MapObject.POI, "Place", "", 1, 2));
    // The phone owns the display, but its posted handoff callback has not stopped the session yet.
    when(mDisplay.isCarDisplayUsed()).thenReturn(false);
    mReadiness.complete();
    verify(mManager, never()).push(any());
  }

  private static Intent intent(String name)
  {
    return new Intent(CarContext.ACTION_NAVIGATE, Uri.parse("geo:1,2?q=" + name));
  }

  private static final class Session extends CarAppSessionBase
  {
    Session(UniversalMaps sdk, DisplayManager display)
    {
      super(sdk, display, mock(SessionInfo.class), true);
    }

    @Override
    protected Screen prepareScreens()
    {
      return new Screen(getCarContext()) {
        @Override
        public Template onGetTemplate()
        {
          return new MessageTemplate.Builder("Root").build();
        }
      };
    }
  }

  @Implements(UniversalMaps.class)
  public static class Sdk
  {
    @Implementation
    protected static void __staticInitializer__()
    {}
  }

  @Implements(StringUtils.class)
  public static class Formatting
  {
    @Implementation
    protected static int nativeFormatSpeed(double speed)
    {
      return 0;
    }
    @Implementation
    protected static String nativeGetLocalizedSpeedUnits()
    {
      return "km/h";
    }
  }

  @Implements(LocationState.class)
  public static class Position
  {
    @Implementation
    protected static void nativeSetListener(LocationState.ModeChangeListener listener)
    {}
    @Implementation
    protected static void nativeRemoveListener()
    {}
  }

  @Implements(Framework.class)
  public static class NativeFramework
  {
    @Implementation
    protected static void nativePlacePageActivationListener(PlacePageActivationListener listener)
    {}
    @Implementation
    protected static void nativeRemovePlacePageActivationListener(PlacePageActivationListener listener)
    {}
  }

  @Implements(MapManager.class)
  public static class MapStorage
  {
    private static MapManager.CurrentCountryChangedListener sListener;

    static void reportCountry(String countryId)
    {
      if (sListener != null)
        sListener.onCurrentCountryChanged(countryId);
    }

    @Implementation
    protected static void nativeSubscribeOnCountryChanged(MapManager.CurrentCountryChangedListener listener)
    {
      sListener = listener;
    }

    @Implementation
    protected static void nativeUnsubscribeOnCountryChanged()
    {
      sListener = null;
    }

    @Implementation
    protected static void nativeGetAttributes(CountryItem country)
    {
      country.present = false;
    }

    @Implementation
    protected static String nativeGetRoot()
    {
      return "root";
    }
  }
}
