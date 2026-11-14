@file:JvmName("RoutePointLabels")

package com.universalmaps.app.routing

import androidx.annotation.StringRes
import com.universalmaps.app.R
import com.universalmaps.app.sdk.routing.RouteMarkType

@StringRes
fun pickTitle(pointType: RouteMarkType, isReplace: Boolean): Int = when (pointType) {
    RouteMarkType.Start -> if (isReplace) R.string.change_start_location else R.string.choose_start_location
    RouteMarkType.Intermediate -> if (isReplace) R.string.change_stop_along_route else R.string.route_add_destination
    RouteMarkType.Finish -> if (isReplace) R.string.change_destination else R.string.choose_destination
}
