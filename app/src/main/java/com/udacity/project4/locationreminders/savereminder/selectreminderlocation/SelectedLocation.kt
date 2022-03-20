package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.os.Parcelable
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.utils.Constants
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectedLocation(
    val latLng: LatLng = Constants.DEFAULT_LOCATION_LATLNG,
    val name: String = Constants.DEFAULT_LOCATION_NAME
): Parcelable