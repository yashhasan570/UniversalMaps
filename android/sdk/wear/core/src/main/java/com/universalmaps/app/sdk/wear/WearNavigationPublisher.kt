package com.universalmaps.app.sdk.wear

import com.universalmaps.app.wear.protocol.WearNavigationDetails
import com.universalmaps.app.wear.protocol.WearNavigationMode

/** Phone-side port for persistent mode and transient navigation details. */
interface WearNavigationPublisher {
    fun publish(mode: WearNavigationMode)

    fun publish(details: WearNavigationDetails)

    /** Stop redistributing instructions whose GPS/routing source is no longer valid. */
    fun invalidateDetails()
}
