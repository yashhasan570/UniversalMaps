package com.universalmaps.app.routing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.universalmaps.app.R
import com.universalmaps.app.base.BaseMwmToolbarFragment
import com.universalmaps.app.sdk.Framework
import com.universalmaps.app.sdk.R as SdkR
import com.universalmaps.app.sdk.routing.RouteMarkData
import com.universalmaps.app.sdk.routing.RouteMarkType
import com.universalmaps.app.sdk.routing.RoutingController
import com.universalmaps.app.util.WindowInsetUtils.BaselinePaddingInsetsListener
import com.universalmaps.app.widget.ToolbarController

/**
 * Full-screen per-leg transit route breakdown, reached by tapping the transit summary tile. The route
 * steps are re-read from the cached transit info rather than passed in, so no marshaling is needed.
 */
class TransitDetailsFragment : BaseMwmToolbarFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_transit_details, container, false)

        val recycler = root.findViewById<RecyclerView>(R.id.transit_details_recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        // A ride row animates its own intermediate-stops height when toggled; the default change animation
        // would fight that, so disable it.
        recycler.itemAnimator = null
        // Pad the scrolling list past the nav bar and the display cutout in landscape; the toolbar above
        // already consumes the top inset.
        ViewCompat.setOnApplyWindowInsetsListener(recycler, BaselinePaddingInsetsListener.excludeTop())

        val info = RoutingController.get().cachedTransitInfo
        if (info == null) {
            // No transit route was built in this process (e.g. it was restored after process death).
            requireActivity().finish()
            return root
        }
        val points = Framework.nativeGetRoutePoints()
        val destination = points.lastOrNull { it.mPointType == RouteMarkType.Finish }?.let { title(it) }
        // The user's added stops by intermediate index.
        val waypointNames = mutableMapOf<Int, String>()
        for (point in points) {
            if (point.mPointType != RouteMarkType.Intermediate) continue
            title(point)?.let { waypointNames[point.mIntermediateIndex] = it }
        }
        recycler.adapter = TransitDetailsAdapter(info.transitSteps.toList(), destination, waypointNames)

        return root
    }

    // A route point titled as the route points list titles it; null when it has no title.
    private fun title(point: RouteMarkData): String? {
        val title = if (point.mIsMyPosition) getString(SdkR.string.core_my_position) else point.mTitle
        return title?.ifEmpty { null }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbarController.setTitle(R.string.transit_details_title)
    }

    // The map activity is always directly below, so the up arrow just finishes this screen instead of
    // relaunching the parent via the default up-navigation.
    override fun onCreateToolbarController(root: View): ToolbarController =
        object : ToolbarController(root, requireActivity()) {
            override fun onUpClick() {
                requireActivity().finish()
            }
        }
}
