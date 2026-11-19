package com.universalmaps.app.widget

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import com.universalmaps.app.MwmActivity
import com.universalmaps.app.MwmApplication
import com.universalmaps.app.location.TrackRecordingService
import com.universalmaps.app.sdk.display.DisplayType
import com.universalmaps.app.sdk.location.LocationUtils
import com.universalmaps.app.sdk.util.log.Logger
import java.io.IOException

/**
 * Starts track recording from the widget without opening the map.
 * Android allows starting a location foreground service only from the foreground.
 */
class TrackRecordingStartActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Normally done by SplashActivity, no-op if already initialized.
        MwmApplication.from(this).displayManager.init(DisplayType.Device)
        if (!startTrackRecording()) {
            // Let the map ask for permissions or report errors.
            val intent =
                Intent(this, MwmActivity::class.java)
                    .putExtra(MwmActivity.EXTRA_START_TRACK_RECORDING, true)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        finish()
    }

    private fun startTrackRecording(): Boolean {
        // Before core init, so that a cold start still goes through SplashActivity.
        if (!LocationUtils.checkFineLocationPermission(this) || !LocationUtils.areLocationServicesTurnedOn(this)) {
            return false
        }

        val app = MwmApplication.from(this)
        if (!app.universalMaps.arePlatformAndCoreInitialized()) {
            try {
                app.initUniversalMaps()
            } catch (e: IOException) {
                Logger.e(TAG, "Failed to initialize the app", e)
                return false
            }
        }

        TrackRecordingService.startForegroundService(this)
        return true
    }

    companion object {
        private val TAG = TrackRecordingStartActivity::class.java.simpleName

        fun createPendingIntent(context: Context): PendingIntent {
            val immutable = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) 0 else PendingIntent.FLAG_IMMUTABLE
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or immutable
            val intent = Intent(context, TrackRecordingStartActivity::class.java)
            return PendingIntent.getActivity(context, 0, intent, flags)
        }
    }
}
