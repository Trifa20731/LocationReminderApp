package com.udacity.project4.utils

import com.google.android.gms.maps.model.LatLng


object Constants {
    // Activity Request Code.
    const val SIGN_IN_REQUEST_CODE: Int = 1997
    const val REQUEST_LOCATION_PERMISSION: Int = 1998
    const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 1933
    const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 1934
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 1929
    const val LOCATION_PERMISSION_INDEX = 0
    const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    // Constant Map Setting Data.
    val HONG_KONG_LATLNG: LatLng = LatLng(22.302711, 114.177216)
    const val ZOOM_LEVEL: Float = 15f

}