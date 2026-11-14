package com.universalmaps.app.sdk.api;

import androidx.annotation.Keep;
import com.universalmaps.app.sdk.Router;

/**
 * Represents Framework::ParsedRoutingData from core.
 */
// Used by JNI.
@Keep
@SuppressWarnings("unused")
public class ParsedRoutingData
{
  public final RoutePoint[] mPoints;
  public final Router mRouterType;

  public ParsedRoutingData(RoutePoint[] points, int routerType)
  {
    this.mPoints = points;
    this.mRouterType = Router.valueOf(routerType);
  }
}
