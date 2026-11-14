package com.universalmaps.app.sdk.car.screens;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.car.app.Screen;
import androidx.car.app.model.Template;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.location.LocationHelper;
import com.universalmaps.app.sdk.util.log.Logger;

public abstract class BaseScreen extends Screen implements DefaultLifecycleObserver
{
  @NonNull
  private final String TAG;

  private final UniversalMaps mUniversalMapsContext;

  public BaseScreen(@NonNull CarContext carContext, @NonNull UniversalMaps universalMapsContext)
  {
    super(carContext);
    TAG = getClass().getSimpleName();
    mUniversalMapsContext = universalMapsContext;

    getLifecycle().addObserver(this);
  }

  @NonNull
  public UniversalMaps getUniversalMapsContext()
  {
    return mUniversalMapsContext;
  }

  @NonNull
  protected LocationHelper getLocationHelper()
  {
    return getUniversalMapsContext().getLocationHelper();
  }

  @NonNull
  protected abstract Template onGetTemplateImpl();

  @Override
  @NonNull
  public final Template onGetTemplate()
  {
    Logger.d(TAG);
    return onGetTemplateImpl();
  }

  @CallSuper
  public void onCreate(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
  }

  @CallSuper
  public void onStart(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
  }

  @CallSuper
  public void onResume(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
  }

  @CallSuper
  public void onPause(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
  }

  @CallSuper
  public void onStop(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
  }

  @CallSuper
  public void onDestroy(@NonNull LifecycleOwner owner)
  {
    Logger.d(TAG);
  }
}
