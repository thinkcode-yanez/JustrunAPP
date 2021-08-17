package com.example.justrun.stuff

import android.graphics.Color

object Constants {

    const val REQUEST_CODE_LOCATION = 0

    const val ACTION_START_OR_RESUME_SERVICE="ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE="ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE="ACTION_STOP_SERVICE"

    const val NOTIFICATION_CHANNEL_ID="tracking_channel"
    const val NOTIFICATOIN_CHANNEL_NAME="Tracking"
    const val NOTIFICATION_ID= 1

    const val ACTION_SHOW_TRACKING_ACTIVITY="ACTION_SHOW_TRACKING_ACTIVITY"

    const val LOCATION_UPDATE_INTERVAL= 5000L
    const val LOCATION_FAST_INTERAL=500L

    const val POLYLINE_COLOR= Color.RED
    const val POLYLINE_WIDTH=8f
    const val MAP_ZOOM=19f
}