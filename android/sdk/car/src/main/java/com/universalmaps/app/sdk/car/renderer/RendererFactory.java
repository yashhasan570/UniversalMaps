package com.universalmaps.app.sdk.car.renderer;

import androidx.annotation.NonNull;
import androidx.car.app.CarContext;
import androidx.lifecycle.LifecycleOwner;
import com.universalmaps.app.sdk.display.DisplayManager;
import com.universalmaps.app.sdk.location.LocationHelper;

public final class RendererFactory
{
  @NonNull
  public static Renderer create(@NonNull CarContext carContext, @NonNull DisplayManager displayManager,
                                @NonNull LocationHelper locationHelper, @NonNull LifecycleOwner lifecycleOwner)
  {
    return new SurfaceRenderer(carContext, displayManager, locationHelper, lifecycleOwner);
  }
}
