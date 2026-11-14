package com.universalmaps.app.sdk.routing;

import androidx.annotation.Keep;

public interface RoutingRecommendationListener
{
  // Called from JNI.
  @Keep
  @SuppressWarnings("unused")
  void onRecommend(RouteRecommendationType recommendation);
}
