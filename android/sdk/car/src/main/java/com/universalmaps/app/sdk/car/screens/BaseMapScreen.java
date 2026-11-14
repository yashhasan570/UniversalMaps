package com.universalmaps.app.sdk.car.screens;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import com.universalmaps.app.sdk.UniversalMaps;
import com.universalmaps.app.sdk.car.renderer.Renderer;

public abstract class BaseMapScreen extends BaseScreen
{
  @NonNull
  private final Renderer mSurfaceRenderer;

  public BaseMapScreen(@NonNull CarContext carContext, @NonNull UniversalMaps universalMapsContext,
                       @NonNull Renderer surfaceRenderer)
  {
    super(carContext, universalMapsContext);
    mSurfaceRenderer = surfaceRenderer;
  }

  @NonNull
  protected Renderer getSurfaceRenderer()
  {
    return mSurfaceRenderer;
  }
}
